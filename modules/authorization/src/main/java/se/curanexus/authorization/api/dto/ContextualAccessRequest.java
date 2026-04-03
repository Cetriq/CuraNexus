package se.curanexus.authorization.api.dto;

import jakarta.validation.constraints.NotNull;
import se.curanexus.authorization.domain.ActionType;
import se.curanexus.authorization.domain.ResourceType;

import java.util.Map;
import java.util.UUID;

/**
 * Request for contextual (ABAC) access check.
 */
public record ContextualAccessRequest(
        @NotNull UUID userId,
        UUID patientId,
        UUID encounterId,
        ResourceType resourceType,
        UUID resourceId,
        ActionType action,
        String userType,
        String department,
        String unit,
        boolean emergencyAccess,
        String accessReason,
        String clientIp,
        String clientApplication,
        Map<String, Object> additionalAttributes
) {}
