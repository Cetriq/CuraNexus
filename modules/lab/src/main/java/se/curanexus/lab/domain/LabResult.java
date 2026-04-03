package se.curanexus.lab.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Labresultat - resultat från en analys.
 */
@Entity
@Table(name = "lab_results", indexes = {
    @Index(name = "idx_result_order_item", columnList = "order_item_id"),
    @Index(name = "idx_result_status", columnList = "status"),
    @Index(name = "idx_result_abnormal", columnList = "abnormal_flag"),
    @Index(name = "idx_result_resulted_at", columnList = "resulted_at")
})
public class LabResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Beställd analys */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private LabOrderItem orderItem;

    /** Resultatstatus */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ResultStatus status = ResultStatus.PENDING;

    // === Numeriskt värde ===

    /** Numeriskt värde */
    @Column(name = "value_numeric", precision = 18, scale = 6)
    private BigDecimal valueNumeric;

    /** Enhet */
    @Column(name = "unit", length = 50)
    private String unit;

    // === Textvärde ===

    /** Textvärde (för kvalitativa resultat) */
    @Column(name = "value_text", length = 2000)
    private String valueText;

    // === Referensvärden ===

    /** Nedre referensgräns */
    @Column(name = "reference_low", precision = 18, scale = 6)
    private BigDecimal referenceLow;

    /** Övre referensgräns */
    @Column(name = "reference_high", precision = 18, scale = 6)
    private BigDecimal referenceHigh;

    /** Referensintervall som text */
    @Column(name = "reference_range_text", length = 200)
    private String referenceRangeText;

    // === Avvikelse ===

    /** Avvikelseflagga */
    @Enumerated(EnumType.STRING)
    @Column(name = "abnormal_flag", length = 20)
    private AbnormalFlag abnormalFlag;

    /** Kritiskt värde som kräver omedelbar åtgärd */
    @Column(name = "is_critical")
    private Boolean isCritical = false;

    // === Analysuppgifter ===

    /** Analysmetod */
    @Column(name = "method", length = 200)
    private String method;

    /** Instrument */
    @Column(name = "instrument", length = 200)
    private String instrument;

    /** Utförande lab-avdelning */
    @Column(name = "performing_department", length = 200)
    private String performingDepartment;

    /** Analyserande ID */
    @Column(name = "analyzer_id")
    private UUID analyzerId;

    /** Analyserande namn */
    @Column(name = "analyzer_name", length = 200)
    private String analyzerName;

    /** Granskare ID */
    @Column(name = "reviewer_id")
    private UUID reviewerId;

    /** Granskare namn */
    @Column(name = "reviewer_name", length = 200)
    private String reviewerName;

    // === Kommentarer ===

    /** Labkommentar */
    @Column(name = "lab_comment", length = 1000)
    private String labComment;

    /** Intern kommentar */
    @Column(name = "internal_comment", length = 1000)
    private String internalComment;

    // === Tidsstämplar ===

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    /** Tidpunkt för analys */
    @Column(name = "analyzed_at")
    private Instant analyzedAt;

    /** Tidpunkt för granskning */
    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    /** Tidpunkt för resultat */
    @Column(name = "resulted_at")
    private Instant resultedAt;

    protected LabResult() {
    }

    public LabResult(LabOrderItem orderItem) {
        this.orderItem = orderItem;
        this.status = ResultStatus.PENDING;
        this.createdAt = Instant.now();
    }

    // === Affärslogik ===

    /**
     * Registrera numeriskt resultat.
     */
    public void registerNumericResult(BigDecimal value, String unit,
                                       BigDecimal referenceLow, BigDecimal referenceHigh) {
        this.valueNumeric = value;
        this.unit = unit;
        this.referenceLow = referenceLow;
        this.referenceHigh = referenceHigh;
        this.status = ResultStatus.PRELIMINARY;
        this.analyzedAt = Instant.now();
        this.updatedAt = Instant.now();

        // Beräkna avvikelseflagga
        calculateAbnormalFlag();
    }

    /**
     * Registrera textvärde.
     */
    public void registerTextResult(String value) {
        this.valueText = value;
        this.status = ResultStatus.PRELIMINARY;
        this.analyzedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Beräkna avvikelseflagga baserat på referensvärden.
     */
    private void calculateAbnormalFlag() {
        if (valueNumeric == null) {
            return;
        }

        if (referenceLow != null && valueNumeric.compareTo(referenceLow) < 0) {
            // Lågt - kontrollera om kritiskt lågt (t.ex. <50% av nedre gräns)
            BigDecimal criticalThreshold = referenceLow.multiply(new BigDecimal("0.5"));
            if (valueNumeric.compareTo(criticalThreshold) < 0) {
                this.abnormalFlag = AbnormalFlag.CRITICAL_LOW;
                this.isCritical = true;
            } else {
                this.abnormalFlag = AbnormalFlag.LOW;
            }
        } else if (referenceHigh != null && valueNumeric.compareTo(referenceHigh) > 0) {
            // Högt - kontrollera om kritiskt högt (t.ex. >150% av övre gräns)
            BigDecimal criticalThreshold = referenceHigh.multiply(new BigDecimal("1.5"));
            if (valueNumeric.compareTo(criticalThreshold) > 0) {
                this.abnormalFlag = AbnormalFlag.CRITICAL_HIGH;
                this.isCritical = true;
            } else {
                this.abnormalFlag = AbnormalFlag.HIGH;
            }
        } else {
            this.abnormalFlag = AbnormalFlag.NORMAL;
        }
    }

    /**
     * Sätt avvikelseflagga manuellt (för kvalitativa tester).
     */
    public void setAbnormalFlagManually(AbnormalFlag flag, boolean isCritical) {
        this.abnormalFlag = flag;
        this.isCritical = isCritical;
    }

    /**
     * Granska och godkänn resultat.
     */
    public void review(UUID reviewerId, String reviewerName) {
        if (status != ResultStatus.PRELIMINARY) {
            throw new IllegalStateException("Kan endast granska preliminära resultat");
        }
        this.reviewerId = reviewerId;
        this.reviewerName = reviewerName;
        this.reviewedAt = Instant.now();
        this.status = ResultStatus.FINAL;
        this.resultedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Korrigera resultat.
     */
    public void correct(BigDecimal newValue, String correctionReason, UUID correctorId, String correctorName) {
        if (status != ResultStatus.FINAL) {
            throw new IllegalStateException("Kan endast korrigera slutgiltiga resultat");
        }
        this.valueNumeric = newValue;
        this.labComment = (this.labComment != null ? this.labComment + " | " : "") +
                          "Korrigerat: " + correctionReason;
        this.reviewerId = correctorId;
        this.reviewerName = correctorName;
        this.status = ResultStatus.CORRECTED;
        this.updatedAt = Instant.now();
        calculateAbnormalFlag();
    }

    /**
     * Makulera resultat.
     */
    public void cancel(String reason) {
        this.status = ResultStatus.CANCELLED;
        this.labComment = (this.labComment != null ? this.labComment + " | " : "") +
                          "Makulerat: " + reason;
        this.updatedAt = Instant.now();
    }

    // === Getters och setters ===

    public UUID getId() {
        return id;
    }

    public LabOrderItem getOrderItem() {
        return orderItem;
    }

    public void setOrderItem(LabOrderItem orderItem) {
        this.orderItem = orderItem;
    }

    public ResultStatus getStatus() {
        return status;
    }

    public BigDecimal getValueNumeric() {
        return valueNumeric;
    }

    public String getUnit() {
        return unit;
    }

    public String getValueText() {
        return valueText;
    }

    public void setValueText(String valueText) {
        this.valueText = valueText;
    }

    public BigDecimal getReferenceLow() {
        return referenceLow;
    }

    public BigDecimal getReferenceHigh() {
        return referenceHigh;
    }

    public String getReferenceRangeText() {
        return referenceRangeText;
    }

    public void setReferenceRangeText(String referenceRangeText) {
        this.referenceRangeText = referenceRangeText;
    }

    public AbnormalFlag getAbnormalFlag() {
        return abnormalFlag;
    }

    public Boolean getIsCritical() {
        return isCritical;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getInstrument() {
        return instrument;
    }

    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }

    public String getPerformingDepartment() {
        return performingDepartment;
    }

    public void setPerformingDepartment(String performingDepartment) {
        this.performingDepartment = performingDepartment;
    }

    public UUID getAnalyzerId() {
        return analyzerId;
    }

    public void setAnalyzerId(UUID analyzerId) {
        this.analyzerId = analyzerId;
    }

    public String getAnalyzerName() {
        return analyzerName;
    }

    public void setAnalyzerName(String analyzerName) {
        this.analyzerName = analyzerName;
    }

    public UUID getReviewerId() {
        return reviewerId;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public String getLabComment() {
        return labComment;
    }

    public void setLabComment(String labComment) {
        this.labComment = labComment;
    }

    public String getInternalComment() {
        return internalComment;
    }

    public void setInternalComment(String internalComment) {
        this.internalComment = internalComment;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getAnalyzedAt() {
        return analyzedAt;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }

    public Instant getResultedAt() {
        return resultedAt;
    }
}
