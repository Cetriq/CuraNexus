package se.curanexus.medication.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Läkemedelsadministrering - registrering av given dos.
 * Används för att dokumentera när läkemedel faktiskt ges till patient.
 */
@Entity
@Table(name = "medication_administrations", indexes = {
    @Index(name = "idx_admin_patient", columnList = "patient_id"),
    @Index(name = "idx_admin_prescription", columnList = "prescription_id"),
    @Index(name = "idx_admin_encounter", columnList = "encounter_id"),
    @Index(name = "idx_admin_administered_at", columnList = "administered_at"),
    @Index(name = "idx_admin_status", columnList = "status")
})
public class MedicationAdministration {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Patient */
    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    /** Ordination som administreringen baseras på */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id")
    private Prescription prescription;

    /** Vårdkontakt */
    @Column(name = "encounter_id")
    private UUID encounterId;

    /** Status */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private AdministrationStatus status = AdministrationStatus.PLANNED;

    /** Planerad tidpunkt */
    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    /** Faktisk tidpunkt för administrering */
    @Column(name = "administered_at")
    private LocalDateTime administeredAt;

    /** Given dos */
    @Column(name = "dose_quantity", precision = 18, scale = 4)
    private BigDecimal doseQuantity;

    /** Dosenhet */
    @Column(name = "dose_unit", length = 30)
    private String doseUnit;

    /** Administreringsväg */
    @Enumerated(EnumType.STRING)
    @Column(name = "route", length = 30)
    private RouteOfAdministration route;

    /** Administreringsställe (t.ex. "vänster överarm") */
    @Column(name = "body_site", length = 100)
    private String bodySite;

    /** Administreringsmetod (t.ex. "IV push", "Slow IV infusion") */
    @Column(name = "method", length = 100)
    private String method;

    /** Infusionshastighet (för IV) */
    @Column(name = "rate_quantity", precision = 18, scale = 4)
    private BigDecimal rateQuantity;

    /** Infusionshastighets-enhet (ml/h, etc.) */
    @Column(name = "rate_unit", length = 30)
    private String rateUnit;

    /** Vem som administrerade */
    @Column(name = "performer_id")
    private UUID performerId;

    /** Utförares HSA-ID */
    @Column(name = "performer_hsa_id", length = 50)
    private String performerHsaId;

    /** Utförares namn */
    @Column(name = "performer_name", length = 200)
    private String performerName;

    /** Anledning till att dos ej gavs (om status NOT_DONE) */
    @Column(name = "not_given_reason", length = 500)
    private String notGivenReason;

    /** Kommentar/anteckning */
    @Column(name = "notes", length = 1000)
    private String notes;

    /** Batch-/LOT-nummer (för spårbarhet) */
    @Column(name = "lot_number", length = 50)
    private String lotNumber;

    /** Utgångsdatum på förpackningen */
    @Column(name = "expiration_date")
    private LocalDateTime expirationDate;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    protected MedicationAdministration() {
    }

    public MedicationAdministration(UUID patientId, Prescription prescription) {
        this.patientId = patientId;
        this.prescription = prescription;
        this.status = AdministrationStatus.PLANNED;
        this.createdAt = Instant.now();
    }

    // Affärslogik

