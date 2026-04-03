package se.curanexus.authorization.abac;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import se.curanexus.authorization.domain.User;
import se.curanexus.authorization.repository.CareRelationRepository;
import se.curanexus.authorization.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Evaluates access policies against an access context.
 * Implements ABAC (Attribute-Based Access Control) for Swedish healthcare requirements.
 *
 * Policy evaluation order:
 * 1. DENY policies are evaluated first (deny takes precedence)
 * 2. REQUIRE_CONTEXT policies check for required attributes
 * 3. PERMIT policies grant access
 * 4. Default is DENY if no PERMIT policy matches
 */
@Component
public class PolicyEvaluator {

    private static final Logger log = LoggerFactory.getLogger(PolicyEvaluator.class);

    private final AccessPolicyRepository policyRepository;
    private final UserRepository userRepository;
    private final CareRelationRepository careRelationRepository;

    public PolicyEvaluator(
            AccessPolicyRepository policyRepository,
            UserRepository userRepository,
            CareRelationRepository careRelationRepository) {
        this.policyRepository = policyRepository;
        this.userRepository = userRepository;
        this.careRelationRepository = careRelationRepository;
    }

    /**
     * Evaluate access for the given context.
     */
    public AccessDecision evaluate(AccessContext context) {
        log.debug("Evaluating access for user {} on resource {} with action {}",
                context.userId(), context.resourceType(), context.action());

        AccessDecision.Builder decisionBuilder = AccessDecision.builder(context);
        List<AccessPolicy> applicablePolicies = getApplicablePolicies(context);

        if (applicablePolicies.isEmpty()) {
            log.debug("No applicable policies found, using default deny");
            return decisionBuilder
                    .deny()
                    .reason("No applicable policies found")
                    .build();
        }

        // Load user for policy evaluation
        Optional<User> userOpt = userRepository.findByIdWithRolesAndPermissions(context.userId());
        if (userOpt.isEmpty()) {
            return decisionBuilder
                    .deny()
                    .reason("User not found")
                    .build();
        }
        User user = userOpt.get();

        // Check if user is active
        if (!user.isActive()) {
            return decisionBuilder
                    .deny()
                    .reason("User is not active")
                    .build();
        }

        // Evaluate policies in priority order
        List<String> denyReasons = new ArrayList<>();
        List<String> contextViolations = new ArrayList<>();
        boolean hasPermit = false;

        for (AccessPolicy policy : applicablePolicies) {
            PolicyEvaluationResult result = evaluatePolicy(policy, context, user);

            decisionBuilder.policyEvaluation(new AccessDecision.PolicyEvaluation(
                    policy.getId(),
                    policy.getName(),
                    policy.getPolicyType(),
                    result.matched(),
                    result.outcome(),
                    result.reason()
            ));

            if (!result.matched()) {
                continue;
            }

            switch (policy.getPolicyType()) {
                case DENY:
                    // Deny takes immediate precedence
                    return decisionBuilder
                            .deny()
                            .reason(result.reason())
                            .build();

                case REQUIRE_CONTEXT:
                    if (!result.conditionsMet()) {
                        contextViolations.add(result.reason());
                    }
                    break;

                case PERMIT:
                case EMERGENCY_OVERRIDE:
                    if (result.conditionsMet()) {
                        hasPermit = true;
                        decisionBuilder.reason(result.reason());
                    }
                    break;
            }
        }

        // Check context violations
        if (!contextViolations.isEmpty()) {
            return decisionBuilder
                    .deny()
                    .reason(String.join("; ", contextViolations))
                    .build();
        }

        // Final decision
        if (hasPermit) {
            return decisionBuilder
                    .permit()
                    .build();
        } else {
            return decisionBuilder
                    .deny()
                    .reason("No matching permit policy")
                    .build();
        }
    }

