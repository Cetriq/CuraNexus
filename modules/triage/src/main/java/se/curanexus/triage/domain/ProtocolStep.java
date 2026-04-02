package se.curanexus.triage.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "protocol_steps", indexes = {
    @Index(name = "idx_step_protocol", columnList = "protocol_id")
})
public class ProtocolStep {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "protocol_id", nullable = false)
    private TriageProtocol protocol;

    @Column(name = "step_order", nullable = false)
    private int stepOrder;

    @Column(name = "instruction", nullable = false, columnDefinition = "TEXT")
    private String instruction;

    @Column(name = "assessment_criteria", columnDefinition = "TEXT")
    private String assessmentCriteria;

    @ElementCollection
    @CollectionTable(name = "step_actions", joinColumns = @JoinColumn(name = "step_id"))
    @Column(name = "action", length = 500)
    private List<String> actions = new ArrayList<>();

    protected ProtocolStep() {
    }

    public ProtocolStep(int stepOrder, String instruction) {
        if (instruction == null || instruction.isBlank()) {
            throw new IllegalArgumentException("Instruction is required");
        }

        this.stepOrder = stepOrder;
        this.instruction = instruction;
    }

    void setProtocol(TriageProtocol protocol) {
        this.protocol = protocol;
    }

    public void addAction(String action) {
        actions.add(action);
    }

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public TriageProtocol getProtocol() {
        return protocol;
    }

    public int getStepOrder() {
        return stepOrder;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public String getAssessmentCriteria() {
        return assessmentCriteria;
    }

    public void setAssessmentCriteria(String assessmentCriteria) {
        this.assessmentCriteria = assessmentCriteria;
    }

    public List<String> getActions() {
        return actions;
    }
}
