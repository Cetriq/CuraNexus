package se.curanexus.encounter.api.dto;

import se.curanexus.encounter.domain.Participant;
import se.curanexus.encounter.domain.ParticipantRole;
import se.curanexus.encounter.domain.ParticipantType;

import java.time.Instant;
import java.util.UUID;

public record ParticipantDto(
        UUID id,
        ParticipantType type,
        UUID practitionerId,
        ParticipantRole role,
        PeriodDto period
) {
    public static ParticipantDto from(Participant participant) {
        PeriodDto period = null;
        if (participant.getPeriodStart() != null || participant.getPeriodEnd() != null) {
            period = new PeriodDto(participant.getPeriodStart(), participant.getPeriodEnd());
        }
        return new ParticipantDto(
                participant.getId(),
                participant.getType(),
                participant.getPractitionerId(),
                participant.getRole(),
                period
        );
    }
}
