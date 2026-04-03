package se.curanexus.booking.domain;

/**
 * Status för en bokad tid.
 */
public enum AppointmentStatus {
    /** Bokningen är bekräftad och väntar på genomförande */
    BOOKED,

    /** Patienten har anlänt/checkat in */
    CHECKED_IN,

    /** Besöket pågår */
    IN_PROGRESS,

    /** Besöket är genomfört */
    COMPLETED,

    /** Patienten uteblev utan avbokning */
    NO_SHOW,

    /** Bokningen är avbokad (generell) */
    CANCELLED,

    /** Bokningen är avbokad av patient */
    CANCELLED_BY_PATIENT,

    /** Bokningen är avbokad av vårdgivare */
    CANCELLED_BY_PROVIDER,

    /** Bokningen har ombokats till annan tid */
    RESCHEDULED
}
