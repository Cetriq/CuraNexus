package se.curanexus.task.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tasks", indexes = {
    @Index(name = "idx_task_assignee", columnList = "assignee_id"),
    @Index(name = "idx_task_patient", columnList = "patient_id"),
    @Index(name = "idx_task_encounter", columnList = "encounter_id"),
    @Index(name = "idx_task_status", columnList = "status"),
    @Index(name = "idx_task_due", columnList = "due_at")
})
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private TaskCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TaskStatus status = TaskStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private TaskPriority priority = TaskPriority.NORMAL;

    @Column(name = "patient_id")
    private UUID patientId;

    @Column(name = "encounter_id")
    private UUID encounterId;

    @Column(name = "assignee_id")
    private UUID assigneeId;

    @Column(name = "created_by_id", nullable = false)
    private UUID createdById;

    @Column(name = "source_type", length = 50)
    private String sourceType;

    @Column(name = "source_id")
    private UUID sourceId;

    @Column(name = "due_at")
    private LocalDateTime dueAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "completion_note", length = 1000)
    private String completionNote;

    @Column(name = "outcome", length = 200)
    private String outcome;

    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    protected Task() {
    }

    public Task(String title, TaskCategory category, TaskPriority priority, UUID createdById) {
        this.title = title;
        this.category = category;
        this.priority = priority;
        this.createdById = createdById;
        this.createdAt = Instant.now();
    }

    public void assign(UUID assigneeId) {
        if (this.status == TaskStatus.COMPLETED || this.status == TaskStatus.CANCELLED) {
            throw new IllegalStateException("Cannot assign a " + status + " task");
        }
        this.assigneeId = assigneeId;
        if (this.status == TaskStatus.PENDING) {
            this.status = TaskStatus.ASSIGNED;
        }
        this.updatedAt = Instant.now();
    }

    public void start() {
        if (this.status != TaskStatus.ASSIGNED && this.status != TaskStatus.PENDING) {
            throw new IllegalStateException("Task can only be started from PENDING or ASSIGNED status");
        }
        this.status = TaskStatus.IN_PROGRESS;
        this.startedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void complete(String completionNote, String outcome) {
        if (this.status == TaskStatus.COMPLETED || this.status == TaskStatus.CANCELLED) {
            throw new IllegalStateException("Task is already " + status);
        }
        this.status = TaskStatus.COMPLETED;
        this.completionNote = completionNote;
        this.outcome = outcome;
        this.completedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void cancel(String reason) {
        if (this.status == TaskStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel a completed task");
        }
        if (this.status == TaskStatus.CANCELLED) {
            throw new IllegalStateException("Task is already cancelled");
        }
        this.status = TaskStatus.CANCELLED;
        this.cancelReason = reason;
        this.updatedAt = Instant.now();
    }

    public void block() {
        if (this.status == TaskStatus.COMPLETED || this.status == TaskStatus.CANCELLED) {
            throw new IllegalStateException("Cannot block a " + status + " task");
        }
        this.status = TaskStatus.BLOCKED;
        this.updatedAt = Instant.now();
    }

    public void unblock() {
        if (this.status != TaskStatus.BLOCKED) {
            throw new IllegalStateException("Task is not blocked");
        }
        this.status = this.assigneeId != null ? TaskStatus.ASSIGNED : TaskStatus.PENDING;
        this.updatedAt = Instant.now();
    }

    public boolean isOverdue() {
        return dueAt != null &&
               LocalDateTime.now().isAfter(dueAt) &&
               status != TaskStatus.COMPLETED &&
               status != TaskStatus.CANCELLED;
    }

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        this.updatedAt = Instant.now();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = Instant.now();
    }

    public TaskCategory getCategory() {
        return category;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
        this.updatedAt = Instant.now();
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

    public UUID getAssigneeId() {
        return assigneeId;
    }

    public UUID getCreatedById() {
        return createdById;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public UUID getSourceId() {
        return sourceId;
    }

    public void setSourceId(UUID sourceId) {
        this.sourceId = sourceId;
    }

    public LocalDateTime getDueAt() {
        return dueAt;
    }

    public void setDueAt(LocalDateTime dueAt) {
        this.dueAt = dueAt;
        this.updatedAt = Instant.now();
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public String getCompletionNote() {
        return completionNote;
    }

    public String getOutcome() {
        return outcome;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
