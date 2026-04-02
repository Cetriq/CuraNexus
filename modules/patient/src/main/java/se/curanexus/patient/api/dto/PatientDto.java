package se.curanexus.patient.api.dto;

import se.curanexus.patient.domain.Gender;
import se.curanexus.patient.domain.Patient;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PatientDto(
        UUID id,
        String personalIdentityNumber,
        String givenName,
        String familyName,
        String middleName,
        LocalDate dateOfBirth,
        Gender gender,
        boolean protectedIdentity,
        boolean deceased,
        LocalDate deceasedDate,
        Instant createdAt,
        Instant updatedAt
) {
    public static PatientDto from(Patient patient) {
        return new PatientDto(
                patient.getId(),
                patient.getPersonalIdentityNumber(),
                patient.getGivenName(),
                patient.getFamilyName(),
                patient.getMiddleName(),
                patient.getDateOfBirth(),
                patient.getGender(),
                patient.isProtectedIdentity(),
                patient.isDeceased(),
                patient.getDeceasedDate(),
                patient.getCreatedAt(),
                patient.getUpdatedAt()
        );
    }
}
