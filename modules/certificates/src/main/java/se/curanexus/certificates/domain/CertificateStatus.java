package se.curanexus.certificates.domain;

/**
 * Status of a certificate.
 */
public enum CertificateStatus {
    /** Draft - not yet signed */
    DRAFT,

    /** Signed but not sent */
    SIGNED,

    /** Sent to recipient (e.g., Försäkringskassan) */
    SENT,

    /** Revoked/cancelled */
    REVOKED,

    /** Replaced by newer version */
    REPLACED,

    /** Archived */
    ARCHIVED
}
