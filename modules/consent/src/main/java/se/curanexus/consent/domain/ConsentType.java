package se.curanexus.consent.domain;

/**
 * Types of patient consent.
 */
public enum ConsentType {
    /** General treatment consent */
    TREATMENT,

    /** Consent to share data with other healthcare providers */
    DATA_SHARING,

    /** Consent for specific procedure */
    PROCEDURE,

    /** Consent to participate in research */
    RESEARCH,

    /** Consent to receive digital communications */
    DIGITAL_COMMUNICATION,

    /** Consent to access by relatives/representatives */
    RELATIVE_ACCESS,

    /** National patient overview (NPÖ) opt-out */
    NPO_OPTOUT,

    /** Sammanhållen journalföring (SJF) consent */
    SJF,

    /** Quality register participation */
    QUALITY_REGISTER,

    /** Marketing communications (if applicable) */
    MARKETING,

    /** Emergency access override consent */
    EMERGENCY_ACCESS
}
