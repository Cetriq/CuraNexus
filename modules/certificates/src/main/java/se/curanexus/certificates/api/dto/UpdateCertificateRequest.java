package se.curanexus.certificates.api.dto;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateCertificateRequest(
        String data,
        LocalDate periodStart,
        LocalDate periodEnd,
        @Size(max = 200) String diagnosisCodes,
        @Size(max = 500) String diagnosisDescription
) {
}
