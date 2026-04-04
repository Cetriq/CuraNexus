package se.curanexus.certificates.api.dto;

import se.curanexus.certificates.domain.Certificate;
import se.curanexus.certificates.domain.CertificateStatus;
import se.curanexus.certificates.domain.CertificateType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record CertificateDto(
        UUID id,
        String certificateNumber,
        String templateCode,
        String templateName,
        CertificateType type,
        UUID patientId,
        UUID encounterId,
        CertificateStatus status,
        String data,
        LocalDate periodStart,
        LocalDate periodEnd,
        String diagnosisCodes,
        String diagnosisDescription,
        UUID issuerId,
        String issuerName,
        String issuerRole,
        UUID issuerUnitId,
        String issuerUnitName,
        Instant signedAt,
        Instant sentAt,
        String recipientTrackingId,
        String revocationReason,
        Instant revokedAt,
        UUID replacesId,
        UUID replacedById,
        LocalDate validUntil,
        Instant createdAt,
        Instant updatedAt
) {
    public static CertificateDto from(Certificate certificate) {
        return new CertificateDto(
                certificate.getId(),
                certificate.getCertificateNumber(),
                certificate.getTemplate().getCode(),
                certificate.getTemplate().getName(),
                certificate.getTemplate().getType(),
                certificate.getPatientId(),
                certificate.getEncounterId(),
                certificate.getStatus(),
                certificate.getData(),
                certificate.getPeriodStart(),
                certificate.getPeriodEnd(),
                certificate.getDiagnosisCodes(),
                certificate.getDiagnosisDescription(),
                certificate.getIssuerId(),
                certificate.getIssuerName(),
                certificate.getIssuerRole(),
                certificate.getIssuerUnitId(),
                certificate.getIssuerUnitName(),
                certificate.getSignedAt(),
                certificate.getSentAt(),
                certificate.getRecipientTrackingId(),
                certificate.getRevocationReason(),
                certificate.getRevokedAt(),
                certificate.getReplacesId(),
                certificate.getReplacedById(),
                certificate.getValidUntil(),
                certificate.getCreatedAt(),
                certificate.getUpdatedAt()
        );
    }
}
