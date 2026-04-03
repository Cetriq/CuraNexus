package se.curanexus.booking.domain;

/**
 * Typ av bokad tid.
 */
public enum AppointmentType {
    /** Fysiskt mottagningsbesök */
    IN_PERSON,

    /** Videobesök */
    VIDEO,

    /** Telefonbesök */
    PHONE,

    /** Hembesök */
    HOME_VISIT,

    /** Akutbesök (walk-in) */
    WALK_IN,

    /** Gruppbesök */
    GROUP,

    /** Provtagning */
    LAB,

    /** Röntgen/undersökning */
    IMAGING
}
