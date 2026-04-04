package se.curanexus.audit.api.dto;

import se.curanexus.audit.domain.DataChangeLog.ChangeType;
import se.curanexus.audit.domain.ResourceType;
import java.time.Instant;
import java.util.UUID;

public record DataChangeLogDto(
    UUID id, UUID auditEventId, Instant timestamp, ResourceType resourceType, UUID resourceId,
    String fieldName, String oldValue, String newValue, ChangeType changeType
) {}
