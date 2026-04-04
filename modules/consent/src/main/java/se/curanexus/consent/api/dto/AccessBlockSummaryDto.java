package se.curanexus.consent.api.dto;

import se.curanexus.consent.domain.AccessBlock;
import se.curanexus.consent.domain.AccessBlockType;

import java.time.LocalDate;
import java.util.UUID;

public record AccessBlockSummaryDto(
        UUID id,
        UUID patientId,
        AccessBlockType blockType,
        String blockedEntityName,
        String reason,
        LocalDate validFrom,
        LocalDate validUntil,
        boolean currentlyActive
) {
    public static AccessBlockSummaryDto fromEntity(AccessBlock block) {
        String blockedEntityName = switch (block.getBlockType()) {
            case UNIT -> block.getBlockedUnitName();
            case PRACTITIONER -> block.getBlockedPractitionerName();
            case DATA_CATEGORY -> block.getBlockedDataCategory();
            case EXTERNAL_UNITS -> "All external units";
            case EMERGENCY_OVERRIDE -> "Emergency override";
        };

        return new AccessBlockSummaryDto(
                block.getId(),
                block.getPatientId(),
                block.getBlockType(),
                blockedEntityName,
                block.getReason(),
                block.getValidFrom(),
                block.getValidUntil(),
                block.isCurrentlyActive()
        );
    }
}
