package se.curanexus.lab.domain;

/**
 * Prioritet för labbeställning.
 */
public enum LabOrderPriority {
    /** Akut - omedelbar analys krävs */
    STAT,

    /** Rutinakut - samma dag */
    URGENT,

    /** Rutin - normal hantering */
    ROUTINE,

    /** Uppföljning - kan vänta */
    FOLLOW_UP
}
