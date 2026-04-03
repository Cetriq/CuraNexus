package se.curanexus.medication.domain;

/**
 * Status för läkemedelsadministrering.
 */
public enum AdministrationStatus {
    /** Planerad men ej utförd */
    PLANNED,

    /** Pågår (t.ex. infusion) */
    IN_PROGRESS,

    /** Utförd */
    COMPLETED,

    /** Ej given (missad dos) */
    NOT_DONE,

    /** Avbruten under pågående administrering */
    STOPPED,

    /** Felaktigt registrerad */
    ENTERED_IN_ERROR
}
