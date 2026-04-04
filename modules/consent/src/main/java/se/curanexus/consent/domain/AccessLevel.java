package se.curanexus.consent.domain;

/**
 * Access levels for patient data.
 */
public enum AccessLevel {
    /** No access */
    NONE,

    /** Read-only access */
    READ,

    /** Read and write access */
    WRITE,

    /** Full access including delegation */
    FULL
}
