package se.curanexus.audit.api.dto;

import se.curanexus.audit.domain.AuditEvent;
import se.curanexus.audit.domain.AuditEventType;
import se.curanexus.audit.domain.ResourceType;

import java.time.Instant;
import java.util.UUID;

public record AuditEventResponse(
        UUID id,
        AuditEventType eventType,
        UUID userId,
        String username,
        ResourceType resourceType,
        UUID resourceId,
        UUID patientId,
        String action,
        String details,
        String ipAddress,
        String userAgent,
        UUID careRelationId,
        String reason,
        Instant timestamp
) {
    public static AuditEventResponse fromEntity(AuditEvent event) {
        return new AuditEventResponse(
                event.getId(),
                event.getEventType(),
                event.getUserId(),
                event.getUsername(),
                event.getResourceType(),
                event.getResourceId(),
                event.getPatientId(),
                event.getAction(),
                event.getDetails(),
                event.getIpAddress(),
                event.getUserAgent(),
                event.getCareRelationId(),
                event.getReason(),
                event.getTimestamp()
        );
    }
}
