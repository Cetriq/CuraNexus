package se.curanexus.lab.domain;

/**
 * Status för labbeställning.
 */
public enum LabOrderStatus {
    /** Utkast - ej skickad */
    DRAFT,

    /** Beställd - skickad till lab */
    ORDERED,

    /** Mottagen av lab */
    RECEIVED,

    /** Prov taget */
    SPECIMEN_COLLECTED,

    /** Under analys */
    IN_PROGRESS,

    /** Delresultat tillgängligt */
    PARTIAL_RESULTS,

    /** Komplett - alla resultat klara */
    COMPLETED,

    /** Makulerad */
    CANCELLED,

    /** Avvisad av lab */
    REJECTED
}
