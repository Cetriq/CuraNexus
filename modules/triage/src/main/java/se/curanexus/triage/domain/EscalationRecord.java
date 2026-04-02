package se.curanexus.triage.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "escalation_records", indexes = {
    @Index(name = "idx_escalation_assessment", columnList = "assessment_id")
})
public class EscalationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false)
    private TriageAssessment assessment;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_priority", length = 20)
    private TriagePriority previousPriority;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_priority", nullable = false, length = 20)
    private TriagePriority newPriority;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "escalated_by", nullable = false)
    private UUID escalatedBy;

    @Column(name = "escalated_at", nullable = false)
    private Instant escalatedAt;

    protected EscalationRecord() {
    }

    public EscalationRecord(TriageAssessment assessment, TriagePriority previousPriority,
                            TriagePriority newPriority, String reason, UUID escalatedBy) {
        this.assessment = assessment;
        this.previousPriority = previousPriority;
        this.newPriority = newPriority;
        this.reason = reason;
        this.escalatedBy = escalatedBy;
        this.escalatedAt = Instant.now();
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public TriageAssessment getAssessment() {
        return assessment;
    }

    public TriagePriority getPreviousPriority() {
        return previousPriority;
    }

    public TriagePriority getNewPriority() {
        return newPriority;
    }

    public String getReason() {
        return reason;
    }

    public UUID getEscalatedBy() {
        return escalatedBy;
    }

    public Instant getEscalatedAt() {
        return escalatedAt;
    }
}
