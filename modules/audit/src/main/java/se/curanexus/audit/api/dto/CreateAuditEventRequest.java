package se.curanexus.audit.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import se.curanexus.audit.domain.AuditAction;
import se.curanexus.audit.domain.ResourceType;
import java.util.UUID;

public record CreateAuditEventRequest(
    @NotBlank String userId, String userHsaId, String userName, String userRole,
    @NotNull AuditAction action, @NotNull ResourceType resourceType, UUID resourceId, String resourceDescription,
    UUID patientId, String patientPersonnummer, UUID careUnitId, String careUnitName, String careUnitHsaId,
    String ipAddress, String userAgent, String sessionId, UUID encounterId, String accessReason,
    Boolean emergencyAccess, UUID consentReference, Boolean success, String errorMessage,
    String details, String sourceSystem, String correlationId
) {}
