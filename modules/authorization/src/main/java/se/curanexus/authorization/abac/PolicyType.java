package se.curanexus.authorization.abac;

/**
 * Types of access policies.
 */
public enum PolicyType {
    /**
     * Grant access when conditions are met.
     */
    PERMIT,

    /**
     * Deny access when conditions are met (takes precedence over PERMIT).
     */
    DENY,

    /**
     * Require specific context attributes to be present.
     */
    REQUIRE_CONTEXT,

    /**
     * Emergency access override (nödåtkomst) - requires explicit reason.
     */
    EMERGENCY_OVERRIDE
}
