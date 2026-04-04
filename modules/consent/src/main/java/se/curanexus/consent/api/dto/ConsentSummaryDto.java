package se.curanexus.consent.api.dto;

import se.curanexus.consent.domain.Consent;
import se.curanexus.consent.domain.ConsentStatus;
import se.curanexus.consent.domain.ConsentType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ConsentSummaryDto(
        UUID id,
        UUID patientId,
        ConsentType type,
        ConsentStatus status,
        String description,
        LocalDate validFrom,
        LocalDate validUntil,
        Instant givenAt,
        boolean valid
) {
    public static ConsentSummaryDto fromEntity(Consent consent) {
        return new ConsentSummaryDto(
                consent.getId(),
                consent.getPatientId(),
                consent.getType(),
                consent.getStatus(),
                consent.getDescription(),
                consent.getValidFrom(),
                consent.getValidUntil(),
                consent.getGivenAt(),
                consent.isValid()
        );
    }
}
