package se.curanexus.patient.api.dto;

import se.curanexus.patient.domain.Consent;
import se.curanexus.patient.domain.ConsentStatus;
import se.curanexus.patient.domain.ConsentType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ConsentDto(
        UUID id,
        ConsentType type,
        ConsentStatus status,
        Instant givenAt,
        String givenBy,
        Instant revokedAt,
        LocalDate validFrom,
        LocalDate validTo,
        String scope
) {
    public static ConsentDto from(Consent consent) {
        return new ConsentDto(
                consent.getId(),
                consent.getType(),
                consent.getStatus(),
                consent.getGivenAt(),
                consent.getGivenBy(),
                consent.getRevokedAt(),
                consent.getValidFrom(),
                consent.getValidTo(),
                consent.getScope()
        );
    }
}
