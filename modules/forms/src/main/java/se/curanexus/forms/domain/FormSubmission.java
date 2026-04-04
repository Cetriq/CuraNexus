package se.curanexus.forms.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A submission of a form by a patient or healthcare professional.
 */
@Entity
@Table(name = "form_submissions")
public class FormSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private FormTemplate template;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Column(name = "encounter_id")
    private UUID encounterId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SubmissionStatus status = SubmissionStatus.IN_PROGRESS;

    @Column(name = "submitted_by")
    private UUID submittedBy;

    @Column(name = "submitted_by_role", length = 50)
    private String submittedByRole;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "reviewed_by")
    private UUID reviewedBy;

    @Column(name = "reviewer_notes", length = 2000)
    private String reviewerNotes;

    @Column(name = "total_score")
    private Double totalScore;

    @Column(name = "score_interpretation", length = 500)
    private String scoreInterpretation;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "source", length = 50)
    private String source;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FormAnswer> answers = new ArrayList<>();

    protected FormSubmission() {
    }

    public FormSubmission(FormTemplate template, UUID patientId) {
        this.template = template;
        this.patientId = patientId;
        this.startedAt = Instant.now();
    }

    public void addAnswer(FormAnswer answer) {
        answers.add(answer);
        answer.setSubmission(this);
    }

    public void removeAnswer(FormAnswer answer) {
        answers.remove(answer);
        answer.setSubmission(null);
    }

    public void complete() {
        if (this.status == SubmissionStatus.IN_PROGRESS) {
            this.status = SubmissionStatus.COMPLETED;
            this.completedAt = Instant.now();
        }
    }

    public void review(UUID reviewerId, String notes) {
        if (this.status == SubmissionStatus.COMPLETED) {
            this.status = SubmissionStatus.REVIEWED;
            this.reviewedAt = Instant.now();
            this.reviewedBy = reviewerId;
            this.reviewerNotes = notes;
        }
    }

    public void cancel() {
        if (this.status == SubmissionStatus.IN_PROGRESS) {
            this.status = SubmissionStatus.CANCELLED;
        }
    }

    public void expire() {
        if (this.status == SubmissionStatus.IN_PROGRESS) {
            this.status = SubmissionStatus.EXPIRED;
        }
    }

    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt)
                && status == SubmissionStatus.IN_PROGRESS;
    }

    public FormAnswer getAnswerForField(String fieldKey) {
        return answers.stream()
                .filter(a -> a.getFieldKey().equals(fieldKey))
                .findFirst()
                .orElse(null);
    }

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public FormTemplate getTemplate() {
        return template;
    }

    public void setTemplate(FormTemplate template) {
        this.template = template;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }

    public UUID getEncounterId() {
        return encounterId;
    }

    public void setEncounterId(UUID encounterId) {
        this.encounterId = encounterId;
    }

    public SubmissionStatus getStatus() {
        return status;
    }

    public void setStatus(SubmissionStatus status) {
        this.status = status;
    }

    public UUID getSubmittedBy() {
        return submittedBy;
    }

    public void setSubmittedBy(UUID submittedBy) {
        this.submittedBy = submittedBy;
    }

    public String getSubmittedByRole() {
        return submittedByRole;
    }

    public void setSubmittedByRole(String submittedByRole) {
        this.submittedByRole = submittedByRole;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }

    public UUID getReviewedBy() {
        return reviewedBy;
    }

    public String getReviewerNotes() {
        return reviewerNotes;
    }

    public void setReviewerNotes(String reviewerNotes) {
        this.reviewerNotes = reviewerNotes;
    }

    public Double getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Double totalScore) {
        this.totalScore = totalScore;
    }

    public String getScoreInterpretation() {
        return scoreInterpretation;
    }

    public void setScoreInterpretation(String scoreInterpretation) {
        this.scoreInterpretation = scoreInterpretation;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public List<FormAnswer> getAnswers() {
        return answers;
    }
}
