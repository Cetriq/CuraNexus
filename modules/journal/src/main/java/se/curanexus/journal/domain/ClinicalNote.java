package se.curanexus.journal.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "clinical_notes", indexes = {
    @Index(name = "idx_note_encounter", columnList = "encounter_id"),
    @Index(name = "idx_note_patient", columnList = "patient_id"),
    @Index(name = "idx_note_created", columnList = "created_at")
})
public class ClinicalNote {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "encounter_id", nullable = false)
    private UUID encounterId;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private NoteType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private NoteStatus status = NoteStatus.DRAFT;

    @Column(name = "title", length = 200)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "author_id")
    private UUID authorId;

    @Column(name = "author_name", length = 200)
    private String authorName;

    @Column(name = "signed_at")
    private Instant signedAt;

    @Column(name = "signed_by_id")
    private UUID signedById;

    @Column(name = "signed_by_name", length = 200)
    private String signedByName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ClinicalNote() {
    }

    public ClinicalNote(UUID encounterId, UUID patientId, NoteType type, String content, UUID authorId) {
        this.encounterId = encounterId;
        this.patientId = patientId;
        this.type = type;
        this.content = content;
        this.authorId = authorId;
        this.status = NoteStatus.DRAFT;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public ClinicalNote(UUID encounterId, UUID patientId, NoteType type, UUID authorId, String authorName) {
        this.encounterId = encounterId;
        this.patientId = patientId;
        this.type = type;
        this.authorId = authorId;
        this.authorName = authorName;
        this.status = NoteStatus.DRAFT;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public boolean canSign() {
        return status == NoteStatus.DRAFT;
    }

    public void sign(UUID signedById, String signedByName) {
        if (!canSign()) {
            throw new IllegalStateException("Note cannot be signed in status: " + status);
        }
        this.signedById = signedById;
        this.signedByName = signedByName;
        this.signedAt = Instant.now();
        this.status = NoteStatus.FINAL;
    }

    public boolean canEdit() {
        return status == NoteStatus.DRAFT;
    }

    public void amend(String newContent) {
        if (status != NoteStatus.FINAL) {
            throw new IllegalStateException("Only FINAL notes can be amended, current status: " + status);
        }
        this.content = newContent;
        this.status = NoteStatus.AMENDED;
        this.updatedAt = Instant.now();
    }

    public void cancel() {
        if (status == NoteStatus.CANCELLED) {
            throw new IllegalStateException("Note is already cancelled");
        }
        this.status = NoteStatus.CANCELLED;
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

    public NoteType getType() {
        return type;
    }

    public void setType(NoteType type) {
        this.type = type;
    }

    public NoteStatus getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public UUID getAuthorId() {
        return authorId;
    }

    public void setAuthorId(UUID authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public Instant getSignedAt() {
        return signedAt;
    }

    public UUID getSignedById() {
        return signedById;
    }

    public String getSignedByName() {
        return signedByName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
