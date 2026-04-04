package se.curanexus.audit.api.dto;

import se.curanexus.audit.domain.AuditAction;
import se.curanexus.audit.domain.ResourceType;
import java.time.Instant;
import java.util.UUID;

public record AuditEventSummaryDto(
    UUID id, Instant timestamp, String userName, String userRole, AuditAction action,
    ResourceType resourceType, UUID resourceId, UUID patientId, boolean emergencyAccess, boolean success
) {}
