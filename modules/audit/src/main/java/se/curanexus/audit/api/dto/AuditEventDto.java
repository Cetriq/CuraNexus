package se.curanexus.audit.api.dto;

import se.curanexus.audit.domain.AuditAction;
import se.curanexus.audit.domain.ResourceType;
import java.time.Instant;
import java.util.UUID;

public record AuditEventDto(
    UUID id, Instant timestamp, String userId, String userHsaId, String userName, String userRole,
    AuditAction action, ResourceType resourceType, UUID resourceId, String resourceDescription,
    UUID patientId, UUID careUnitId, String careUnitName, String careUnitHsaId, String ipAddress,
    UUID encounterId, String accessReason, boolean emergencyAccess, boolean success, String errorMessage,
    String sourceSystem, String correlationId
) {}
