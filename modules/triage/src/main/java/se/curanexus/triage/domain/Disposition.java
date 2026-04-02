package se.curanexus.triage.domain;

/**
 * Disposition decision after triage assessment.
 */
public enum Disposition {
    ADMIT,
    OBSERVE,
    TREAT_AND_RELEASE,
    REFER,
    DISCHARGE,
    TRANSFER,
    LEFT_WITHOUT_BEING_SEEN
}
