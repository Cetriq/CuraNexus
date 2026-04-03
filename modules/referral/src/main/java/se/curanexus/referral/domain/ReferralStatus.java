package se.curanexus.referral.domain;

/**
 * Status för en remiss.
 */
public enum ReferralStatus {
    /** Utkast - ej skickad */
    DRAFT,

    /** Skickad till mottagare */
    SENT,

    /** Mottagen av mottagare */
    RECEIVED,

    /** Under bedömning */
    UNDER_ASSESSMENT,

    /** Accepterad - patient kommer att kallas */
    ACCEPTED,

    /** Avvisad - skickas tillbaka till remittent */
    REJECTED,

    /** Komplettering begärd */
    PENDING_INFORMATION,

    /** Vidareskickad till annan enhet */
    FORWARDED,

    /** Avslutad - besök genomfört */
    COMPLETED,

    /** Makulerad */
    CANCELLED,

    /** Utgången (ej hanterad inom tidsgräns) */
    EXPIRED
}
