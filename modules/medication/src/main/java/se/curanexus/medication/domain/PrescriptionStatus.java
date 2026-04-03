package se.curanexus.medication.domain;

/**
 * Status för en ordination/recept.
 */
public enum PrescriptionStatus {
    /** Ordination skapad men ej aktiverad */
    DRAFT,

    /** Aktiv ordination */
    ACTIVE,

    /** Tillfälligt pausad */
    ON_HOLD,

    /** Avslutad normalt (behandling klar) */
    COMPLETED,

    /** Avbruten i förtid */
    CANCELLED,

    /** Ersatt av ny ordination */
    SUPERSEDED,

    /** Felaktigt inlagd, ej utförd */
    ENTERED_IN_ERROR
}
