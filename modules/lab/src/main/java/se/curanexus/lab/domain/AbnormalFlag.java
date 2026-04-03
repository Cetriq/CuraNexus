package se.curanexus.lab.domain;

/**
 * Flagga för avvikande resultat.
 */
public enum AbnormalFlag {
    /** Normalt värde */
    NORMAL,

    /** Lågt värde */
    LOW,

    /** Högt värde */
    HIGH,

    /** Kritiskt lågt - kräver omedelbar åtgärd */
    CRITICAL_LOW,

    /** Kritiskt högt - kräver omedelbar åtgärd */
    CRITICAL_HIGH,

    /** Positivt (för kvalitativa tester) */
    POSITIVE,

    /** Negativt (för kvalitativa tester) */
    NEGATIVE,

    /** Abnormt (ospecificerat) */
    ABNORMAL
}
