package se.curanexus.certificates.api.dto;

import se.curanexus.certificates.domain.Certificate;
import se.curanexus.certificates.domain.CertificateStatus;
import se.curanexus.certificates.domain.CertificateType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record CertificateSummaryDto(
        UUID id,
        String certificateNumber,
        String templateCode,
        String templateName,
        CertificateType type,
        UUID patientId,
        CertificateStatus status,
        LocalDate periodStart,
        LocalDate periodEnd,
        String diagnosisDescription,
        String issuerName,
        Instant signedAt,
        Instant sentAt,
        LocalDate validUntil,
        Instant createdAt
) {
    public static CertificateSummaryDto from(Certificate certificate) {
        return new CertificateSummaryDto(
                certificate.getId(),
                certificate.getCertificateNumber(),
                certificate.getTemplate().getCode(),
                certificate.getTemplate().getName(),
                certificate.getTemplate().getType(),
                certificate.getPatientId(),
                certificate.getStatus(),
                certificate.getPeriodStart(),
                certificate.getPeriodEnd(),
                certificate.getDiagnosisDescription(),
                certificate.getIssuerName(),
                certificate.getSignedAt(),
                certificate.getSentAt(),
                certificate.getValidUntil(),
                certificate.getCreatedAt()
        );
    }
}
