package se.curanexus.audit.api.dto;

import se.curanexus.audit.domain.AccessLog;
import se.curanexus.audit.domain.AccessType;
import se.curanexus.audit.domain.ResourceType;

import java.time.Instant;
import java.util.UUID;

public record AccessLogResponse(
        UUID id,
        UUID userId,
        String username,
        UUID patientId,
        ResourceType resourceType,
        UUID resourceId,
        AccessType accessType,
        UUID careRelationId,
        String careRelationType,
        String reason,
        String ipAddress,
        Instant timestamp
) {
    public static AccessLogResponse fromEntity(AccessLog log) {
        return new AccessLogResponse(
                log.getId(),
                log.getUserId(),
                log.getUsername(),
                log.getPatientId(),
                log.getResourceType(),
                log.getResourceId(),
                log.getAccessType(),
                log.getCareRelationId(),
                log.getCareRelationType(),
                log.getReason(),
                log.getIpAddress(),
                log.getTimestamp()
        );
    }
}
