package se.curanexus.task.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reminders", indexes = {
    @Index(name = "idx_reminder_user", columnList = "user_id"),
    @Index(name = "idx_reminder_remind_at", columnList = "remind_at"),
    @Index(name = "idx_reminder_status", columnList = "status")
})
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "message", nullable = false, length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReminderStatus status = ReminderStatus.PENDING;

    @Column(name = "remind_at", nullable = false)
    private LocalDateTime remindAt;

    @Column(name = "patient_id")
    private UUID patientId;

    @Column(name = "encounter_id")
    private UUID encounterId;

    @Column(name = "task_id")
    private UUID taskId;

    @Column(name = "recurring")
    private boolean recurring = false;

    @Column(name = "recurrence_pattern", length = 100)
    private String recurrencePattern;

    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

    @Column(name = "snoozed_until")
    private LocalDateTime snoozedUntil;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Reminder() {
    }

    public Reminder(UUID userId, String message, LocalDateTime remindAt) {
        this.userId = userId;
        this.message = message;
        this.remindAt = remindAt;
        this.createdAt = Instant.now();
    }

    public void trigger() {
        if (this.status != ReminderStatus.PENDING && this.status != ReminderStatus.SNOOZED) {
            throw new IllegalStateException("Reminder can only be triggered from PENDING or SNOOZED status");
        }
        this.status = ReminderStatus.TRIGGERED;
    }

    public void acknowledge() {
        if (this.status != ReminderStatus.TRIGGERED && this.status != ReminderStatus.SNOOZED) {
            throw new IllegalStateException("Reminder can only be acknowledged when TRIGGERED or SNOOZED");
        }
        this.status = ReminderStatus.ACKNOWLEDGED;
        this.acknowledgedAt = Instant.now();
    }

    public void snooze(LocalDateTime until) {
        if (this.status != ReminderStatus.TRIGGERED) {
            throw new IllegalStateException("Only triggered reminders can be snoozed");
        }
        this.status = ReminderStatus.SNOOZED;
        this.snoozedUntil = until;
        this.remindAt = until;
    }

    public void cancel() {
        if (this.status == ReminderStatus.ACKNOWLEDGED) {
            throw new IllegalStateException("Cannot cancel an acknowledged reminder");
        }
        this.status = ReminderStatus.CANCELLED;
    }

    public boolean isDue() {
        return (status == ReminderStatus.PENDING || status == ReminderStatus.SNOOZED) &&
               LocalDateTime.now().isAfter(remindAt);
    }

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ReminderStatus getStatus() {
        return status;
    }

    public LocalDateTime getRemindAt() {
        return remindAt;
    }

    public void setRemindAt(LocalDateTime remindAt) {
        this.remindAt = remindAt;
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

    public UUID getTaskId() {
        return taskId;
    }

    public void setTaskId(UUID taskId) {
        this.taskId = taskId;
    }

    public boolean isRecurring() {
        return recurring;
    }

    public void setRecurring(boolean recurring) {
        this.recurring = recurring;
    }

    public String getRecurrencePattern() {
        return recurrencePattern;
    }

    public void setRecurrencePattern(String recurrencePattern) {
        this.recurrencePattern = recurrencePattern;
    }

    public Instant getAcknowledgedAt() {
        return acknowledgedAt;
    }

    public LocalDateTime getSnoozedUntil() {
        return snoozedUntil;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
