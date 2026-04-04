package se.curanexus.certificates.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * A generated certificate for a patient.
 */
@Entity
@Table(name = "certificates")
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private CertificateTemplate template;

    @Column(name = "certificate_number", nullable = false, unique = true, length = 50)
    private String certificateNumber;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Column(name = "encounter_id")
    private UUID encounterId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CertificateStatus status = CertificateStatus.DRAFT;

    /** Certificate data as JSON matching template schema */
    @Column(name = "data", columnDefinition = "TEXT", nullable = false)
    private String data;

    /** Period start date (e.g., sick leave start) */
    @Column(name = "period_start")
    private LocalDate periodStart;

    /** Period end date (e.g., sick leave end) */
    @Column(name = "period_end")
    private LocalDate periodEnd;

    /** Diagnosis codes (ICD-10), comma-separated */
    @Column(name = "diagnosis_codes", length = 200)
    private String diagnosisCodes;

    /** Main diagnosis description */
    @Column(name = "diagnosis_description", length = 500)
    private String diagnosisDescription;

    /** Issuing practitioner ID */
    @Column(name = "issuer_id", nullable = false)
    private UUID issuerId;

    /** Issuer name for display */
    @Column(name = "issuer_name", length = 200)
    private String issuerName;

    /** Issuer role (e.g., DOCTOR, NURSE) */
    @Column(name = "issuer_role", length = 50)
    private String issuerRole;

    /** Issuer unit */
    @Column(name = "issuer_unit_id")
    private UUID issuerUnitId;

    @Column(name = "issuer_unit_name", length = 200)
    private String issuerUnitName;

    /** Signed timestamp */
    @Column(name = "signed_at")
    private Instant signedAt;

    /** Signature data (e.g., BankID signature) */
    @Column(name = "signature", columnDefinition = "TEXT")
    private String signature;

    /** Sent to recipient timestamp */
    @Column(name = "sent_at")
    private Instant sentAt;

    /** Recipient confirmation/tracking ID */
    @Column(name = "recipient_tracking_id", length = 100)
    private String recipientTrackingId;

    /** Revocation reason */
    @Column(name = "revocation_reason", length = 500)
    private String revocationReason;

    /** Revoked timestamp */
    @Column(name = "revoked_at")
    private Instant revokedAt;

    /** Replacing certificate ID */
    @Column(name = "replaces_id")
    private UUID replacesId;

    /** Replaced by certificate ID */
    @Column(name = "replaced_by_id")
    private UUID replacedById;

    /** Rendered PDF as base64 or file reference */
    @Column(name = "rendered_pdf_ref", length = 500)
    private String renderedPdfRef;

    /** Valid until date */
    @Column(name = "valid_until")
    private LocalDate validUntil;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Certificate() {
    }

    public Certificate(CertificateTemplate template, UUID patientId, UUID issuerId) {
        this.template = template;
        this.patientId = patientId;
        this.issuerId = issuerId;
        this.certificateNumber = generateCertificateNumber();
        this.data = "{}";
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    private String generateCertificateNumber() {
        return "CERT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public void sign(String signature) {
        if (this.status == CertificateStatus.DRAFT) {
            this.signature = signature;
            this.signedAt = Instant.now();
            this.status = CertificateStatus.SIGNED;

            // Calculate valid until if template has validity days
            if (template.getValidityDays() != null) {
                this.validUntil = LocalDate.now().plusDays(template.getValidityDays());
            }
        }
    }

    public void markSent(String trackingId) {
        if (this.status == CertificateStatus.SIGNED) {
            this.recipientTrackingId = trackingId;
            this.sentAt = Instant.now();
            this.status = CertificateStatus.SENT;
        }
    }

    public void revoke(String reason) {
        if (this.status != CertificateStatus.REVOKED && this.status != CertificateStatus.REPLACED) {
            this.revocationReason = reason;
            this.revokedAt = Instant.now();
            this.status = CertificateStatus.REVOKED;
        }
    }

    public void replaceWith(UUID newCertificateId) {
        if (this.status != CertificateStatus.REVOKED) {
            this.replacedById = newCertificateId;
            this.status = CertificateStatus.REPLACED;
        }
    }

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public CertificateTemplate getTemplate() {
        return template;
    }

    public void setTemplate(CertificateTemplate template) {
        this.template = template;
    }

    public String getCertificateNumber() {
        return certificateNumber;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }

    public UUID getEncounterId() {
        return encounterId;
    }

    public void setEncounterId(UUID encounterId) {
        this.encounterId = encounterId;
    }

    public CertificateStatus getStatus() {
        return status;
    }

    public void setStatus(CertificateStatus status) {
        this.status = status;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(LocalDate periodStart) {
        this.periodStart = periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(LocalDate periodEnd) {
        this.periodEnd = periodEnd;
    }

    public String getDiagnosisCodes() {
        return diagnosisCodes;
    }

    public void setDiagnosisCodes(String diagnosisCodes) {
        this.diagnosisCodes = diagnosisCodes;
    }

    public String getDiagnosisDescription() {
        return diagnosisDescription;
    }

    public void setDiagnosisDescription(String diagnosisDescription) {
        this.diagnosisDescription = diagnosisDescription;
    }

    public UUID getIssuerId() {
        return issuerId;
    }

    public void setIssuerId(UUID issuerId) {
        this.issuerId = issuerId;
    }

    public String getIssuerName() {
        return issuerName;
    }

    public void setIssuerName(String issuerName) {
        this.issuerName = issuerName;
    }

    public String getIssuerRole() {
        return issuerRole;
    }

    public void setIssuerRole(String issuerRole) {
        this.issuerRole = issuerRole;
    }

    public UUID getIssuerUnitId() {
        return issuerUnitId;
    }

    public void setIssuerUnitId(UUID issuerUnitId) {
        this.issuerUnitId = issuerUnitId;
    }

    public String getIssuerUnitName() {
        return issuerUnitName;
    }

    public void setIssuerUnitName(String issuerUnitName) {
        this.issuerUnitName = issuerUnitName;
    }

    public Instant getSignedAt() {
        return signedAt;
    }

    public String getSignature() {
        return signature;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public String getRecipientTrackingId() {
        return recipientTrackingId;
    }

    public String getRevocationReason() {
        return revocationReason;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public UUID getReplacesId() {
        return replacesId;
    }

    public void setReplacesId(UUID replacesId) {
        this.replacesId = replacesId;
    }

    public UUID getReplacedById() {
        return replacedById;
    }

    public String getRenderedPdfRef() {
        return renderedPdfRef;
    }

    public void setRenderedPdfRef(String renderedPdfRef) {
        this.renderedPdfRef = renderedPdfRef;
    }

    public LocalDate getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(LocalDate validUntil) {
        this.validUntil = validUntil;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
