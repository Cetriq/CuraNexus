package se.curanexus.lab.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Beställd analys/test inom en labbeställning.
 */
@Entity
@Table(name = "lab_order_items", indexes = {
    @Index(name = "idx_order_item_order", columnList = "lab_order_id"),
    @Index(name = "idx_order_item_test_code", columnList = "test_code")
})
public class LabOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Labbeställning */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_order_id", nullable = false)
    private LabOrder labOrder;

    /** Testkod (NPU, lokalt, etc.) */
    @Column(name = "test_code", nullable = false, length = 50)
    private String testCode;

    /** Kodningssystem (NPU, LOINC, lokalt) */
    @Column(name = "code_system", length = 50)
    private String codeSystem;

    /** Testnamn */
    @Column(name = "test_name", nullable = false, length = 200)
    private String testName;

    /** Testbeskrivning */
    @Column(name = "test_description", length = 500)
    private String testDescription;

    /** Provmaterialtyp som krävs */
    @Enumerated(EnumType.STRING)
    @Column(name = "specimen_type", length = 30)
    private SpecimenType specimenType;

    /** Specifik kommentar för detta test */
    @Column(name = "item_comment", length = 500)
    private String itemComment;

    /** Resultat för detta test */
    @OneToOne(mappedBy = "orderItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private LabResult result;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected LabOrderItem() {
    }

    public LabOrderItem(String testCode, String testName) {
        this.testCode = testCode;
        this.testName = testName;
        this.createdAt = Instant.now();
    }

    public LabOrderItem(String testCode, String codeSystem, String testName, SpecimenType specimenType) {
        this.testCode = testCode;
        this.codeSystem = codeSystem;
        this.testName = testName;
        this.specimenType = specimenType;
        this.createdAt = Instant.now();
    }

    // === Getters och setters ===

    public UUID getId() {
        return id;
    }

    public LabOrder getLabOrder() {
        return labOrder;
    }

    public void setLabOrder(LabOrder labOrder) {
        this.labOrder = labOrder;
    }

    public String getTestCode() {
        return testCode;
    }

    public void setTestCode(String testCode) {
        this.testCode = testCode;
    }

    public String getCodeSystem() {
        return codeSystem;
    }

    public void setCodeSystem(String codeSystem) {
        this.codeSystem = codeSystem;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getTestDescription() {
        return testDescription;
    }

    public void setTestDescription(String testDescription) {
        this.testDescription = testDescription;
    }

    public SpecimenType getSpecimenType() {
        return specimenType;
    }

    public void setSpecimenType(SpecimenType specimenType) {
        this.specimenType = specimenType;
    }

    public String getItemComment() {
        return itemComment;
    }

    public void setItemComment(String itemComment) {
        this.itemComment = itemComment;
    }

    public LabResult getResult() {
        return result;
    }

    public void setResult(LabResult result) {
        this.result = result;
        if (result != null) {
            result.setOrderItem(this);
        }
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
