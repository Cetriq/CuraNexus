package se.curanexus.authorization.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.curanexus.authorization.api.dto.*;
import se.curanexus.authorization.domain.Role;
import se.curanexus.authorization.service.AuthorizationService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {

    private final AuthorizationService authorizationService;

    public RoleController(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @PostMapping
    public ResponseEntity<RoleResponse> createRole(@Valid @RequestBody CreateRoleRequest request) {
        Role role = authorizationService.createRole(
                request.name(),
                request.code(),
                request.description()
        );

        if (request.permissionCodes() != null && !request.permissionCodes().isEmpty()) {
            role = authorizationService.addPermissionsToRole(role.getId(), request.permissionCodes());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(RoleResponse.from(role));
    }

    @GetMapping
    public ResponseEntity<List<RoleResponse>> getAllRoles(
            @RequestParam(required = false) Boolean systemOnly) {

        List<Role> roles;
        if (Boolean.TRUE.equals(systemOnly)) {
            roles = authorizationService.getSystemRoles();
        } else if (Boolean.FALSE.equals(systemOnly)) {
            roles = authorizationService.getCustomRoles();
        } else {
            roles = authorizationService.getAllRoles();
        }

        List<RoleResponse> responses = roles.stream()
                .map(RoleResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{roleId}")
    public ResponseEntity<RoleResponse> getRole(@PathVariable UUID roleId) {
        Role role = authorizationService.getRoleWithPermissions(roleId);
        return ResponseEntity.ok(RoleResponse.from(role));
    }

    @GetMapping("/by-code/{code}")
    public ResponseEntity<RoleResponse> getRoleByCode(@PathVariable String code) {
        Role role = authorizationService.getRoleByCode(code);
        return ResponseEntity.ok(RoleResponse.from(role));
    }

    @PutMapping("/{roleId}")
    public ResponseEntity<RoleResponse> updateRole(
            @PathVariable UUID roleId,
            @Valid @RequestBody UpdateRoleRequest request) {

        Role role = authorizationService.updateRole(
                roleId,
                request.name(),
                request.description()
        );
        return ResponseEntity.ok(RoleResponse.from(role));
    }

    @DeleteMapping("/{roleId}")
    public ResponseEntity<Void> deleteRole(@PathVariable UUID roleId) {
        authorizationService.deleteRole(roleId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{roleId}/permissions")
    public ResponseEntity<RoleResponse> addPermissions(
            @PathVariable UUID roleId,
            @Valid @RequestBody AssignPermissionsRequest request) {

        Role role = authorizationService.addPermissionsToRole(roleId, request.permissionCodes());
        return ResponseEntity.ok(RoleResponse.from(role));
    }

    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    public ResponseEntity<RoleResponse> removePermission(
            @PathVariable UUID roleId,
            @PathVariable UUID permissionId) {

        Role role = authorizationService.removePermissionFromRole(roleId, permissionId);
        return ResponseEntity.ok(RoleResponse.from(role));
    }
}
