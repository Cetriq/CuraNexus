package se.curanexus.encounter.api.dto;

import se.curanexus.encounter.domain.ParticipantRole;

import java.time.Instant;

public record UpdateParticipantRequest(
        ParticipantRole role,
        Instant periodStart,
        Instant periodEnd
) {
}
