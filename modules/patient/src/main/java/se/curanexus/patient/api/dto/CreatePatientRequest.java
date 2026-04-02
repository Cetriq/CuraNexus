package se.curanexus.patient.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreatePatientRequest(
        @NotBlank(message = "Personal identity number is required")
        @Pattern(regexp = "^\\d{12}$", message = "Personal identity number must be 12 digits")
        String personalIdentityNumber,

        @NotBlank(message = "Given name is required")
        @Size(max = 100, message = "Given name must not exceed 100 characters")
        String givenName,

        @NotBlank(message = "Family name is required")
        @Size(max = 100, message = "Family name must not exceed 100 characters")
        String familyName,

        @Size(max = 100, message = "Middle name must not exceed 100 characters")
        String middleName,

        Boolean protectedIdentity
) {
}
