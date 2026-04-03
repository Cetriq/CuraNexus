package se.curanexus.lab.domain;

/**
 * Typ av provmaterial.
 */
public enum SpecimenType {
    /** Blod - venöst */
    BLOOD_VENOUS,

    /** Blod - kapillärt */
    BLOOD_CAPILLARY,

    /** Blod - arteriellt */
    BLOOD_ARTERIAL,

    /** Urin */
    URINE,

    /** Urin - dygnssamling */
    URINE_24H,

    /** Avföring */
    STOOL,

    /** Sputum */
    SPUTUM,

    /** Saliv */
    SALIVA,

    /** Cerebrospinalvätska */
    CSF,

    /** Ledvätska */
    SYNOVIAL_FLUID,

    /** Pleuravätska */
    PLEURAL_FLUID,

    /** Ascites */
    ASCITES,

    /** Biopsi */
    BIOPSY,

    /** Svabb/odling */
    SWAB,

    /** Hår */
    HAIR,

    /** Nagel */
    NAIL,

    /** Annat */
    OTHER
}
