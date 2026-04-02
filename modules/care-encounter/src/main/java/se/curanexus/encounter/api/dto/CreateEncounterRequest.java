package se.curanexus.encounter.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import se.curanexus.encounter.domain.EncounterClass;
import se.curanexus.encounter.domain.EncounterPriority;
import se.curanexus.encounter.domain.EncounterType;

import java.time.Instant;
import java.util.UUID;

public record CreateEncounterRequest(
        @NotNull(message = "Patient ID is required")
        UUID patientId,

        @NotNull(message = "Encounter class is required")
        EncounterClass encounterClass,

        EncounterType type,
        EncounterPriority priority,

        @Size(max = 100, message = "Service type must not exceed 100 characters")
        String serviceType,

        UUID responsibleUnitId,
        UUID responsiblePractitionerId,
        Instant plannedStartTime,
        Instant plannedEndTime
) {
}
