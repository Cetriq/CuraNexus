package se.curanexus.audit.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_events", indexes = {
    @Index(name = "idx_audit_event_user", columnList = "user_id"),
    @Index(name = "idx_audit_event_patient", columnList = "patient_id"),
    @Index(name = "idx_audit_event_resource", columnList = "resource_type, resource_id"),
    @Index(name = "idx_audit_event_timestamp", columnList = "timestamp"),
    @Index(name = "idx_audit_event_type", columnList = "event_type")
})
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    private AuditEventType eventType;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "username", length = 100)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false, length = 30)
    private ResourceType resourceType;

    @Column(name = "resource_id")
    private UUID resourceId;

    @Column(name = "patient_id")
    private UUID patientId;

    @Column(name = "encounter_id")
    private UUID encounterId;

    @Column(name = "action", length = 100)
    private String action;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "care_relation_id")
    private UUID careRelationId;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    protected AuditEvent() {
    }

    public AuditEvent(AuditEventType eventType, UUID userId, ResourceType resourceType) {
        if (eventType == null) {
            throw new IllegalArgumentException("Event type is required");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (resourceType == null) {
            throw new IllegalArgumentException("Resource type is required");
        }
        this.eventType = eventType;
        this.userId = userId;
        this.resourceType = resourceType;
        this.timestamp = Instant.now();
    }

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public AuditEventType getEventType() {
        return eventType;
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

    public void setResourceId(UUID resourceId) {
        this.resourceId = resourceId;
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

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public UUID getCareRelationId() {
        return careRelationId;
    }

    public void setCareRelationId(UUID careRelationId) {
        this.careRelationId = careRelationId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
