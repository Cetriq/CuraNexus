package se.curanexus.consent.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * A patient consent record.
 */
@Entity
@Table(name = "consents")
public class Consent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private ConsentType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ConsentStatus status = ConsentStatus.PENDING;

    @Column(name = "description", length = 1000)
    private String description;

    /** Specific scope (e.g., specific research project, procedure type) */
    @Column(name = "scope", length = 500)
    private String scope;

    /** Unit that requested or manages this consent */
    @Column(name = "managing_unit_id")
    private UUID managingUnitId;

    @Column(name = "managing_unit_name", length = 200)
    private String managingUnitName;

    /** Date consent was given */
    @Column(name = "given_at")
    private Instant givenAt;

    /** Who gave consent (patient or representative) */
    @Column(name = "given_by")
    private UUID givenBy;

    @Column(name = "given_by_name", length = 200)
    private String givenByName;

    /** If given by representative, the relationship */
    @Column(name = "representative_relation", length = 100)
    private String representativeRelation;

    /** How consent was collected */
    @Column(name = "collection_method", length = 50)
    private String collectionMethod;

    /** Valid from date */
    @Column(name = "valid_from")
    private LocalDate validFrom;

    /** Valid until date (null = indefinite) */
    @Column(name = "valid_until")
    private LocalDate validUntil;

    /** Date consent was withdrawn */
    @Column(name = "withdrawn_at")
    private Instant withdrawnAt;

    /** Reason for withdrawal */
    @Column(name = "withdrawal_reason", length = 500)
    private String withdrawalReason;

    /** Practitioner who recorded the consent */
    @Column(name = "recorded_by")
    private UUID recordedBy;

    @Column(name = "recorded_by_name", length = 200)
    private String recordedByName;

    /** Reference to signed document if applicable */
    @Column(name = "document_reference", length = 500)
    private String documentReference;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Consent() {
    }

    public Consent(UUID patientId, ConsentType type) {
        this.patientId = patientId;
        this.type = type;
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

    public void activate() {
        if (this.status == ConsentStatus.PENDING) {
            this.status = ConsentStatus.ACTIVE;
            this.givenAt = Instant.now();
        }
    }

    public void withdraw(String reason) {
        if (this.status == ConsentStatus.ACTIVE) {
            this.status = ConsentStatus.WITHDRAWN;
            this.withdrawnAt = Instant.now();
            this.withdrawalReason = reason;
        }
    }

    public void reject() {
        if (this.status == ConsentStatus.PENDING) {
            this.status = ConsentStatus.REJECTED;
        }
    }

    public void expire() {
        if (this.status == ConsentStatus.ACTIVE) {
            this.status = ConsentStatus.EXPIRED;
        }
    }

    public boolean isValid() {
        if (status != ConsentStatus.ACTIVE) {
            return false;
        }
        LocalDate today = LocalDate.now();
        if (validFrom != null && today.isBefore(validFrom)) {
            return false;
        }
        if (validUntil != null && today.isAfter(validUntil)) {
            return false;
        }
        return true;
    }

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }

    public ConsentType getType() {
        return type;
    }

    public void setType(ConsentType type) {
        this.type = type;
    }

    public ConsentStatus getStatus() {
        return status;
    }

    public void setStatus(ConsentStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public UUID getManagingUnitId() {
        return managingUnitId;
    }

    public void setManagingUnitId(UUID managingUnitId) {
        this.managingUnitId = managingUnitId;
    }

    public String getManagingUnitName() {
        return managingUnitName;
    }

    public void setManagingUnitName(String managingUnitName) {
        this.managingUnitName = managingUnitName;
    }

    public Instant getGivenAt() {
        return givenAt;
    }

    public void setGivenAt(Instant givenAt) {
        this.givenAt = givenAt;
    }

    public UUID getGivenBy() {
        return givenBy;
    }

    public void setGivenBy(UUID givenBy) {
        this.givenBy = givenBy;
    }

    public String getGivenByName() {
        return givenByName;
    }

    public void setGivenByName(String givenByName) {
        this.givenByName = givenByName;
    }

    public String getRepresentativeRelation() {
        return representativeRelation;
    }

    public void setRepresentativeRelation(String representativeRelation) {
        this.representativeRelation = representativeRelation;
    }

    public String getCollectionMethod() {
        return collectionMethod;
    }

    public void setCollectionMethod(String collectionMethod) {
        this.collectionMethod = collectionMethod;
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDate validFrom) {
        this.validFrom = validFrom;
    }

    public LocalDate getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(LocalDate validUntil) {
        this.validUntil = validUntil;
    }

    public Instant getWithdrawnAt() {
        return withdrawnAt;
    }

    public String getWithdrawalReason() {
        return withdrawalReason;
    }

    public UUID getRecordedBy() {
        return recordedBy;
    }

    public void setRecordedBy(UUID recordedBy) {
        this.recordedBy = recordedBy;
    }

    public String getRecordedByName() {
        return recordedByName;
    }

    public void setRecordedByName(String recordedByName) {
        this.recordedByName = recordedByName;
    }

    public String getDocumentReference() {
        return documentReference;
    }

    public void setDocumentReference(String documentReference) {
        this.documentReference = documentReference;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
