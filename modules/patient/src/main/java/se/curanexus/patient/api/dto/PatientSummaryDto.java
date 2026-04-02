package se.curanexus.patient.api.dto;

import se.curanexus.patient.domain.Patient;

import java.time.LocalDate;
import java.util.UUID;

public record PatientSummaryDto(
        UUID id,
        String personalIdentityNumber,
        String givenName,
        String familyName,
        LocalDate dateOfBirth,
        boolean protectedIdentity
) {
    public static PatientSummaryDto from(Patient patient) {
        String maskedPnr = patient.isProtectedIdentity()
                ? "************"
                : patient.getPersonalIdentityNumber();

        return new PatientSummaryDto(
                patient.getId(),
                maskedPnr,
                patient.getGivenName(),
                patient.getFamilyName(),
                patient.getDateOfBirth(),
                patient.isProtectedIdentity()
        );
    }
}
