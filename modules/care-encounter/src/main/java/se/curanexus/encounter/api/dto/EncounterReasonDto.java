package se.curanexus.encounter.api.dto;

import se.curanexus.encounter.domain.EncounterReason;
import se.curanexus.encounter.domain.ReasonType;

import java.util.UUID;

public record EncounterReasonDto(
        UUID id,
        ReasonType type,
        String code,
        String codeSystem,
        String displayText,
        boolean isPrimary
) {
    public static EncounterReasonDto from(EncounterReason reason) {
        return new EncounterReasonDto(
                reason.getId(),
                reason.getType(),
                reason.getCode(),
                reason.getCodeSystem(),
                reason.getDisplayText(),
                reason.isPrimary()
        );
    }
}
