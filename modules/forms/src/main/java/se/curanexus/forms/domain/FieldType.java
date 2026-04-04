package se.curanexus.forms.domain;

/**
 * Types of form fields.
 */
public enum FieldType {
    /** Single line text input */
    TEXT,

    /** Multi-line text area */
    TEXTAREA,

    /** Numeric input */
    NUMBER,

    /** Date picker */
    DATE,

    /** Date and time picker */
    DATETIME,

    /** Time picker */
    TIME,

    /** Yes/No toggle */
    BOOLEAN,

    /** Single choice from options */
    SINGLE_CHOICE,

    /** Multiple choice from options */
    MULTIPLE_CHOICE,

    /** Dropdown select */
    SELECT,

    /** Numeric scale (e.g., 1-10) */
    SCALE,

    /** Visual analog scale */
    VAS,

    /** File upload */
    FILE,

    /** Signature */
    SIGNATURE,

    /** Section header (display only) */
    SECTION,

    /** Informational text (display only) */
    INFO,

    /** Calculated field */
    CALCULATED
}
