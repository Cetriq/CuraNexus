package se.curanexus.medication.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Läkemedelsallergi/överkänslighet - registrerad allergi eller överkänslighet.
 */
@Entity
@Table(name = "drug_allergies", indexes = {
    @Index(name = "idx_allergy_patient", columnList = "patient_id"),
    @Index(name = "idx_allergy_atc", columnList = "atc_code"),
    @Index(name = "idx_allergy_active", columnList = "active")
})
public class DrugAllergy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Patient */
    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    /** Läkemedel (om kopplat till specifikt läkemedel) */
    @Column(name = "medication_id")
    private UUID medicationId;

    /** ATC-kod (för substans-/gruppnivå) */
    @Column(name = "atc_code", length = 10)
    private String atcCode;

    /** Substansnamn (fritext) */
    @Column(name = "substance_name", length = 200)
    private String substanceName;

    /** Reaktionstyp */
    @Enumerated(EnumType.STRING)
    @Column(name = "reaction_type", nullable = false, length = 30)
    private ReactionType reactionType;

    /** Allvarlighetsgrad */
    @Enumerated(EnumType.STRING)
    @Column(name = "severity", length = 20)
    private AllergySeverity severity;

    /** Beskrivning av reaktion */
    @Column(name = "reaction_description", length = 1000)
    private String reactionDescription;

    /** När reaktionen inträffade första gången */
    @Column(name = "onset_date")
    private LocalDate onsetDate;

    /** Verifierad (bekräftad genom test eller tydlig anamnes) */
    @Column(name = "verified")
    private boolean verified = false;

    /** Verifierad av */
    @Column(name = "verified_by_id")
    private UUID verifiedById;

    /** Verifierad datum */
    @Column(name = "verified_at")
    private Instant verifiedAt;

    /** Källa (patient, journal, nationellt register) */
    @Column(name = "source", length = 50)
    private String source;

    /** Aktiv */
    @Column(name = "active")
    private boolean active = true;

    /** Registrerad av */
    @Column(name = "recorded_by_id")
    private UUID recordedById;

    /** Registrerad av namn */
    @Column(name = "recorded_by_name", length = 200)
    private String recordedByName;

    /** Kommentar */
    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    protected DrugAllergy() {
    }

    public DrugAllergy(UUID patientId, String substanceName, ReactionType reactionType) {
        this.patientId = patientId;
        this.substanceName = substanceName;
        this.reactionType = reactionType;
        this.createdAt = Instant.now();
    }

    // Affärslogik

    /**
     * Verifiera allergin.
     */
    public void verify(UUID verifiedById) {
        this.verified = true;
        this.verifiedById = verifiedById;
        this.verifiedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Inaktivera allergi (t.ex. efter negativ provokation).
     */
    public void deactivate(String reason) {
        this.active = false;
        this.notes = (notes != null ? notes + "\n" : "") + "Inaktiverad: " + reason;
        this.updatedAt = Instant.now();
    }

    // Getters och setters

    public UUID getId() {
        return id;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public UUID getMedicationId() {
        return medicationId;
    }

    public void setMedicationId(UUID medicationId) {
        this.medicationId = medicationId;
    }

    public String getAtcCode() {
        return atcCode;
    }

    public void setAtcCode(String atcCode) {
        this.atcCode = atcCode;
    }

    public String getSubstanceName() {
        return substanceName;
    }

    public void setSubstanceName(String substanceName) {
        this.substanceName = substanceName;
    }

    public ReactionType getReactionType() {
        return reactionType;
    }

    public void setReactionType(ReactionType reactionType) {
        this.reactionType = reactionType;
    }

    public AllergySeverity getSeverity() {
        return severity;
    }

    public void setSeverity(AllergySeverity severity) {
        this.severity = severity;
    }

    public String getReactionDescription() {
        return reactionDescription;
    }

    public void setReactionDescription(String reactionDescription) {
        this.reactionDescription = reactionDescription;
    }

    public LocalDate getOnsetDate() {
        return onsetDate;
    }

    public void setOnsetDate(LocalDate onsetDate) {
        this.onsetDate = onsetDate;
    }

    public boolean isVerified() {
        return verified;
    }

    public UUID getVerifiedById() {
        return verifiedById;
    }

    public Instant getVerifiedAt() {
        return verifiedAt;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public UUID getRecordedById() {
        return recordedById;
    }

    public void setRecordedById(UUID recordedById) {
        this.recordedById = recordedById;
    }

    public String getRecordedByName() {
        return recordedByName;
    }

    public void setRecordedByName(String recordedByName) {
        this.recordedByName = recordedByName;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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

    /**
     * Typ av reaktion.
     */
    public enum ReactionType {
        /** Allergisk reaktion (immunologisk) */
        ALLERGY,

        /** Överkänslighet (ej immunologisk) */
        INTOLERANCE,

        /** Biverkning */
        SIDE_EFFECT,

        /** Okänd typ */
        UNKNOWN
    }

    /**
     * Allvarlighetsgrad.
     */
    public enum AllergySeverity {
        /** Mild reaktion */
        MILD,

        /** Måttlig reaktion */
        MODERATE,

        /** Allvarlig reaktion */
        SEVERE,

        /** Livshotande (anafylaxi) */
        LIFE_THREATENING
    }
}
