package se.curanexus.consent.api.dto;

import jakarta.validation.constraints.NotNull;
import se.curanexus.consent.domain.ConsentType;

import java.time.LocalDate;
import java.util.UUID;

public record CreateConsentRequest(
        @NotNull(message = "Patient ID is required")
        UUID patientId,

        @NotNull(message = "Consent type is required")
        ConsentType type,

        String description,

        String scope,

        UUID managingUnitId,

        String managingUnitName,

        UUID givenBy,

        String givenByName,

        String representativeRelation,

        String collectionMethod,

        LocalDate validFrom,

        LocalDate validUntil,

        UUID recordedBy,

        String recordedByName,

        String documentReference
) {}
