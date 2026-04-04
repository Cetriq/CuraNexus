package se.curanexus.forms.domain;

/**
 * Types of forms in the healthcare system.
 */
public enum FormType {
    /** Medical history questionnaire */
    ANAMNESIS,

    /** Screening questionnaire (e.g., depression, anxiety) */
    SCREENING,

    /** Patient-reported outcome measures */
    PROM,

    /** Patient-reported experience measures */
    PREM,

    /** Consent form */
    CONSENT,

    /** Risk assessment */
    RISK_ASSESSMENT,

    /** Triage questionnaire */
    TRIAGE,

    /** General questionnaire */
    QUESTIONNAIRE,

    /** Checklist */
    CHECKLIST,

    /** Custom form */
    CUSTOM
}
