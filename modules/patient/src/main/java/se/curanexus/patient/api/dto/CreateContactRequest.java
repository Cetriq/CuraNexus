package se.curanexus.patient.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import se.curanexus.patient.domain.ContactType;
import se.curanexus.patient.domain.ContactUse;

import java.time.LocalDate;

public record CreateContactRequest(
        @NotNull(message = "Contact type is required")
        ContactType type,

        @NotBlank(message = "Value is required")
        @Size(max = 255, message = "Value must not exceed 255 characters")
        String value,

        ContactUse use,
        Boolean primary,
        LocalDate validFrom,
        LocalDate validTo
) {
}
