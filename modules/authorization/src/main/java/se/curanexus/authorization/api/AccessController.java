package se.curanexus.authorization.api;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.curanexus.authorization.abac.AccessContext;
import se.curanexus.authorization.abac.AccessControlService;
import se.curanexus.authorization.abac.AccessDecision;
import se.curanexus.authorization.api.dto.*;
import se.curanexus.authorization.service.AuthorizationService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/access")
public class AccessController {

    private final AuthorizationService authorizationService;
    private final AccessControlService accessControlService;

    public AccessController(AuthorizationService authorizationService,
                           AccessControlService accessControlService) {
        this.authorizationService = authorizationService;
        this.accessControlService = accessControlService;
    }

    @PostMapping("/check")
    public ResponseEntity<AccessCheckResponse> checkAccess(@Valid @RequestBody CheckAccessRequest request) {
        // Check multiple permissions if provided
        if (request.permissionCodes() != null && !request.permissionCodes().isEmpty()) {
            Map<String, Boolean> results = new HashMap<>();
            boolean allGranted = true;
            for (String code : request.permissionCodes()) {
                boolean hasPermission = authorizationService.hasPermission(request.userId(), code);
                results.put(code, hasPermission);
                if (!hasPermission) allGranted = false;
            }
            return ResponseEntity.ok(AccessCheckResponse.withPermissions(allGranted, results));
        }

        // Check single permission
        if (request.permissionCode() != null) {
            boolean hasPermission = authorizationService.hasPermission(request.userId(), request.permissionCode());
            if (!hasPermission) {
                return ResponseEntity.ok(AccessCheckResponse.denied("Missing permission: " + request.permissionCode()));
            }
        }

        // Check patient access if patientId provided
        if (request.patientId() != null) {
            boolean hasCareRelation = authorizationService.hasCareRelation(request.userId(), request.patientId());
            if (!hasCareRelation) {
                return ResponseEntity.ok(AccessCheckResponse.denied("No active care relation with patient"));
            }
        }

        return ResponseEntity.ok(AccessCheckResponse.accessGranted());
    }

    @GetMapping("/check/permission")
    public ResponseEntity<AccessCheckResponse> checkPermission(
            @RequestParam UUID userId,
            @RequestParam String permissionCode) {

        boolean hasPermission = authorizationService.hasPermission(userId, permissionCode);
        if (hasPermission) {
            return ResponseEntity.ok(AccessCheckResponse.accessGranted());
        } else {
            return ResponseEntity.ok(AccessCheckResponse.denied("Missing permission: " + permissionCode));
        }
    }

    @GetMapping("/check/role")
    public ResponseEntity<AccessCheckResponse> checkRole(
            @RequestParam UUID userId,
            @RequestParam String roleCode) {

        boolean hasRole = authorizationService.hasRole(userId, roleCode);
        if (hasRole) {
            return ResponseEntity.ok(AccessCheckResponse.accessGranted());
        } else {
            return ResponseEntity.ok(AccessCheckResponse.denied("Missing role: " + roleCode));
        }
    }

    @GetMapping("/check/care-relation")
    public ResponseEntity<AccessCheckResponse> checkCareRelation(
            @RequestParam UUID userId,
            @RequestParam UUID patientId) {

        boolean hasCareRelation = authorizationService.hasCareRelation(userId, patientId);
        if (hasCareRelation) {
            return ResponseEntity.ok(AccessCheckResponse.accessGranted());
        } else {
            return ResponseEntity.ok(AccessCheckResponse.denied("No active care relation with patient"));
        }
    }

    @GetMapping("/check/patient-access")
    public ResponseEntity<AccessCheckResponse> checkPatientAccess(
            @RequestParam UUID userId,
            @RequestParam UUID patientId) {

        boolean canAccess = authorizationService.canAccessPatient(userId, patientId);
        if (canAccess) {
            return ResponseEntity.ok(AccessCheckResponse.accessGranted());
        } else {
            return ResponseEntity.ok(AccessCheckResponse.denied("Cannot access patient - missing permission or care relation"));
        }
    }

    // ========== ABAC Endpoints ==========

    /**
     * Contextual access check using ABAC policies.
     * This is the primary endpoint for access control in the system.
     */
    @PostMapping("/check/contextual")
    public ResponseEntity<AccessDecisionResponse> checkContextualAccess(
            @Valid @RequestBody ContextualAccessRequest request) {

        AccessContext.Builder builder = AccessContext.builder()
                .userId(request.userId())
                .patientId(request.patientId())
                .encounterId(request.encounterId())
                .resourceType(request.resourceType())
                .resourceId(request.resourceId())
                .action(request.action())
                .userType(request.userType())
                .department(request.department())
                .unit(request.unit())
                .clientIp(request.clientIp())
                .clientApplication(request.clientApplication());

        if (request.emergencyAccess()) {
            builder.emergencyAccess(true);
            builder.accessReason(request.accessReason());
        }

        if (request.additionalAttributes() != null) {
            request.additionalAttributes().forEach(builder::attribute);
        }

        AccessDecision decision = accessControlService.checkAccess(builder.build());
        return ResponseEntity.ok(AccessDecisionResponse.from(decision));
    }

    /**
     * Emergency access (nödåtkomst) - bypasses care relation but requires reason.
     * All emergency access is specially logged.
     */
    @PostMapping("/emergency")
    public ResponseEntity<AccessDecisionResponse> requestEmergencyAccess(
            @Valid @RequestBody EmergencyAccessRequest request) {

        AccessDecision decision = accessControlService.checkEmergencyAccess(
                request.userId(),
                request.patientId(),
                request.reason()
        );
        return ResponseEntity.ok(AccessDecisionResponse.from(decision));
    }

    /**
     * Quick check for patient access (optimized for performance).
     */
    @GetMapping("/check/patient/{patientId}")
    public ResponseEntity<AccessCheckResponse> canAccessPatient(
            @RequestParam UUID userId,
            @PathVariable UUID patientId) {

        boolean canAccess = accessControlService.canAccessPatient(userId, patientId);
        if (canAccess) {
            return ResponseEntity.ok(AccessCheckResponse.accessGranted());
        } else {
            return ResponseEntity.ok(AccessCheckResponse.denied("No access to patient"));
        }
    }
}
