package se.curanexus.encounter.api.dto;

import jakarta.validation.constraints.Size;
import se.curanexus.encounter.domain.EncounterPriority;
import se.curanexus.encounter.domain.EncounterType;

import java.time.Instant;
import java.util.UUID;

public record UpdateEncounterRequest(
        EncounterType type,
        EncounterPriority priority,

        @Size(max = 100, message = "Service type must not exceed 100 characters")
        String serviceType,

        UUID responsibleUnitId,
        UUID responsiblePractitionerId,
        Instant plannedStartTime,
        Instant plannedEndTime,
        Instant actualStartTime,
        Instant actualEndTime
) {
}
