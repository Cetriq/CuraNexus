package se.curanexus.patient.api.dto;

import jakarta.validation.constraints.Size;
import se.curanexus.patient.domain.ContactUse;

import java.time.LocalDate;

public record UpdateContactRequest(
        @Size(max = 255, message = "Value must not exceed 255 characters")
        String value,

        ContactUse use,
        Boolean primary,
        LocalDate validFrom,
        LocalDate validTo
) {
}
