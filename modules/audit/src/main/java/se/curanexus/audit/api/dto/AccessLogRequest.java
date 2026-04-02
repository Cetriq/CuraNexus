package se.curanexus.audit.api.dto;

import jakarta.validation.constraints.NotNull;
import se.curanexus.audit.domain.AccessType;
import se.curanexus.audit.domain.ResourceType;

import java.util.UUID;

public record AccessLogRequest(
        @NotNull(message = "User ID is required")
        UUID userId,

        String username,

        @NotNull(message = "Patient ID is required")
        UUID patientId,

        @NotNull(message = "Resource type is required")
        ResourceType resourceType,

        UUID resourceId,

        @NotNull(message = "Access type is required")
        AccessType accessType,

        UUID careRelationId,

        String careRelationType,

        String reason,

        String ipAddress
) {}
