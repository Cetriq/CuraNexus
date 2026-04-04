package se.curanexus.encounter.api.dto;

import se.curanexus.encounter.domain.*;

import java.time.Instant;
import java.util.UUID;

public record EncounterSummaryDto(
        UUID id,
        UUID patientId,
        EncounterStatus status,
        EncounterClass encounterClass,
        EncounterType type,
        String serviceType,
        Instant plannedStartTime,
        Instant actualStartTime,
        String chiefComplaint
) {
    public static EncounterSummaryDto from(Encounter encounter) {
        // Extract primary chief complaint for convenience
        String chiefComplaint = encounter.getReasons().stream()
                .filter(r -> r.getType() == ReasonType.CHIEF_COMPLAINT && r.isPrimary())
                .findFirst()
                .map(EncounterReason::getDisplayText)
                .orElse(null);

        return new EncounterSummaryDto(
                encounter.getId(),
                encounter.getPatientId(),
                encounter.getStatus(),
                encounter.getEncounterClass(),
                encounter.getType(),
                encounter.getServiceType(),
                encounter.getPlannedStartTime(),
                encounter.getActualStartTime(),
                chiefComplaint
        );
    }
}
