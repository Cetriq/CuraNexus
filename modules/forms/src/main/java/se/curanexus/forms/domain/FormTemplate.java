package se.curanexus.forms.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A form template defines the structure of a form that can be filled out.
 * Templates are versioned - when updated, a new version is created.
 */
@Entity
@Table(name = "form_templates")
public class FormTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private FormType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private FormStatus status = FormStatus.DRAFT;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "estimated_duration_minutes")
    private Integer estimatedDurationMinutes;

    @Column(name = "instructions", length = 2000)
    private String instructions;

    @Column(name = "scoring_formula", length = 1000)
    private String scoringFormula;

    @Column(name = "owner_unit_id")
    private UUID ownerUnitId;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<FormField> fields = new ArrayList<>();

    protected FormTemplate() {
    }

    public FormTemplate(String code, String name, FormType type) {
        this.code = code;
        this.name = name;
        this.type = type;
        this.version = 1;
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

    public void addField(FormField field) {
        fields.add(field);
        field.setTemplate(this);
        if (field.getSortOrder() == null) {
            field.setSortOrder(fields.size());
        }
    }

    public void removeField(FormField field) {
        fields.remove(field);
        field.setTemplate(null);
    }

    public void publish() {
        if (this.status == FormStatus.DRAFT) {
            this.status = FormStatus.ACTIVE;
            this.publishedAt = Instant.now();
        }
    }

    public void deprecate() {
        if (this.status == FormStatus.ACTIVE) {
            this.status = FormStatus.DEPRECATED;
        }
    }

    public void archive() {
        this.status = FormStatus.ARCHIVED;
    }

    public FormTemplate createNewVersion() {
        FormTemplate newVersion = new FormTemplate(this.code, this.name, this.type);
        newVersion.setVersion(this.version + 1);
        newVersion.setDescription(this.description);
        newVersion.setCategory(this.category);
        newVersion.setEstimatedDurationMinutes(this.estimatedDurationMinutes);
        newVersion.setInstructions(this.instructions);
        newVersion.setScoringFormula(this.scoringFormula);
        newVersion.setOwnerUnitId(this.ownerUnitId);

        for (FormField field : this.fields) {
            FormField newField = field.copy();
            newVersion.addField(newField);
        }

        return newVersion;
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

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
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

    public FormType getType() {
        return type;
    }

    public void setType(FormType type) {
        this.type = type;
    }

    public FormStatus getStatus() {
        return status;
    }

    public void setStatus(FormStatus status) {
        this.status = status;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getEstimatedDurationMinutes() {
        return estimatedDurationMinutes;
    }

    public void setEstimatedDurationMinutes(Integer estimatedDurationMinutes) {
        this.estimatedDurationMinutes = estimatedDurationMinutes;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getScoringFormula() {
        return scoringFormula;
    }

    public void setScoringFormula(String scoringFormula) {
        this.scoringFormula = scoringFormula;
    }

    public UUID getOwnerUnitId() {
        return ownerUnitId;
    }

    public void setOwnerUnitId(UUID ownerUnitId) {
        this.ownerUnitId = ownerUnitId;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
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

    public List<FormField> getFields() {
        return fields;
    }
}
