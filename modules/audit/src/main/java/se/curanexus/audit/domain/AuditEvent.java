package se.curanexus.audit.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Represents an auditable event in the system.
 * Designed to meet PDL (Patientdatalagen) requirements for healthcare data access logging.
 * 
 * Key requirements:
 * - WHO: userId, userRole, userHsaId
 * - WHAT: action, resourceType, resourceId
 * - WHEN: timestamp
 * - WHERE: careUnitId, careUnitName, ipAddress
 * - WHY: careRelationContext, accessReason
 */
@Entity
@Table(name = "audit_events", indexes = {
    @Index(name = "idx_audit_patient", columnList = "patient_id"),
    @Index(name = "idx_audit_user", columnList = "user_id"),
    @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
    @Index(name = "idx_audit_resource", columnList = "resource_type, resource_id"),
    @Index(name = "idx_audit_care_unit", columnList = "care_unit_id")
})
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // WHEN
    @Column(name = "timestamp", nullable = false, updatable = false)
    private Instant timestamp;

    // WHO - User information
    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @Column(name = "user_hsa_id", length = 50)
    private String userHsaId;

    @Column(name = "user_name", length = 200)
    private String userName;

    @Column(name = "user_role", length = 50)
    private String userRole;

    // WHAT - Action and resource
    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 30)
    private AuditAction action;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false, length = 30)
    private ResourceType resourceType;

    @Column(name = "resource_id")
    private UUID resourceId;

    @Column(name = "resource_description", length = 500)
    private String resourceDescription;

    // Patient context (if applicable)
    @Column(name = "patient_id")
    private UUID patientId;

    @Column(name = "patient_personnummer_hash", length = 64)
    private String patientPersonnummerHash;

    // WHERE - Location context
    @Column(name = "care_unit_id")
    private UUID careUnitId;

    @Column(name = "care_unit_name", length = 200)
    private String careUnitName;

    @Column(name = "care_unit_hsa_id", length = 50)
    private String careUnitHsaId;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "session_id", length = 100)
    private String sessionId;

    // WHY - Care relation context
    @Column(name = "encounter_id")
    private UUID encounterId;

    @Column(name = "access_reason", length = 500)
    private String accessReason;

    @Column(name = "emergency_access", nullable = false)
    private boolean emergencyAccess = false;

    @Column(name = "consent_reference")
    private UUID consentReference;

    // Result
    @Column(name = "success", nullable = false)
    private boolean success = true;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    // Additional details as JSON
    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    // Source system
    @Column(name = "source_system", length = 50)
    private String sourceSystem;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    protected AuditEvent() {
    }

    public AuditEvent(String userId, AuditAction action, ResourceType resourceType) {
        this.userId = userId;
        this.action = action;
        this.resourceType = resourceType;
        this.timestamp = Instant.now();
    }

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserHsaId() {
        return userHsaId;
    }

    public void setUserHsaId(String userHsaId) {
        this.userHsaId = userHsaId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public AuditAction getAction() {
        return action;
    }

    public void setAction(AuditAction action) {
        this.action = action;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public UUID getResourceId() {
        return resourceId;
    }

    public void setResourceId(UUID resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceDescription() {
        return resourceDescription;
    }

    public void setResourceDescription(String resourceDescription) {
        this.resourceDescription = resourceDescription;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }

    public String getPatientPersonnummerHash() {
        return patientPersonnummerHash;
    }

    public void setPatientPersonnummerHash(String patientPersonnummerHash) {
        this.patientPersonnummerHash = patientPersonnummerHash;
    }

    public UUID getCareUnitId() {
        return careUnitId;
    }

    public void setCareUnitId(UUID careUnitId) {
        this.careUnitId = careUnitId;
    }

    public String getCareUnitName() {
        return careUnitName;
    }

    public void setCareUnitName(String careUnitName) {
        this.careUnitName = careUnitName;
    }

    public String getCareUnitHsaId() {
        return careUnitHsaId;
    }

    public void setCareUnitHsaId(String careUnitHsaId) {
        this.careUnitHsaId = careUnitHsaId;
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

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public UUID getEncounterId() {
        return encounterId;
    }

    public void setEncounterId(UUID encounterId) {
        this.encounterId = encounterId;
    }

    public String getAccessReason() {
        return accessReason;
    }

    public void setAccessReason(String accessReason) {
        this.accessReason = accessReason;
    }

    public boolean isEmergencyAccess() {
        return emergencyAccess;
    }

    public void setEmergencyAccess(boolean emergencyAccess) {
        this.emergencyAccess = emergencyAccess;
    }

    public UUID getConsentReference() {
        return consentReference;
    }

    public void setConsentReference(UUID consentReference) {
        this.consentReference = consentReference;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    // Alias methods for backwards compatibility with old tests
    public String getUsername() { return userName; }
    public void setUsername(String username) { this.userName = username; }
    public UUID getCareRelationId() { return encounterId; }
    public void setCareRelationId(UUID careRelationId) { this.encounterId = careRelationId; }
    public String getReason() { return accessReason; }
    public void setReason(String reason) { this.accessReason = reason; }
}
