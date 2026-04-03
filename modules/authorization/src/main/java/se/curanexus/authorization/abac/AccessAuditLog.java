package se.curanexus.authorization.abac;

import jakarta.persistence.*;
import se.curanexus.authorization.domain.ActionType;
import se.curanexus.authorization.domain.ResourceType;

import java.time.Instant;
import java.util.UUID;

/**
 * Audit log entry for access decisions.
 * All access attempts (both granted and denied) are logged for compliance.
 *
 * According to Swedish PDL (Patientdatalagen):
 * - All access to patient data must be logged
 * - Logs must include: who, what, when, outcome
 * - Emergency access must be specially marked
 */
@Entity
@Table(name = "access_audit_log", indexes = {
        @Index(name = "idx_audit_user", columnList = "user_id"),
        @Index(name = "idx_audit_patient", columnList = "patient_id"),
        @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
        @Index(name = "idx_audit_outcome", columnList = "outcome"),
        @Index(name = "idx_audit_emergency", columnList = "emergency_access")
})
public class AccessAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "decision_id", nullable = false)
    private UUID decisionId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "user_type", length = 50)
    private String userType;

    @Column(name = "department", length = 100)
    private String department;

    @Column(name = "patient_id")
    private UUID patientId;

    @Column(name = "encounter_id")
    private UUID encounterId;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", length = 30)
    private ResourceType resourceType;

    @Column(name = "resource_id")
    private UUID resourceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", length = 30)
    private ActionType actionType;

    @Column(name = "outcome", nullable = false, length = 20)
    private String outcome;

    @Column(name = "granted", nullable = false)
    private boolean granted;

    @Column(name = "reason", length = 1000)
    private String reason;

    @Column(name = "emergency_access")
    private boolean emergencyAccess;

    @Column(name = "access_reason", length = 500)
    private String accessReason;

    @Column(name = "client_ip", length = 50)
    private String clientIp;

    @Column(name = "client_application", length = 100)
    private String clientApplication;

    @Column(name = "policies_evaluated", length = 1000)
    private String policiesEvaluated;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    protected AccessAuditLog() {
    }

    /**
     * Create audit log from access decision.
     */
    public static AccessAuditLog fromDecision(AccessDecision decision) {
        AccessAuditLog log = new AccessAuditLog();
        AccessContext ctx = decision.context();

        log.decisionId = decision.decisionId();
        log.userId = ctx.userId();
        log.username = ctx.username();
        log.userType = ctx.userType();
        log.department = ctx.department();
        log.patientId = ctx.patientId();
        log.encounterId = ctx.encounterId();
        log.resourceType = ctx.resourceType();
        log.resourceId = ctx.resourceId();
        log.actionType = ctx.action();
        log.outcome = decision.outcome();
        log.granted = decision.granted();
        log.reason = String.join("; ", decision.reasons());
        log.emergencyAccess = ctx.isEmergencyAccess();
        log.accessReason = ctx.getAccessReason().orElse(null);
        log.clientIp = ctx.clientIp();
        log.clientApplication = ctx.clientApplication();
        log.timestamp = decision.timestamp();

        // Record evaluated policies
        if (!decision.evaluatedPolicies().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (AccessDecision.PolicyEvaluation pe : decision.evaluatedPolicies()) {
                if (!sb.isEmpty()) sb.append("; ");
                sb.append(pe.policyName()).append(":").append(pe.result());
            }
            log.policiesEvaluated = sb.toString();
        }

        return log;
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public UUID getDecisionId() {
        return decisionId;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getUserType() {
        return userType;
    }

    public String getDepartment() {
        return department;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public UUID getEncounterId() {
        return encounterId;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public UUID getResourceId() {
        return resourceId;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public String getOutcome() {
        return outcome;
    }

    public boolean isGranted() {
        return granted;
    }

    public String getReason() {
        return reason;
    }

    public boolean isEmergencyAccess() {
        return emergencyAccess;
    }

    public String getAccessReason() {
        return accessReason;
    }

    public String getClientIp() {
        return clientIp;
    }

    public String getClientApplication() {
        return clientApplication;
    }

    public String getPoliciesEvaluated() {
        return policiesEvaluated;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
