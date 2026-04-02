package se.curanexus.audit.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "change_logs", indexes = {
    @Index(name = "idx_change_log_user", columnList = "user_id"),
    @Index(name = "idx_change_log_resource", columnList = "resource_type, resource_id"),
    @Index(name = "idx_change_log_timestamp", columnList = "timestamp")
})
public class ChangeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "username", length = 100)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false, length = 30)
    private ResourceType resourceType;

    @Column(name = "resource_id", nullable = false)
    private UUID resourceId;

    @Column(name = "patient_id")
    private UUID patientId;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false, length = 20)
    private ChangeType changeType;

    @Column(name = "field_name", length = 100)
    private String fieldName;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    protected ChangeLog() {
    }

    public ChangeLog(UUID userId, ResourceType resourceType, UUID resourceId, ChangeType changeType) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (resourceType == null) {
            throw new IllegalArgumentException("Resource type is required");
        }
        if (resourceId == null) {
            throw new IllegalArgumentException("Resource ID is required");
        }
        if (changeType == null) {
            throw new IllegalArgumentException("Change type is required");
        }
        this.userId = userId;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.changeType = changeType;
        this.timestamp = Instant.now();
    }

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public UUID getResourceId() {
        return resourceId;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
