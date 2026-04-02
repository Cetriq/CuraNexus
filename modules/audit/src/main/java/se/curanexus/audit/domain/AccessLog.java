package se.curanexus.audit.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "access_logs", indexes = {
    @Index(name = "idx_access_log_user", columnList = "user_id"),
    @Index(name = "idx_access_log_patient", columnList = "patient_id"),
    @Index(name = "idx_access_log_timestamp", columnList = "timestamp"),
    @Index(name = "idx_access_log_resource", columnList = "resource_type, resource_id")
})
public class AccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false, length = 30)
    private ResourceType resourceType;

    @Column(name = "resource_id")
    private UUID resourceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_type", nullable = false, length = 20)
    private AccessType accessType;

    @Column(name = "care_relation_id")
    private UUID careRelationId;

    @Column(name = "care_relation_type", length = 30)
    private String careRelationType;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    protected AccessLog() {
    }

    public AccessLog(UUID userId, UUID patientId, ResourceType resourceType, AccessType accessType) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (patientId == null) {
            throw new IllegalArgumentException("Patient ID is required");
        }
        if (resourceType == null) {
            throw new IllegalArgumentException("Resource type is required");
        }
        if (accessType == null) {
            throw new IllegalArgumentException("Access type is required");
        }
        this.userId = userId;
        this.patientId = patientId;
        this.resourceType = resourceType;
        this.accessType = accessType;
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

    public UUID getPatientId() {
        return patientId;
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

    public AccessType getAccessType() {
        return accessType;
    }

    public UUID getCareRelationId() {
        return careRelationId;
    }

    public void setCareRelationId(UUID careRelationId) {
        this.careRelationId = careRelationId;
    }

    public String getCareRelationType() {
        return careRelationType;
    }

    public void setCareRelationType(String careRelationType) {
        this.careRelationType = careRelationType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
