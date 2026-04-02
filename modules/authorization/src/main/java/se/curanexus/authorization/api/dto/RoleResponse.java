package se.curanexus.authorization.api.dto;

import se.curanexus.authorization.domain.Role;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RoleResponse(
        UUID id,
        String name,
        String code,
        String description,
        boolean systemRole,
        List<PermissionSummary> permissions,
        int permissionCount,
        Instant createdAt,
        Instant updatedAt
) {
    public static RoleResponse from(Role role) {
        List<PermissionSummary> permissionSummaries = role.getPermissions().stream()
                .map(p -> new PermissionSummary(p.getId(), p.getCode(), p.getName()))
                .toList();

        return new RoleResponse(
                role.getId(),
                role.getName(),
                role.getCode(),
                role.getDescription(),
                role.isSystemRole(),
                permissionSummaries,
                role.getPermissionCount(),
                role.getCreatedAt(),
                role.getUpdatedAt()
        );
    }

    public record PermissionSummary(UUID id, String code, String name) {}
}