    /**
     * Get policies applicable to the given context.
     */
    private List<AccessPolicy> getApplicablePolicies(AccessContext context) {
        List<AccessPolicy> policies;

        if (context.resourceType() != null && context.action() != null) {
            policies = policyRepository.findByResourceAndAction(
                    context.resourceType(), context.action());
        } else if (context.resourceType() != null) {
            policies = policyRepository.findByResourceType(context.resourceType());
        } else {
            policies = policyRepository.findAllActive();
        }

        // Filter to only applicable policies and sort by priority
        return policies.stream()
                .filter(p -> p.appliesTo(context))
                .sorted(Comparator.comparingInt(AccessPolicy::getPriority).reversed())
                .toList();
    }

    /**
     * Evaluate a single policy against the context.
     */
    private PolicyEvaluationResult evaluatePolicy(AccessPolicy policy, AccessContext context, User user) {
        List<String> violations = new ArrayList<>();

        // Check required role
        if (policy.getRequiredRole() != null && !policy.getRequiredRole().isEmpty()) {
            if (!user.hasRole(policy.getRequiredRole())) {
                violations.add("Missing required role: " + policy.getRequiredRole());
            }
        }

        // Check required permission
        if (policy.getRequiredPermission() != null && !policy.getRequiredPermission().isEmpty()) {
            if (!user.hasPermission(policy.getRequiredPermission())) {
                violations.add("Missing required permission: " + policy.getRequiredPermission());
            }
        }

        // Check care relation requirement
        if (policy.isRequireCareRelation() && context.patientId() != null) {
            boolean hasCareRelation = careRelationRepository.hasActiveCareRelation(
                    context.userId(), context.patientId(), LocalDateTime.now());

            if (!hasCareRelation) {
                // Check for emergency access
                if (context.isEmergencyAccess() && policy.getPolicyType() == PolicyType.EMERGENCY_OVERRIDE) {
                    // Emergency access allowed - will be logged
                    log.warn("Emergency access granted for user {} to patient {}",
                            context.userId(), context.patientId());
                } else {
                    violations.add("No active care relation with patient");
                }
            }
        }

        // Check encounter context requirement
        if (policy.isRequireEncounterContext() && !context.hasEncounterContext()) {
            violations.add("Encounter context required");
        }

        // Check allowed user types
        if (policy.getAllowedUserTypes() != null && !policy.getAllowedUserTypes().isEmpty()) {
            Set<String> allowedTypes = Set.of(policy.getAllowedUserTypes().split(","));
            if (context.userType() == null || !allowedTypes.contains(context.userType().trim())) {
                violations.add("User type not allowed: " + context.userType());
            }
        }

        // Check allowed departments
        if (policy.getAllowedDepartments() != null && !policy.getAllowedDepartments().isEmpty()) {
            Set<String> allowedDepts = Set.of(policy.getAllowedDepartments().split(","));
            if (context.department() == null || !allowedDepts.contains(context.department().trim())) {
                violations.add("Department not allowed: " + context.department());
            }
        }

        if (violations.isEmpty()) {
            return new PolicyEvaluationResult(true, true, "PERMIT",
                    "Policy " + policy.getName() + " permits access");
        } else {
            return new PolicyEvaluationResult(true, false, "DENY",
                    String.join("; ", violations));
        }
    }

    /**
     * Quick check if user can access patient (for performance-critical paths).
     */
    public boolean canAccessPatient(UUID userId, UUID patientId) {
        // Check if user has PATIENT_READ permission
        Optional<User> userOpt = userRepository.findByIdWithRolesAndPermissions(userId);
        if (userOpt.isEmpty() || !userOpt.get().isActive()) {
            return false;
        }

        User user = userOpt.get();
        if (!user.hasPermission("PATIENT_READ")) {
            return false;
        }

        // Check care relation
        return careRelationRepository.hasActiveCareRelation(userId, patientId, LocalDateTime.now());
    }

    private record PolicyEvaluationResult(
            boolean matched,
            boolean conditionsMet,
            String outcome,
            String reason
    ) {}
}
