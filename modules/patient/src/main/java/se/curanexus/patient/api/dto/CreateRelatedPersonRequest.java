package se.curanexus.patient.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import se.curanexus.patient.domain.RelationshipType;

import java.time.LocalDate;

public record CreateRelatedPersonRequest(
        @NotNull(message = "Relationship is required")
        RelationshipType relationship,

        @Pattern(regexp = "^\\d{12}$", message = "Personal identity number must be 12 digits")
        String personalIdentityNumber,

        @NotBlank(message = "Given name is required")
        @Size(max = 100, message = "Given name must not exceed 100 characters")
        String givenName,

        @NotBlank(message = "Family name is required")
        @Size(max = 100, message = "Family name must not exceed 100 characters")
        String familyName,

        @Size(max = 20, message = "Phone must not exceed 20 characters")
        String phone,

        @Email(message = "Email must be valid")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        String email,

        Boolean isEmergencyContact,
        Boolean isLegalGuardian,
        LocalDate validFrom,
        LocalDate validTo
) {
}
