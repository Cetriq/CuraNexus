package se.curanexus.authorization.api.dto;

import se.curanexus.authorization.service.AuthorizationService.AuthorizationContext;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public record AuthorizationContextResponse(
        UUID userId,
        String username,
        Set<String> roles,
        Set<String> permissions,
        List<UUID> accessiblePatients,
        boolean active
) {
    public static AuthorizationContextResponse from(AuthorizationContext context) {
        return new AuthorizationContextResponse(
                context.userId(),
                context.username(),
                context.roles(),
                context.permissions(),
                context.accessiblePatients(),
                context.active()
        );
    }
}
