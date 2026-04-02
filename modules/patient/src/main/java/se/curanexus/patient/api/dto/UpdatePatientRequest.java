package se.curanexus.patient.api.dto;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdatePatientRequest(
        @Size(max = 100, message = "Given name must not exceed 100 characters")
        String givenName,

        @Size(max = 100, message = "Family name must not exceed 100 characters")
        String familyName,

        @Size(max = 100, message = "Middle name must not exceed 100 characters")
        String middleName,

        Boolean protectedIdentity,
        Boolean deceased,
        LocalDate deceasedDate
) {
}
