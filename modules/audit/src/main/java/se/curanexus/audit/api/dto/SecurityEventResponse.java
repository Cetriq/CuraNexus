package se.curanexus.audit.api.dto;

import se.curanexus.audit.domain.SecurityEvent;
import se.curanexus.audit.domain.SecurityEventType;

import java.time.Instant;
import java.util.UUID;

public record SecurityEventResponse(
        UUID id,
        UUID userId,
        String username,
        SecurityEventType eventType,
        boolean success,
        String ipAddress,
        String userAgent,
        String details,
        Instant timestamp
) {
    public static SecurityEventResponse fromEntity(SecurityEvent event) {
        return new SecurityEventResponse(
                event.getId(),
                event.getUserId(),
                event.getUsername(),
                event.getEventType(),
                event.isSuccess(),
                event.getIpAddress(),
                event.getUserAgent(),
                event.getDetails(),
                event.getTimestamp()
        );
    }
}
