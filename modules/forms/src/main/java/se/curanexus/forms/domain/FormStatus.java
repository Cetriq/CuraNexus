package se.curanexus.forms.domain;

/**
 * Status of a form template.
 */
public enum FormStatus {
    /** Form is being drafted */
    DRAFT,

    /** Form is active and can be used */
    ACTIVE,

    /** Form is deprecated but existing submissions are kept */
    DEPRECATED,

    /** Form is archived and no longer visible */
    ARCHIVED
}
