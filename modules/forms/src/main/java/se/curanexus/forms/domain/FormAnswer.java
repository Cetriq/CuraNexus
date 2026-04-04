package se.curanexus.forms.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * An answer to a single field in a form submission.
 */
@Entity
@Table(name = "form_answers")
public class FormAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private FormSubmission submission;

    @Column(name = "field_key", nullable = false, length = 100)
    private String fieldKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "field_type", nullable = false, length = 30)
    private FieldType fieldType;

    /** String value for text, single choice, etc. */
    @Column(name = "value_text", columnDefinition = "TEXT")
    private String valueText;

    /** Numeric value for numbers, scales */
    @Column(name = "value_number")
    private Double valueNumber;

    /** Boolean value */
    @Column(name = "value_boolean")
    private Boolean valueBoolean;

    /** Date/time value */
    @Column(name = "value_datetime")
    private Instant valueDatetime;

    /** JSON array for multiple choice answers */
    @Column(name = "value_array", columnDefinition = "TEXT")
    private String valueArray;

    /** File reference for file uploads */
    @Column(name = "file_reference", length = 500)
    private String fileReference;

    /** Code system if standardized answer */
    @Column(name = "code_system", length = 50)
    private String codeSystem;

    /** Code value if standardized answer */
    @Column(name = "code", length = 50)
    private String code;

    /** Display text for coded answers */
    @Column(name = "code_display", length = 500)
    private String codeDisplay;

    @Column(name = "answered_at", nullable = false)
    private Instant answeredAt;

    @Column(name = "modified_at")
    private Instant modifiedAt;

    protected FormAnswer() {
    }

    public FormAnswer(String fieldKey, FieldType fieldType) {
        this.fieldKey = fieldKey;
        this.fieldType = fieldType;
        this.answeredAt = Instant.now();
    }

    public void setTextValue(String value) {
        this.valueText = value;
        this.modifiedAt = Instant.now();
    }

    public void setNumericValue(Double value) {
        this.valueNumber = value;
        this.modifiedAt = Instant.now();
    }

    public void setBooleanValue(Boolean value) {
        this.valueBoolean = value;
        this.modifiedAt = Instant.now();
    }

    public void setDatetimeValue(Instant value) {
        this.valueDatetime = value;
        this.modifiedAt = Instant.now();
    }

    public void setArrayValue(String jsonArray) {
        this.valueArray = jsonArray;
        this.modifiedAt = Instant.now();
    }

    public void setCodedValue(String codeSystem, String code, String display) {
        this.codeSystem = codeSystem;
        this.code = code;
        this.codeDisplay = display;
        this.modifiedAt = Instant.now();
    }

    /**
     * Get the primary value based on field type.
     */
    public Object getValue() {
        return switch (fieldType) {
            case TEXT, TEXTAREA, SELECT, SINGLE_CHOICE, SIGNATURE -> valueText;
            case NUMBER, SCALE, VAS -> valueNumber;
            case BOOLEAN -> valueBoolean;
            case DATE, DATETIME, TIME -> valueDatetime;
            case MULTIPLE_CHOICE -> valueArray;
            case FILE -> fileReference;
            default -> valueText;
        };
    }

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public FormSubmission getSubmission() {
        return submission;
    }

    public void setSubmission(FormSubmission submission) {
        this.submission = submission;
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

    public String getValueText() {
        return valueText;
    }

    public void setValueText(String valueText) {
        this.valueText = valueText;
    }

    public Double getValueNumber() {
        return valueNumber;
    }

    public void setValueNumber(Double valueNumber) {
        this.valueNumber = valueNumber;
    }

    public Boolean getValueBoolean() {
        return valueBoolean;
    }

    public void setValueBoolean(Boolean valueBoolean) {
        this.valueBoolean = valueBoolean;
    }

    public Instant getValueDatetime() {
        return valueDatetime;
    }

    public void setValueDatetime(Instant valueDatetime) {
        this.valueDatetime = valueDatetime;
    }

    public String getValueArray() {
        return valueArray;
    }

    public void setValueArray(String valueArray) {
        this.valueArray = valueArray;
    }

    public String getFileReference() {
        return fileReference;
    }

    public void setFileReference(String fileReference) {
        this.fileReference = fileReference;
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

    public String getCodeDisplay() {
        return codeDisplay;
    }

    public void setCodeDisplay(String codeDisplay) {
        this.codeDisplay = codeDisplay;
    }

    public Instant getAnsweredAt() {
        return answeredAt;
    }

    public Instant getModifiedAt() {
        return modifiedAt;
    }
}
