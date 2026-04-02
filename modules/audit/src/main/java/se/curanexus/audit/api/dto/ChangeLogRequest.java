package se.curanexus.audit.api.dto;

import jakarta.validation.constraints.NotNull;
import se.curanexus.audit.domain.ChangeType;
import se.curanexus.audit.domain.ResourceType;

import java.util.UUID;

public record ChangeLogRequest(
        @NotNull(message = "User ID is required")
        UUID userId,

        String username,

        @NotNull(message = "Resource type is required")
        ResourceType resourceType,

        @NotNull(message = "Resource ID is required")
        UUID resourceId,

        UUID patientId,

        @NotNull(message = "Change type is required")
        ChangeType changeType,

        String fieldName,

        String oldValue,

        String newValue
) {}
