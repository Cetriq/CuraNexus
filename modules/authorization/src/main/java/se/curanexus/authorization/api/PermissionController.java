package se.curanexus.authorization.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.curanexus.authorization.api.dto.PermissionResponse;
import se.curanexus.authorization.domain.Permission;
import se.curanexus.authorization.domain.ResourceType;
import se.curanexus.authorization.service.AuthorizationService;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/permissions")
public class PermissionController {

    private final AuthorizationService authorizationService;

    public PermissionController(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @GetMapping
    public ResponseEntity<List<PermissionResponse>> getAllPermissions(
            @RequestParam(required = false) ResourceType resource) {

        List<Permission> permissions;
        if (resource != null) {
            permissions = authorizationService.getPermissionsByResource(resource);
        } else {
            permissions = authorizationService.getAllPermissions();
        }

        List<PermissionResponse> responses = permissions.stream()
                .map(PermissionResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{permissionId}")
    public ResponseEntity<PermissionResponse> getPermission(@PathVariable UUID permissionId) {
        Permission permission = authorizationService.getPermission(permissionId);
        return ResponseEntity.ok(PermissionResponse.from(permission));
    }

    @GetMapping("/by-code/{code}")
    public ResponseEntity<PermissionResponse> getPermissionByCode(@PathVariable String code) {
        Permission permission = authorizationService.getPermissionByCode(code);
        return ResponseEntity.ok(PermissionResponse.from(permission));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PermissionResponse>> getUserPermissions(@PathVariable UUID userId) {
        Set<Permission> permissions = authorizationService.getUserPermissions(userId);
        List<PermissionResponse> responses = permissions.stream()
                .map(PermissionResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }
}
