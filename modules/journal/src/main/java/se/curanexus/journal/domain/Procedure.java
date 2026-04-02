package se.curanexus.journal.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "procedures", indexes = {
    @Index(name = "idx_procedure_encounter", columnList = "encounter_id"),
    @Index(name = "idx_procedure_patient", columnList = "patient_id"),
    @Index(name = "idx_procedure_code", columnList = "code")
})
public class Procedure {

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
    private String codeSystem = "KVÅ";

    @Column(name = "display_text", length = 500)
    private String displayText;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProcedureStatus status = ProcedureStatus.PLANNED;

    @Column(name = "performed_at")
    private LocalDateTime performedAt;

    @Column(name = "performed_by_id")
    private UUID performedById;

    @Column(name = "performed_by_name", length = 200)
    private String performedByName;

    @Column(name = "body_site", length = 100)
    private String bodySite;

    @Column(name = "laterality", length = 20)
    private String laterality;

    @Column(name = "outcome", length = 500)
    private String outcome;

    @Column(name = "notes", length = 2000)
    private String notes;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    protected Procedure() {
    }

    public Procedure(UUID encounterId, UUID patientId, String code) {
        this.encounterId = encounterId;
        this.patientId = patientId;
        this.code = code;
        this.createdAt = Instant.now();
    }

    public void start(UUID performedById, String performedByName) {
        if (this.status != ProcedureStatus.PLANNED) {
            throw new IllegalStateException("Procedure can only be started from PLANNED status");
        }
        this.status = ProcedureStatus.IN_PROGRESS;
        this.performedById = performedById;
        this.performedByName = performedByName;
        this.performedAt = LocalDateTime.now();
        this.updatedAt = Instant.now();
    }

    public void complete(String outcome) {
        if (this.status != ProcedureStatus.IN_PROGRESS) {
            throw new IllegalStateException("Procedure can only be completed from IN_PROGRESS status");
        }
        this.status = ProcedureStatus.COMPLETED;
        this.outcome = outcome;
        this.updatedAt = Instant.now();
    }

    public void cancel() {
        if (this.status == ProcedureStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel a completed procedure");
        }
        this.status = ProcedureStatus.CANCELLED;
        this.updatedAt = Instant.now();
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

    public ProcedureStatus getStatus() {
        return status;
    }

    public LocalDateTime getPerformedAt() {
        return performedAt;
    }

    public UUID getPerformedById() {
        return performedById;
    }

    public String getPerformedByName() {
        return performedByName;
    }

    public String getBodySite() {
        return bodySite;
    }

    public void setBodySite(String bodySite) {
        this.bodySite = bodySite;
    }

    public String getLaterality() {
        return laterality;
    }

    public void setLaterality(String laterality) {
        this.laterality = laterality;
    }

    public String getOutcome() {
        return outcome;
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
}
