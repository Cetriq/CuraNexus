package se.curanexus.encounter.api.dto;

import se.curanexus.encounter.domain.*;

import java.time.Instant;
import java.util.List;
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
        Instant updatedAt,
        List<EncounterReasonDto> reasons,
        String chiefComplaint
) {
    public static EncounterDto from(Encounter encounter) {
        List<EncounterReasonDto> reasonDtos = encounter.getReasons().stream()
                .map(EncounterReasonDto::from)
                .toList();

        // Extract primary chief complaint for convenience
        String chiefComplaint = encounter.getReasons().stream()
                .filter(r -> r.getType() == ReasonType.CHIEF_COMPLAINT && r.isPrimary())
                .findFirst()
                .map(EncounterReason::getDisplayText)
                .orElse(null);

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
                encounter.getUpdatedAt(),
                reasonDtos,
                chiefComplaint
        );
    }
}
