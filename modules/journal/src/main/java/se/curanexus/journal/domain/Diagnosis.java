package se.curanexus.journal.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "diagnoses", indexes = {
    @Index(name = "idx_diagnosis_encounter", columnList = "encounter_id"),
    @Index(name = "idx_diagnosis_patient", columnList = "patient_id"),
    @Index(name = "idx_diagnosis_code", columnList = "code")
})
public class Diagnosis {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "encounter_id", nullable = false)
    private UUID encounterId;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Column(name = "code", nullable = false, length = 20)
    private String code;

    @Column(name = "code_system", nullable = false, length = 50)
    private String codeSystem = "ICD-10-SE";

    @Column(name = "display_text", length = 500)
    private String displayText;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20)
    private DiagnosisType type;

    @Column(name = "rank")
    private Integer rank;

    @Column(name = "onset_date")
    private LocalDate onsetDate;

    @Column(name = "resolved_date")
    private LocalDate resolvedDate;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    @Column(name = "recorded_by_id")
    private UUID recordedById;

    protected Diagnosis() {
    }

    public Diagnosis(UUID encounterId, UUID patientId, String code) {
        this.encounterId = encounterId;
        this.patientId = patientId;
        this.code = code;
        this.recordedAt = Instant.now();
    }

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public UUID getEncounterId() {
        return encounterId;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCodeSystem() {
        return codeSystem;
    }

    public void setCodeSystem(String codeSystem) {
        this.codeSystem = codeSystem;
    }

    public String getDisplayText() {
        return displayText;
    }

    public void setDisplayText(String displayText) {
        this.displayText = displayText;
    }

    public DiagnosisType getType() {
        return type;
    }

    public void setType(DiagnosisType type) {
        this.type = type;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public LocalDate getOnsetDate() {
        return onsetDate;
    }

    public void setOnsetDate(LocalDate onsetDate) {
        this.onsetDate = onsetDate;
    }

    public LocalDate getResolvedDate() {
        return resolvedDate;
    }

    public void setResolvedDate(LocalDate resolvedDate) {
        this.resolvedDate = resolvedDate;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }

    public UUID getRecordedById() {
        return recordedById;
    }

    public void setRecordedById(UUID recordedById) {
        this.recordedById = recordedById;
    }
}
