package se.curanexus.audit.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Tracks changes to data for audit purposes.
 * Stores before/after values for compliance with PDL requirements.
 */
@Entity
@Table(name = "data_change_logs", indexes = {
    @Index(name = "idx_change_audit_event", columnList = "audit_event_id"),
    @Index(name = "idx_change_resource", columnList = "resource_type, resource_id"),
    @Index(name = "idx_change_timestamp", columnList = "timestamp")
})
public class DataChangeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "audit_event_id")
    private UUID auditEventId;

    @Column(name = "timestamp", nullable = false, updatable = false)
    private Instant timestamp;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false, length = 30)
    private ResourceType resourceType;

    @Column(name = "resource_id", nullable = false)
    private UUID resourceId;

    @Column(name = "field_name", nullable = false, length = 100)
    private String fieldName;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false, length = 20)
    private ChangeType changeType;

    protected DataChangeLog() {
    }

    public DataChangeLog(UUID auditEventId, ResourceType resourceType, UUID resourceId, 
                         String fieldName, String oldValue, String newValue, ChangeType changeType) {
        this.auditEventId = auditEventId;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.fieldName = fieldName;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.changeType = changeType;
        this.timestamp = Instant.now();
    }

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }

    public enum ChangeType {
        ADDED,
        MODIFIED,
        REMOVED
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public UUID getAuditEventId() {
        return auditEventId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public UUID getResourceId() {
        return resourceId;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public ChangeType getChangeType() {
        return changeType;
    }
}
