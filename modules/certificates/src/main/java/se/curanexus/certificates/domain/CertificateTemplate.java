package se.curanexus.certificates.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * A template defining the structure of a certificate type.
 */
@Entity
@Table(name = "certificate_templates")
public class CertificateTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private CertificateType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TemplateStatus status = TemplateStatus.DRAFT;

    @Column(name = "version", nullable = false)
    private Integer version = 1;

    /** JSON schema for certificate data fields */
    @Column(name = "data_schema", columnDefinition = "TEXT")
    private String dataSchema;

    /** HTML/PDF template for rendering */
    @Column(name = "render_template", columnDefinition = "TEXT")
    private String renderTemplate;

    /** Recipient system (e.g., INTYGSTJANSTEN, FORSAKRINGSKASSAN) */
    @Column(name = "recipient_system", length = 50)
    private String recipientSystem;

    /** Whether this certificate requires electronic signature */
    @Column(name = "requires_signature", nullable = false)
    private boolean requiresSignature = true;

    /** Validity period in days (null = no expiry) */
    @Column(name = "validity_days")
    private Integer validityDays;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    protected CertificateTemplate() {
    }

    public CertificateTemplate(String code, String name, CertificateType type) {
        this.code = code;
        this.name = name;
        this.type = type;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public void publish() {
        if (this.status == TemplateStatus.DRAFT) {
            this.status = TemplateStatus.ACTIVE;
            this.publishedAt = Instant.now();
        }
    }

    public void deprecate() {
        if (this.status == TemplateStatus.ACTIVE) {
            this.status = TemplateStatus.DEPRECATED;
        }
    }

    public void archive() {
        this.status = TemplateStatus.ARCHIVED;
    }

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public CertificateType getType() {
        return type;
    }

    public void setType(CertificateType type) {
        this.type = type;
    }

    public TemplateStatus getStatus() {
        return status;
    }

    public void setStatus(TemplateStatus status) {
        this.status = status;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getDataSchema() {
        return dataSchema;
    }

    public void setDataSchema(String dataSchema) {
        this.dataSchema = dataSchema;
    }

    public String getRenderTemplate() {
        return renderTemplate;
    }

    public void setRenderTemplate(String renderTemplate) {
        this.renderTemplate = renderTemplate;
    }

    public String getRecipientSystem() {
        return recipientSystem;
    }

    public void setRecipientSystem(String recipientSystem) {
        this.recipientSystem = recipientSystem;
    }

    public boolean isRequiresSignature() {
        return requiresSignature;
    }

    public void setRequiresSignature(boolean requiresSignature) {
        this.requiresSignature = requiresSignature;
    }

    public Integer getValidityDays() {
        return validityDays;
    }

    public void setValidityDays(Integer validityDays) {
        this.validityDays = validityDays;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }
}
