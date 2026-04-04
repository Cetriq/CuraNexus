package se.curanexus.consent.domain;

/**
 * Status of a consent record.
 */
public enum ConsentStatus {
    /** Consent is active and valid */
    ACTIVE,

    /** Consent has been withdrawn */
    WITHDRAWN,

    /** Consent has expired */
    EXPIRED,

    /** Consent is pending patient confirmation */
    PENDING,

    /** Consent was rejected by patient */
    REJECTED
}
