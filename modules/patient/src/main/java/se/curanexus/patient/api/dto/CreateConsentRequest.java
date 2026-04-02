package se.curanexus.patient.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import se.curanexus.patient.domain.ConsentType;

import java.time.LocalDate;

public record CreateConsentRequest(
        @NotNull(message = "Consent type is required")
        ConsentType type,

        String givenBy,
        LocalDate validFrom,
        LocalDate validTo,

        @Size(max = 500, message = "Scope must not exceed 500 characters")
        String scope
) {
}
