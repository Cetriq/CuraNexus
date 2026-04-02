package se.curanexus.patient.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "consents")
public class Consent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private ConsentType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ConsentStatus status = ConsentStatus.ACTIVE;

    @Column(name = "given_at", nullable = false)
    private Instant givenAt;

    @Column(name = "given_by")
    private String givenBy;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "valid_from")
    private LocalDate validFrom;

    @Column(name = "valid_to")
    private LocalDate validTo;

    @Column(name = "scope", length = 500)
    private String scope;

    protected Consent() {
    }

    public Consent(ConsentType type) {
        this.type = type;
        this.givenAt = Instant.now();
        this.status = ConsentStatus.ACTIVE;
    }

    public void revoke() {
        this.status = ConsentStatus.REVOKED;
        this.revokedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
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

    public Instant getGivenAt() {
        return givenAt;
    }

    public void setGivenAt(Instant givenAt) {
        this.givenAt = givenAt;
    }

    public String getGivenBy() {
        return givenBy;
    }

    public void setGivenBy(String givenBy) {
        this.givenBy = givenBy;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDate validFrom) {
        this.validFrom = validFrom;
    }

    public LocalDate getValidTo() {
        return validTo;
    }

    public void setValidTo(LocalDate validTo) {
        this.validTo = validTo;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}
