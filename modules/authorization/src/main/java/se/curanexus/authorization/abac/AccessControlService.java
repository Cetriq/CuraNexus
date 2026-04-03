package se.curanexus.authorization.abac;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.curanexus.authorization.domain.ActionType;
import se.curanexus.authorization.domain.ResourceType;
import se.curanexus.authorization.service.AccessDeniedException;

import java.util.UUID;

/**
 * Main service for ABAC-based access control.
 * Coordinates policy evaluation and audit logging.
 *
 * This service implements Zero Trust Access according to Swedish healthcare requirements:
 * - All access requires context (care relation, encounter)
 * - All access decisions are logged
 * - Emergency access (nödåtkomst) is supported but specially logged
 */
@Service
@Transactional
public class AccessControlService {

    private static final Logger log = LoggerFactory.getLogger(AccessControlService.class);

    private final PolicyEvaluator policyEvaluator;
    private final AccessPolicyRepository policyRepository;
    private final AccessAuditLogRepository auditLogRepository;

    public AccessControlService(
            PolicyEvaluator policyEvaluator,
            AccessPolicyRepository policyRepository,
            AccessAuditLogRepository auditLogRepository) {
        this.policyEvaluator = policyEvaluator;
        this.policyRepository = policyRepository;
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Check access and log the decision.
     * Returns the decision without throwing exceptions.
     */
    public AccessDecision checkAccess(AccessContext context) {
        AccessDecision decision = policyEvaluator.evaluate(context);

        // Log all access decisions
        logAccessDecision(decision);

        return decision;
    }

    /**
     * Require access - throws exception if denied.
     */
    public void requireAccess(AccessContext context) {
        AccessDecision decision = checkAccess(context);

        if (!decision.granted()) {
            throw new AccessDeniedException(
                    context.userId(),
                    context.resourceType() != null ? context.resourceType().name() : "UNKNOWN",
                    context.action() != null ? context.action().name() : "UNKNOWN"
            );
        }
    }

    /**
     * Quick check for patient access (performance optimized).
     */
    @Transactional(readOnly = true)
    public boolean canAccessPatient(UUID userId, UUID patientId) {
        return policyEvaluator.canAccessPatient(userId, patientId);
    }

    /**
     * Check access for reading patient data.
     */
    public AccessDecision checkPatientReadAccess(UUID userId, UUID patientId, UUID encounterId) {
        AccessContext context = AccessContext.builder()
                .userId(userId)
                .patientId(patientId)
                .encounterId(encounterId)
                .resourceType(ResourceType.PATIENT)
                .action(ActionType.READ)
                .build();

        return checkAccess(context);
    }

    /**
     * Check access for reading encounter data.
     */
    public AccessDecision checkEncounterAccess(UUID userId, UUID encounterId, UUID patientId, ActionType action) {
        AccessContext context = AccessContext.builder()
                .userId(userId)
                .encounterId(encounterId)
                .patientId(patientId)
                .resourceType(ResourceType.ENCOUNTER)
                .action(action)
                .build();

        return checkAccess(context);
    }

    /**
     * Check access for journal notes.
     */
    public AccessDecision checkNoteAccess(UUID userId, UUID noteId, UUID patientId, UUID encounterId, ActionType action) {
        AccessContext context = AccessContext.builder()
                .userId(userId)
                .resourceType(ResourceType.NOTE)
                .resourceId(noteId)
                .patientId(patientId)
                .encounterId(encounterId)
                .action(action)
                .build();

        return checkAccess(context);
    }

    /**
     * Check emergency access (nödåtkomst).
     * Requires explicit reason and is specially logged.
     */
    public AccessDecision checkEmergencyAccess(UUID userId, UUID patientId, String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            return AccessDecision.deny(
                    AccessContext.builder().userId(userId).patientId(patientId).build(),
                    "Emergency access requires a reason"
            );
        }

        AccessContext context = AccessContext.builder()
                .userId(userId)
                .patientId(patientId)
                .resourceType(ResourceType.PATIENT)
                .action(ActionType.READ)
                .emergencyAccess(true)
                .accessReason(reason)
                .build();

        log.warn("Emergency access requested by user {} for patient {} with reason: {}",
                userId, patientId, reason);

        return checkAccess(context);
    }

    /**
     * Log access decision to audit trail.
     */
    private void logAccessDecision(AccessDecision decision) {
        try {
            AccessAuditLog auditLog = AccessAuditLog.fromDecision(decision);
            auditLogRepository.save(auditLog);

            if (decision.context().isEmergencyAccess()) {
                log.warn("EMERGENCY ACCESS: User {} accessed patient {} - Reason: {}",
                        decision.context().userId(),
                        decision.context().patientId(),
                        decision.context().getAccessReason().orElse("No reason provided"));
            } else if (!decision.granted()) {
                log.info("ACCESS DENIED: User {} attempted {} on {} - Reason: {}",
                        decision.context().userId(),
                        decision.context().action(),
                        decision.context().resourceType(),
                        String.join("; ", decision.reasons()));
            }
        } catch (Exception e) {
            // Audit logging should not fail the access check
            log.error("Failed to log access decision: {}", e.getMessage(), e);
        }
    }

    // ========== Policy Management ==========

    /**
     * Create a new access policy.
     */
    public AccessPolicy createPolicy(String name, PolicyType policyType, String description) {
        if (policyRepository.existsByName(name)) {
            throw new IllegalArgumentException("Policy with name already exists: " + name);
        }

        AccessPolicy policy = new AccessPolicy(name, policyType);
        policy.setDescription(description);
        return policyRepository.save(policy);
    }

    /**
     * Get policy by name.
     */
    @Transactional(readOnly = true)
    public AccessPolicy getPolicy(String name) {
        return policyRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Policy not found: " + name));
    }

    /**
     * Update policy.
     */
    public AccessPolicy updatePolicy(UUID policyId, AccessPolicy updates) {
        AccessPolicy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new IllegalArgumentException("Policy not found: " + policyId));

        if (updates.getDescription() != null) {
            policy.setDescription(updates.getDescription());
        }
        if (updates.getResourceType() != null) {
            policy.setResourceType(updates.getResourceType());
        }
        if (updates.getActionType() != null) {
            policy.setActionType(updates.getActionType());
        }
        if (updates.getRequiredRole() != null) {
            policy.setRequiredRole(updates.getRequiredRole());
        }
        if (updates.getRequiredPermission() != null) {
            policy.setRequiredPermission(updates.getRequiredPermission());
        }
        policy.setRequireCareRelation(updates.isRequireCareRelation());
        policy.setRequireEncounterContext(updates.isRequireEncounterContext());
        if (updates.getAllowedUserTypes() != null) {
            policy.setAllowedUserTypes(updates.getAllowedUserTypes());
        }
        if (updates.getAllowedDepartments() != null) {
            policy.setAllowedDepartments(updates.getAllowedDepartments());
        }
        policy.setPriority(updates.getPriority());
        policy.setActive(updates.isActive());

        return policyRepository.save(policy);
    }

    /**
     * Deactivate a policy.
     */
    public void deactivatePolicy(UUID policyId) {
        AccessPolicy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new IllegalArgumentException("Policy not found: " + policyId));
        policy.setActive(false);
        policyRepository.save(policy);
    }
}
