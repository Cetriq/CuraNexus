package se.curanexus.referral.domain;

/**
 * Prioritet för remiss.
 * Baserat på svenska vårdgarantin och medicinsk angelägenhetsgrad.
 */
public enum ReferralPriority {
    /** Akut - ska bedömas inom 24 timmar */
    IMMEDIATE,

    /** Mycket brådskande - ska bedömas inom 1 vecka */
    URGENT,

    /** Brådskande - ska bedömas inom 2 veckor */
    SEMI_URGENT,

    /** Normal prioritet - vårdgaranti (max 90 dagar till besök) */
    ROUTINE,

    /** Elektiv - planerad vård utan tidskrav */
    ELECTIVE
}
