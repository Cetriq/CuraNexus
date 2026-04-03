package se.curanexus.medication.domain;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * Läkemedelsinteraktion - varning för interaktion mellan läkemedel.
 * Referensdata som används för interaktionskontroll.
 */
@Entity
@Table(name = "medication_interactions", indexes = {
    @Index(name = "idx_interaction_med1", columnList = "medication_id_1"),
    @Index(name = "idx_interaction_med2", columnList = "medication_id_2"),
    @Index(name = "idx_interaction_atc1", columnList = "atc_code_1"),
    @Index(name = "idx_interaction_atc2", columnList = "atc_code_2"),
    @Index(name = "idx_interaction_severity", columnList = "severity")
})
public class MedicationInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Första läkemedlet (valfritt, kan använda ATC istället) */
    @Column(name = "medication_id_1")
    private UUID medicationId1;

    /** Andra läkemedlet (valfritt, kan använda ATC istället) */
    @Column(name = "medication_id_2")
    private UUID medicationId2;

    /** ATC-kod för första substansen/gruppen */
    @Column(name = "atc_code_1", length = 10)
    private String atcCode1;

    /** ATC-kod för andra substansen/gruppen */
    @Column(name = "atc_code_2", length = 10)
    private String atcCode2;

    /** Allvarlighetsgrad */
    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    private InteractionSeverity severity;

    /** Beskrivning av interaktionen */
    @Column(name = "description", length = 2000, nullable = false)
    private String description;

    /** Klinisk effekt */
    @Column(name = "clinical_effect", length = 1000)
    private String clinicalEffect;

    /** Rekommendation/åtgärd */
    @Column(name = "recommendation", length = 1000)
    private String recommendation;

    /** Evidensgrad (A, B, C, D) */
    @Column(name = "evidence_level", length = 5)
    private String evidenceLevel;

    /** Källa (SFINX, Janusinfo, etc.) */
    @Column(name = "source", length = 50)
    private String source;

    /** Aktiv (visas i kontroll) */
    @Column(name = "active")
    private boolean active = true;

    protected MedicationInteraction() {
    }

    public MedicationInteraction(String atcCode1, String atcCode2, InteractionSeverity severity, String description) {
        this.atcCode1 = atcCode1;
        this.atcCode2 = atcCode2;
        this.severity = severity;
        this.description = description;
    }

    // Getters och setters

    public UUID getId() {
        return id;
    }

    public UUID getMedicationId1() {
        return medicationId1;
    }

    public void setMedicationId1(UUID medicationId1) {
        this.medicationId1 = medicationId1;
    }

    public UUID getMedicationId2() {
        return medicationId2;
    }

    public void setMedicationId2(UUID medicationId2) {
        this.medicationId2 = medicationId2;
    }

    public String getAtcCode1() {
        return atcCode1;
    }

    public void setAtcCode1(String atcCode1) {
        this.atcCode1 = atcCode1;
    }

    public String getAtcCode2() {
        return atcCode2;
    }

    public void setAtcCode2(String atcCode2) {
        this.atcCode2 = atcCode2;
    }

    public InteractionSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(InteractionSeverity severity) {
        this.severity = severity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getClinicalEffect() {
        return clinicalEffect;
    }

    public void setClinicalEffect(String clinicalEffect) {
        this.clinicalEffect = clinicalEffect;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public String getEvidenceLevel() {
        return evidenceLevel;
    }

    public void setEvidenceLevel(String evidenceLevel) {
        this.evidenceLevel = evidenceLevel;
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

    /**
     * Allvarlighetsgrad för interaktion.
     */
    public enum InteractionSeverity {
        /** Undvik kombination (kontraindicerat) */
        CONTRAINDICATED,

        /** Allvarlig risk - kräver åtgärd */
        SEVERE,

        /** Moderat risk - överväg alternativ */
        MODERATE,

        /** Lindrig risk - var uppmärksam */
        MINOR,

        /** Information endast */
        INFORMATIONAL
    }
}
