package se.curanexus.forms.domain;

/**
 * Status of a form submission.
 */
public enum SubmissionStatus {
    /** Submission started but not completed */
    IN_PROGRESS,

    /** Submission completed by patient/user */
    COMPLETED,

    /** Submission reviewed by healthcare professional */
    REVIEWED,

    /** Submission cancelled or invalidated */
    CANCELLED,

    /** Submission expired (not completed in time) */
    EXPIRED
}
