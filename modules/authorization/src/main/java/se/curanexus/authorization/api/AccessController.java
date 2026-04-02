package se.curanexus.authorization.api;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.curanexus.authorization.api.dto.AccessCheckResponse;
import se.curanexus.authorization.api.dto.CheckAccessRequest;
import se.curanexus.authorization.service.AuthorizationService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/access")
public class AccessController {

    private final AuthorizationService authorizationService;

    public AccessController(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
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
}
