package se.curanexus.consent.api.dto;

import se.curanexus.consent.domain.AccessBlock;
import se.curanexus.consent.domain.AccessBlockType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AccessBlockDto(
        UUID id,
        UUID patientId,
        AccessBlockType blockType,
        UUID blockedUnitId,
        String blockedUnitName,
        UUID blockedPractitionerId,
        String blockedPractitionerName,
        String blockedDataCategory,
        String reason,
        boolean active,
        LocalDate validFrom,
        LocalDate validUntil,
        UUID requestedBy,
        String requestedByName,
        Instant requestedAt,
        Instant deactivatedAt,
        UUID deactivatedBy,
        String deactivationReason,
        boolean currentlyActive,
        Instant createdAt,
        Instant updatedAt
) {
    public static AccessBlockDto fromEntity(AccessBlock block) {
        return new AccessBlockDto(
                block.getId(),
                block.getPatientId(),
                block.getBlockType(),
                block.getBlockedUnitId(),
                block.getBlockedUnitName(),
                block.getBlockedPractitionerId(),
                block.getBlockedPractitionerName(),
                block.getBlockedDataCategory(),
                block.getReason(),
                block.isActive(),
                block.getValidFrom(),
                block.getValidUntil(),
                block.getRequestedBy(),
                block.getRequestedByName(),
                block.getRequestedAt(),
                block.getDeactivatedAt(),
                block.getDeactivatedBy(),
                block.getDeactivationReason(),
                block.isCurrentlyActive(),
                block.getCreatedAt(),
                block.getUpdatedAt()
        );
    }
}
