package se.curanexus.authorization.api.dto;

import se.curanexus.authorization.abac.AccessDecision;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Response for ABAC access decision.
 */
public record AccessDecisionResponse(
        UUID decisionId,
        boolean granted,
        String outcome,
        List<String> reasons,
        List<PolicyEvaluationResult> evaluatedPolicies,
        Instant timestamp
) {
    public record PolicyEvaluationResult(
            UUID policyId,
            String policyName,
            String policyType,
            boolean matched,
            String result,
            String reason
    ) {}

    public static AccessDecisionResponse from(AccessDecision decision) {
        List<PolicyEvaluationResult> policies = decision.evaluatedPolicies().stream()
                .map(pe -> new PolicyEvaluationResult(
                        pe.policyId(),
                        pe.policyName(),
                        pe.policyType().name(),
                        pe.matched(),
                        pe.result(),
                        pe.reason()
                ))
                .toList();

        return new AccessDecisionResponse(
                decision.decisionId(),
                decision.granted(),
                decision.outcome(),
                decision.reasons(),
                policies,
                decision.timestamp()
        );
    }
}
