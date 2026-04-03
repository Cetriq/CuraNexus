package se.curanexus.referral.domain;

/**
 * Typ av remiss.
 */
public enum ReferralType {
    /** Konsultationsremiss - begäran om råd */
    CONSULTATION,

    /** Behandlingsremiss - övertagande av vård */
    TREATMENT,

    /** Utredningsremiss - begäran om utredning */
    INVESTIGATION,

    /** Röntgenremiss */
    RADIOLOGY,

    /** Labremiss */
    LABORATORY,

    /** Fysioterapiremiss */
    PHYSIOTHERAPY,

    /** Specialistremiss */
    SPECIALIST,

    /** Internremiss (inom samma organisation) */
    INTERNAL,

    /** Egenremiss (patient remitterar sig själv) */
    SELF_REFERRAL
}
