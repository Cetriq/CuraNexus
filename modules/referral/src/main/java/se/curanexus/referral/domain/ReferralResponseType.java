package se.curanexus.referral.domain;

/**
 * Typ av remissvar.
 */
public enum ReferralResponseType {
    /** Remissen accepterad */
    ACCEPTANCE,

    /** Remissen avvisad */
    REJECTION,

    /** Komplettering begärd */
    INFORMATION_REQUEST,

    /** Komplettering mottagen */
    INFORMATION_PROVIDED,

    /** Remissen vidareskickad */
    FORWARDED,

    /** Preliminär bedömning */
    PRELIMINARY_ASSESSMENT,

    /** Slutgiltigt svar/epikris */
    FINAL_RESPONSE,

    /** Notering/kommentar */
    NOTE
}
