package se.curanexus.forms.domain;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * A field within a form template.
 * Fields define what data to collect and validation rules.
 */
@Entity
@Table(name = "form_fields")
public class FormField {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private FormTemplate template;

    @Column(name = "field_key", nullable = false, length = 100)
    private String fieldKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "field_type", nullable = false, length = 30)
    private FieldType fieldType;

    @Column(name = "label", nullable = false, length = 500)
    private String label;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "placeholder", length = 200)
    private String placeholder;

    @Column(name = "help_text", length = 500)
    private String helpText;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "required", nullable = false)
    private boolean required = false;

    @Column(name = "read_only", nullable = false)
    private boolean readOnly = false;

    @Column(name = "hidden", nullable = false)
    private boolean hidden = false;

    @Column(name = "default_value", length = 500)
    private String defaultValue;

    /** JSON array of options for choice fields */
    @Column(name = "options", columnDefinition = "TEXT")
    private String options;

    /** Validation rules as JSON */
    @Column(name = "validation_rules", columnDefinition = "TEXT")
    private String validationRules;

    /** Conditional visibility rules as JSON */
    @Column(name = "conditional_rules", columnDefinition = "TEXT")
    private String conditionalRules;

    /** For SCALE type: minimum value */
    @Column(name = "min_value")
    private Integer minValue;

    /** For SCALE type: maximum value */
    @Column(name = "max_value")
    private Integer maxValue;

    /** For SCALE type: step increment */
    @Column(name = "step_value")
    private Integer stepValue;

    /** For SCALE type: labels for min/max */
    @Column(name = "scale_labels", length = 500)
    private String scaleLabels;

    /** Code system for standardized fields (e.g., SNOMED, LOINC) */
    @Column(name = "code_system", length = 50)
    private String codeSystem;

    /** Code within the code system */
    @Column(name = "code", length = 50)
    private String code;

    /** Unit of measurement */
    @Column(name = "unit", length = 50)
    private String unit;

    protected FormField() {
    }

    public FormField(String fieldKey, FieldType fieldType, String label) {
        this.fieldKey = fieldKey;
        this.fieldType = fieldType;
        this.label = label;
    }

    public FormField copy() {
        FormField copy = new FormField(this.fieldKey, this.fieldType, this.label);
        copy.setDescription(this.description);
        copy.setPlaceholder(this.placeholder);
        copy.setHelpText(this.helpText);
        copy.setSortOrder(this.sortOrder);
        copy.setRequired(this.required);
        copy.setReadOnly(this.readOnly);
        copy.setHidden(this.hidden);
        copy.setDefaultValue(this.defaultValue);
        copy.setOptions(this.options);
        copy.setValidationRules(this.validationRules);
        copy.setConditionalRules(this.conditionalRules);
        copy.setMinValue(this.minValue);
        copy.setMaxValue(this.maxValue);
        copy.setStepValue(this.stepValue);
        copy.setScaleLabels(this.scaleLabels);
        copy.setCodeSystem(this.codeSystem);
        copy.setCode(this.code);
        copy.setUnit(this.unit);
        return copy;
    }

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public FormTemplate getTemplate() {
        return template;
    }

    public void setTemplate(FormTemplate template) {
        this.template = template;
    }

    public String getFieldKey() {
        return fieldKey;
    }

    public void setFieldKey(String fieldKey) {
        this.fieldKey = fieldKey;
    }

    public FieldType getFieldType() {
        return fieldType;
    }

    public void setFieldType(FieldType fieldType) {
        this.fieldType = fieldType;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public String getHelpText() {
        return helpText;
    }

    public void setHelpText(String helpText) {
        this.helpText = helpText;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public boolean isRequired() {
        return required;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public String getValidationRules() {
        return validationRules;
    }

    public void setValidationRules(String validationRules) {
        this.validationRules = validationRules;
    }

    public String getConditionalRules() {
        return conditionalRules;
    }

    public void setConditionalRules(String conditionalRules) {
        this.conditionalRules = conditionalRules;
    }

    public Integer getMinValue() {
        return minValue;
    }

    public void setMinValue(Integer minValue) {
        this.minValue = minValue;
    }

    public Integer getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Integer maxValue) {
        this.maxValue = maxValue;
    }

    public Integer getStepValue() {
        return stepValue;
    }

    public void setStepValue(Integer stepValue) {
        this.stepValue = stepValue;
    }

    public String getScaleLabels() {
        return scaleLabels;
    }

    public void setScaleLabels(String scaleLabels) {
        this.scaleLabels = scaleLabels;
    }

    public String getCodeSystem() {
        return codeSystem;
    }

    public void setCodeSystem(String codeSystem) {
        this.codeSystem = codeSystem;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
