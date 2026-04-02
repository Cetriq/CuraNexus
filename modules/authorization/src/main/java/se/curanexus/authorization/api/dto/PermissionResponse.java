package se.curanexus.authorization.api.dto;

import se.curanexus.authorization.domain.ActionType;
import se.curanexus.authorization.domain.Permission;
import se.curanexus.authorization.domain.ResourceType;

import java.time.Instant;
import java.util.UUID;

public record PermissionResponse(
        UUID id,
        String code,
        String name,
        String description,
        ResourceType resource,
        ActionType action,
        Instant createdAt
) {
    public static PermissionResponse from(Permission permission) {
        return new PermissionResponse(
                permission.getId(),
                permission.getCode(),
                permission.getName(),
                permission.getDescription(),
                permission.getResource(),
                permission.getAction(),
                permission.getCreatedAt()
        );
    }
}
