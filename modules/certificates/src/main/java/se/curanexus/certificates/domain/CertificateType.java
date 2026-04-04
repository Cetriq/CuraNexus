package se.curanexus.certificates.domain;

/**
 * Types of medical certificates.
 */
public enum CertificateType {
    /** Sjukintyg - Sick leave certificate */
    SICK_LEAVE,

    /** Läkarintyg för sjukpenning (FK 7804) */
    FK_7804,

    /** Läkarutlåtande för aktivitetsersättning (FK 7800) */
    FK_7800,

    /** Dödsbevis */
    DEATH_CERTIFICATE,

    /** Dödsorsaksintyg */
    DEATH_CAUSE,

    /** Läkarintyg för körkort */
    DRIVING_LICENSE,

    /** Läkarintyg för vapen */
    WEAPONS_PERMIT,

    /** Vaccinationsintyg */
    VACCINATION,

    /** Hälsointyg */
    HEALTH,

    /** Graviditetsintyg */
    PREGNANCY,

    /** Intyg för skola/förskola */
    SCHOOL,

    /** Intyg om arbetsförmåga */
    WORK_CAPACITY,

    /** Övrigt läkarintyg */
    OTHER
}
