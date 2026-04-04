package se.curanexus.certificates.domain;

/**
 * Status of a certificate template.
 */
public enum TemplateStatus {
    /** Draft - being developed */
    DRAFT,

    /** Active - available for use */
    ACTIVE,

    /** Deprecated - should not be used for new certificates */
    DEPRECATED,

    /** Archived - no longer available */
    ARCHIVED
}
