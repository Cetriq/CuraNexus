package se.curanexus.encounter.api.dto;

import se.curanexus.encounter.domain.*;

import java.time.Instant;
import java.util.UUID;

public record EncounterDto(
        UUID id,
        UUID patientId,
        EncounterStatus status,
        EncounterClass encounterClass,
        EncounterType type,
        EncounterPriority priority,
        String serviceType,
        UUID responsibleUnitId,
        UUID responsiblePractitionerId,
        Instant plannedStartTime,
        Instant plannedEndTime,
        Instant actualStartTime,
        Instant actualEndTime,
        Instant createdAt,
        Instant updatedAt
) {
    public static EncounterDto from(Encounter encounter) {
        return new EncounterDto(
                encounter.getId(),
                encounter.getPatientId(),
                encounter.getStatus(),
                encounter.getEncounterClass(),
                encounter.getType(),
                encounter.getPriority(),
                encounter.getServiceType(),
                encounter.getResponsibleUnitId(),
                encounter.getResponsiblePractitionerId(),
                encounter.getPlannedStartTime(),
                encounter.getPlannedEndTime(),
                encounter.getActualStartTime(),
                encounter.getActualEndTime(),
                encounter.getCreatedAt(),
                encounter.getUpdatedAt()
        );
    }
}
