package se.curanexus.audit.api.dto;

import se.curanexus.audit.domain.AuditAction;
import se.curanexus.audit.domain.ResourceType;
import java.time.Instant;
import java.util.UUID;

public record AuditSearchRequest(
    UUID patientId, String userId, UUID careUnitId, ResourceType resourceType,
    AuditAction action, Instant from, Instant to
) {}
