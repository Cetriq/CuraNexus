package se.curanexus.task.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "watches", indexes = {
    @Index(name = "idx_watch_user", columnList = "user_id"),
    @Index(name = "idx_watch_target", columnList = "watch_type, target_id")
})
public class Watch {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "watch_type", nullable = false, length = 30)
    private WatchType watchType;

    @Column(name = "target_id", nullable = false)
    private UUID targetId;

    @Column(name = "notify_on_change")
    private boolean notifyOnChange = true;

    @Column(name = "note", length = 500)
    private String note;

    @Column(name = "active")
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "last_notified_at")
    private Instant lastNotifiedAt;

    protected Watch() {
    }

    public Watch(UUID userId, WatchType watchType, UUID targetId) {
        this.userId = userId;
        this.watchType = watchType;
        this.targetId = targetId;
        this.createdAt = Instant.now();
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    public void recordNotification() {
        this.lastNotifiedAt = Instant.now();
    }

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public WatchType getWatchType() {
        return watchType;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public boolean isNotifyOnChange() {
        return notifyOnChange;
    }

    public void setNotifyOnChange(boolean notifyOnChange) {
        this.notifyOnChange = notifyOnChange;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getLastNotifiedAt() {
        return lastNotifiedAt;
    }
}
