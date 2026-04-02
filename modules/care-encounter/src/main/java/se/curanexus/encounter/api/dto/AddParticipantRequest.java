package se.curanexus.encounter.api.dto;

import jakarta.validation.constraints.NotNull;
import se.curanexus.encounter.domain.ParticipantRole;
import se.curanexus.encounter.domain.ParticipantType;

import java.time.Instant;
import java.util.UUID;

public record AddParticipantRequest(
        @NotNull(message = "Participant type is required")
        ParticipantType type,

        UUID practitionerId,
        ParticipantRole role,
        Instant periodStart,
        Instant periodEnd
) {
}
