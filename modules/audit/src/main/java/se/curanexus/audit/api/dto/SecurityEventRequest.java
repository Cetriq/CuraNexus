package se.curanexus.audit.api.dto;

import jakarta.validation.constraints.NotNull;
import se.curanexus.audit.domain.SecurityEventType;

import java.util.Map;
import java.util.UUID;

public record SecurityEventRequest(
        UUID userId,

        String username,

        @NotNull(message = "Event type is required")
        SecurityEventType eventType,

        @NotNull(message = "Success status is required")
        Boolean success,

        String ipAddress,

        String userAgent,

        Map<String, Object> details
) {}
