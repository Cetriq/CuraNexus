package se.curanexus.consent.domain;

/**
 * Types of access blocks that patients can configure.
 */
public enum AccessBlockType {
    /** Block specific unit from accessing data */
    UNIT,

    /** Block specific practitioner from accessing data */
    PRACTITIONER,

    /** Block specific category of data */
    DATA_CATEGORY,

    /** Block access from all external units */
    EXTERNAL_UNITS,

    /** Emergency break-glass override allowed */
    EMERGENCY_OVERRIDE
}
