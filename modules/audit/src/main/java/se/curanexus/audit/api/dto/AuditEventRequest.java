package se.curanexus.audit.api.dto;

import jakarta.validation.constraints.NotNull;
import se.curanexus.audit.domain.AuditEventType;
import se.curanexus.audit.domain.ResourceType;

import java.util.Map;
import java.util.UUID;

public record AuditEventRequest(
        @NotNull(message = "Event type is required")
        AuditEventType eventType,

        @NotNull(message = "User ID is required")
        UUID userId,

        String username,

        @NotNull(message = "Resource type is required")
        ResourceType resourceType,

        UUID resourceId,

        UUID patientId,

        String action,

        Map<String, Object> details,

        String ipAddress,

        String userAgent,

        UUID careRelationId,

        String reason
) {}
