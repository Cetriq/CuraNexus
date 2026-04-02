package se.curanexus.journal.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "observations", indexes = {
    @Index(name = "idx_observation_encounter", columnList = "encounter_id"),
    @Index(name = "idx_observation_patient", columnList = "patient_id"),
    @Index(name = "idx_observation_code", columnList = "code"),
    @Index(name = "idx_observation_category", columnList = "category")
})
public class Observation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "encounter_id")
    private UUID encounterId;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "code_system", length = 50)
    private String codeSystem;

    @Column(name = "display_text", length = 500)
    private String displayText;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private ObservationCategory category;

    // Value can be numeric, string, or boolean - we store all variants
    @Column(name = "value_numeric", precision = 18, scale = 4)
    private BigDecimal valueNumeric;

    @Column(name = "value_string", length = 1000)
    private String valueString;

    @Column(name = "value_boolean")
    private Boolean valueBoolean;

    @Column(name = "unit", length = 50)
    private String unit;

    // Reference range for numeric values
    @Column(name = "reference_range_low", precision = 18, scale = 4)
    private BigDecimal referenceRangeLow;

    @Column(name = "reference_range_high", precision = 18, scale = 4)
    private BigDecimal referenceRangeHigh;

    @Enumerated(EnumType.STRING)
    @Column(name = "interpretation", length = 30)
    private ObservationInterpretation interpretation;

    @Column(name = "observed_at", nullable = false)
    private LocalDateTime observedAt;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    @Column(name = "recorded_by_id")
    private UUID recordedById;

    @Column(name = "recorded_by_name", length = 200)
    private String recordedByName;

    @Column(name = "method", length = 200)
    private String method;

    @Column(name = "body_site", length = 100)
    private String bodySite;

    @Column(name = "device", length = 200)
    private String device;

    @Column(name = "notes", length = 1000)
    private String notes;

    protected Observation() {
    }

    public Observation(UUID patientId, String code, ObservationCategory category, LocalDateTime observedAt) {
        this.patientId = patientId;
        this.code = code;
        this.category = category;
        this.observedAt = observedAt;
        this.recordedAt = Instant.now();
    }

    public void setNumericValue(BigDecimal value, String unit) {
        this.valueNumeric = value;
        this.unit = unit;
        this.valueString = null;
        this.valueBoolean = null;
    }

    public void setStringValue(String value) {
        this.valueString = value;
        this.valueNumeric = null;
        this.valueBoolean = null;
        this.unit = null;
    }

    public void setBooleanValue(Boolean value) {
        this.valueBoolean = value;
        this.valueNumeric = null;
        this.valueString = null;
        this.unit = null;
    }

    public void setReferenceRange(BigDecimal low, BigDecimal high) {
        this.referenceRangeLow = low;
        this.referenceRangeHigh = high;
    }

    public boolean isWithinReferenceRange() {
        if (valueNumeric == null || (referenceRangeLow == null && referenceRangeHigh == null)) {
            return true; // No numeric value or no reference range defined
        }
        if (referenceRangeLow != null && valueNumeric.compareTo(referenceRangeLow) < 0) {
            return false;
        }
        if (referenceRangeHigh != null && valueNumeric.compareTo(referenceRangeHigh) > 0) {
            return false;
        }
        return true;
    }

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public UUID getEncounterId() {
        return encounterId;
    }

    public void setEncounterId(UUID encounterId) {
        this.encounterId = encounterId;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCodeSystem() {
        return codeSystem;
    }

    public void setCodeSystem(String codeSystem) {
        this.codeSystem = codeSystem;
    }

    public String getDisplayText() {
        return displayText;
    }

    public void setDisplayText(String displayText) {
        this.displayText = displayText;
    }

    public ObservationCategory getCategory() {
        return category;
    }

    public void setCategory(ObservationCategory category) {
        this.category = category;
    }

    public BigDecimal getValueNumeric() {
        return valueNumeric;
    }

    public String getValueString() {
        return valueString;
    }

    public Boolean getValueBoolean() {
        return valueBoolean;
    }

    public String getUnit() {
        return unit;
    }

    public BigDecimal getReferenceRangeLow() {
        return referenceRangeLow;
    }

    public BigDecimal getReferenceRangeHigh() {
        return referenceRangeHigh;
    }

    public ObservationInterpretation getInterpretation() {
        return interpretation;
    }

    public void setInterpretation(ObservationInterpretation interpretation) {
        this.interpretation = interpretation;
    }

    public LocalDateTime getObservedAt() {
        return observedAt;
    }

    public void setObservedAt(LocalDateTime observedAt) {
        this.observedAt = observedAt;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }

    public UUID getRecordedById() {
        return recordedById;
    }

    public void setRecordedById(UUID recordedById) {
        this.recordedById = recordedById;
    }

    public String getRecordedByName() {
        return recordedByName;
    }

    public void setRecordedByName(String recordedByName) {
        this.recordedByName = recordedByName;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getBodySite() {
        return bodySite;
    }

    public void setBodySite(String bodySite) {
        this.bodySite = bodySite;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
