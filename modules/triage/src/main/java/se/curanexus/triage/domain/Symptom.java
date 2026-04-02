package se.curanexus.triage.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "symptoms", indexes = {
    @Index(name = "idx_symptom_assessment", columnList = "assessment_id"),
    @Index(name = "idx_symptom_code", columnList = "symptom_code")
})
public class Symptom {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false)
    private TriageAssessment assessment;

    @Column(name = "symptom_code", nullable = false, length = 20)
    private String symptomCode;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Column(name = "onset")
    private Instant onset;

    @Column(name = "duration", length = 50)
    private String duration;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", length = 20)
    private Severity severity;

    @Column(name = "body_location", length = 100)
    private String bodyLocation;

    @Column(name = "is_chief_complaint", nullable = false)
    private boolean isChiefComplaint;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    protected Symptom() {
    }

    public Symptom(String symptomCode, String description) {
        if (symptomCode == null || symptomCode.isBlank()) {
            throw new IllegalArgumentException("Symptom code is required");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description is required");
        }

        this.symptomCode = symptomCode;
        this.description = description;
        this.isChiefComplaint = false;
        this.recordedAt = Instant.now();
    }

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public TriageAssessment getAssessment() {
        return assessment;
    }

    void setAssessment(TriageAssessment assessment) {
        this.assessment = assessment;
    }

    public String getSymptomCode() {
        return symptomCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getOnset() {
        return onset;
    }

    public void setOnset(Instant onset) {
        this.onset = onset;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public String getBodyLocation() {
        return bodyLocation;
    }

    public void setBodyLocation(String bodyLocation) {
        this.bodyLocation = bodyLocation;
    }

    public boolean isChiefComplaint() {
        return isChiefComplaint;
    }

    public void setChiefComplaint(boolean chiefComplaint) {
        isChiefComplaint = chiefComplaint;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }
}
