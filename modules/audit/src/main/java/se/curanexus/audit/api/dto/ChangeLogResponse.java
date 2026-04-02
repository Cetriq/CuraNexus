package se.curanexus.audit.api.dto;

import se.curanexus.audit.domain.ChangeLog;
import se.curanexus.audit.domain.ChangeType;
import se.curanexus.audit.domain.ResourceType;

import java.time.Instant;
import java.util.UUID;

public record ChangeLogResponse(
        UUID id,
        UUID userId,
        String username,
        ResourceType resourceType,
        UUID resourceId,
        UUID patientId,
        ChangeType changeType,
        String fieldName,
        String oldValue,
        String newValue,
        Instant timestamp
) {
    public static ChangeLogResponse fromEntity(ChangeLog log) {
        return new ChangeLogResponse(
                log.getId(),
                log.getUserId(),
                log.getUsername(),
                log.getResourceType(),
                log.getResourceId(),
                log.getPatientId(),
                log.getChangeType(),
                log.getFieldName(),
                log.getOldValue(),
                log.getNewValue(),
                log.getTimestamp()
        );
    }
}
