package se.curanexus.authorization.abac;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Result of an access control decision with full audit trail.
 * All decisions are logged for compliance with Swedish healthcare regulations.
 */
public record AccessDecision(
        UUID decisionId,
        boolean granted,
        String outcome,
        List<String> reasons,
        List<PolicyEvaluation> evaluatedPolicies,
        AccessContext context,
        Instant timestamp
) {
    public AccessDecision {
        if (decisionId == null) {
            decisionId = UUID.randomUUID();
        }
        if (reasons == null) {
            reasons = new ArrayList<>();
        }
        if (evaluatedPolicies == null) {
            evaluatedPolicies = new ArrayList<>();
        }
        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }

    /**
     * Create a PERMIT decision.
     */
    public static AccessDecision permit(AccessContext context, String reason) {
        return new AccessDecision(
                UUID.randomUUID(),
                true,
                "PERMIT",
                List.of(reason),
                new ArrayList<>(),
                context,
                Instant.now()
        );
    }

    /**
     * Create a DENY decision.
     */
    public static AccessDecision deny(AccessContext context, String reason) {
        return new AccessDecision(
                UUID.randomUUID(),
                false,
                "DENY",
                List.of(reason),
                new ArrayList<>(),
                context,
                Instant.now()
        );
    }

    /**
     * Create a DENY decision with multiple reasons.
     */
    public static AccessDecision deny(AccessContext context, List<String> reasons) {
        return new AccessDecision(
                UUID.randomUUID(),
                false,
                "DENY",
                reasons,
                new ArrayList<>(),
                context,
                Instant.now()
        );
    }

    /**
     * Create an INDETERMINATE decision (could not evaluate).
     */
    public static AccessDecision indeterminate(AccessContext context, String reason) {
        return new AccessDecision(
                UUID.randomUUID(),
                false,
                "INDETERMINATE",
                List.of(reason),
                new ArrayList<>(),
                context,
                Instant.now()
        );
    }

    /**
     * Builder for creating complex decisions.
     */
    public static Builder builder(AccessContext context) {
        return new Builder(context);
    }

    /**
     * Record of a single policy evaluation.
     */
    public record PolicyEvaluation(
            UUID policyId,
            String policyName,
            PolicyType policyType,
            boolean matched,
            String result,
            String reason
    ) {}

    public static class Builder {
        private final AccessContext context;
        private boolean granted = false;
        private String outcome = "DENY";
        private final List<String> reasons = new ArrayList<>();
        private final List<PolicyEvaluation> evaluatedPolicies = new ArrayList<>();

        public Builder(AccessContext context) {
            this.context = context;
        }

        public Builder permit() {
            this.granted = true;
            this.outcome = "PERMIT";
            return this;
        }

        public Builder deny() {
            this.granted = false;
            this.outcome = "DENY";
            return this;
        }

        public Builder reason(String reason) {
            this.reasons.add(reason);
            return this;
        }

        public Builder policyEvaluation(PolicyEvaluation evaluation) {
            this.evaluatedPolicies.add(evaluation);
            return this;
        }

        public AccessDecision build() {
            return new AccessDecision(
                    UUID.randomUUID(),
                    granted,
                    outcome,
                    reasons,
                    evaluatedPolicies,
                    context,
                    Instant.now()
            );
        }
    }
}
