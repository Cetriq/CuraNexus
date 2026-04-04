package se.curanexus.certificates.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record CreateCertificateRequest(
        @NotBlank String templateCode,
        @NotNull UUID patientId,
        UUID encounterId,
        @NotNull UUID issuerId,
        @Size(max = 200) String issuerName,
        @Size(max = 50) String issuerRole,
        UUID issuerUnitId,
        @Size(max = 200) String issuerUnitName,
        String data,
        LocalDate periodStart,
        LocalDate periodEnd,
        @Size(max = 200) String diagnosisCodes,
        @Size(max = 500) String diagnosisDescription,
        UUID replacesId
) {
}
