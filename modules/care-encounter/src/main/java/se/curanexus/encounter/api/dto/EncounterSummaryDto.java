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
        Instant actualStartTime
) {
    public static EncounterSummaryDto from(Encounter encounter) {
        return new EncounterSummaryDto(
                encounter.getId(),
                encounter.getPatientId(),
                encounter.getStatus(),
                encounter.getEncounterClass(),
                encounter.getType(),
                encounter.getServiceType(),
                encounter.getPlannedStartTime(),
                encounter.getActualStartTime()
        );
    }
}