    /**
     * Starta administrering (för infusioner eller längre behandlingar).
     */
    public void startAdministration(UUID performerId, String performerName) {
        if (status != AdministrationStatus.PLANNED) {
            throw new IllegalStateException("Kan endast starta planerad administrering");
        }
        this.status = AdministrationStatus.IN_PROGRESS;
        this.performerId = performerId;
        this.performerName = performerName;
        this.administeredAt = LocalDateTime.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Markera administrering som utförd.
     */
    public void complete(UUID performerId, String performerName, BigDecimal givenDose, String doseUnit) {
        if (status != AdministrationStatus.PLANNED && status != AdministrationStatus.IN_PROGRESS) {
            throw new IllegalStateException("Kan endast slutföra planerad eller pågående administrering");
        }
        this.status = AdministrationStatus.COMPLETED;
        this.performerId = performerId;
        this.performerName = performerName;
        this.doseQuantity = givenDose;
        this.doseUnit = doseUnit;
        if (this.administeredAt == null) {
            this.administeredAt = LocalDateTime.now();
        }
        this.updatedAt = Instant.now();
    }

    /**
     * Markera som ej given.
     */
    public void markNotGiven(String reason) {
        if (status == AdministrationStatus.COMPLETED) {
            throw new IllegalStateException("Kan ej markera slutförd administrering som ej given");
        }
        this.status = AdministrationStatus.NOT_DONE;
        this.notGivenReason = reason;
        this.updatedAt = Instant.now();
    }

    /**
     * Avbryt pågående administrering.
     */
    public void stop(String reason) {
        if (status != AdministrationStatus.IN_PROGRESS) {
            throw new IllegalStateException("Kan endast avbryta pågående administrering");
        }
        this.status = AdministrationStatus.STOPPED;
        this.notes = (notes != null ? notes + "\n" : "") + "Avbruten: " + reason;
        this.updatedAt = Instant.now();
    }

    /**
     * Markera som felaktigt registrerad.
     */
    public void markAsEnteredInError(String reason) {
        this.status = AdministrationStatus.ENTERED_IN_ERROR;
        this.notes = (notes != null ? notes + "\n" : "") + "Felaktigt registrerad: " + reason;
        this.updatedAt = Instant.now();
    }

    /**
     * Kontrollera om administreringen är försenad.
     */
    public boolean isOverdue() {
        if (status != AdministrationStatus.PLANNED || scheduledAt == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(scheduledAt.plusMinutes(30)); // 30 min grace period
    }

    // Getters och setters

    public UUID getId() {
        return id;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public Prescription getPrescription() {
        return prescription;
    }

    public void setPrescription(Prescription prescription) {
        this.prescription = prescription;
    }

    public UUID getEncounterId() {
        return encounterId;
    }

    public void setEncounterId(UUID encounterId) {
        this.encounterId = encounterId;
    }

    public AdministrationStatus getStatus() {
        return status;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public LocalDateTime getAdministeredAt() {
        return administeredAt;
    }

    public void setAdministeredAt(LocalDateTime administeredAt) {
        this.administeredAt = administeredAt;
    }

    public BigDecimal getDoseQuantity() {
        return doseQuantity;
    }

    public void setDoseQuantity(BigDecimal doseQuantity) {
        this.doseQuantity = doseQuantity;
    }

    public String getDoseUnit() {
        return doseUnit;
    }

    public void setDoseUnit(String doseUnit) {
        this.doseUnit = doseUnit;
    }

    public RouteOfAdministration getRoute() {
        return route;
    }

    public void setRoute(RouteOfAdministration route) {
        this.route = route;
    }

    public String getBodySite() {
        return bodySite;
    }

    public void setBodySite(String bodySite) {
        this.bodySite = bodySite;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public BigDecimal getRateQuantity() {
        return rateQuantity;
    }

    public void setRateQuantity(BigDecimal rateQuantity) {
        this.rateQuantity = rateQuantity;
    }

    public String getRateUnit() {
        return rateUnit;
    }

    public void setRateUnit(String rateUnit) {
        this.rateUnit = rateUnit;
    }

    public UUID getPerformerId() {
        return performerId;
    }

    public void setPerformerId(UUID performerId) {
        this.performerId = performerId;
    }

    public String getPerformerHsaId() {
        return performerHsaId;
    }

    public void setPerformerHsaId(String performerHsaId) {
        this.performerHsaId = performerHsaId;
    }

    public String getPerformerName() {
        return performerName;
    }

    public void setPerformerName(String performerName) {
        this.performerName = performerName;
    }

    public String getNotGivenReason() {
        return notGivenReason;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getLotNumber() {
        return lotNumber;
    }

    public void setLotNumber(String lotNumber) {
        this.lotNumber = lotNumber;
    }

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
