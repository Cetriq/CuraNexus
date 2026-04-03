package se.curanexus.lab.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Provmaterial - fysiskt prov taget från patient.
 */
@Entity
@Table(name = "lab_specimens", indexes = {
    @Index(name = "idx_specimen_order", columnList = "lab_order_id"),
    @Index(name = "idx_specimen_barcode", columnList = "barcode"),
    @Index(name = "idx_specimen_collected_at", columnList = "collected_at")
})
public class LabSpecimen {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Labbeställning */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_order_id", nullable = false)
    private LabOrder labOrder;

    /** Streckkod på provröret */
    @Column(name = "barcode", length = 50)
    private String barcode;

    /** Provtyp */
    @Enumerated(EnumType.STRING)
    @Column(name = "specimen_type", nullable = false, length = 30)
    private SpecimenType specimenType;

    /** Insamlingsmetod */
    @Column(name = "collection_method", length = 100)
    private String collectionMethod;

    /** Anatomisk lokalisation */
    @Column(name = "body_site", length = 100)
    private String bodySite;

    /** Volym/mängd */
    @Column(name = "quantity", length = 50)
    private String quantity;

    /** Provrörsfärg/typ */
    @Column(name = "container_type", length = 50)
    private String containerType;

    /** Provtagare ID */
    @Column(name = "collector_id")
    private UUID collectorId;

    /** Provtagare namn */
    @Column(name = "collector_name", length = 200)
    private String collectorName;

    /** Tidpunkt för provtagning */
    @Column(name = "collected_at")
    private Instant collectedAt;

    /** Tidpunkt för mottagning på lab */
    @Column(name = "received_at_lab")
    private Instant receivedAtLab;

    /** Provkvalitet */
    @Column(name = "quality_status", length = 50)
    private String qualityStatus;

    /** Kommentar om provet */
    @Column(name = "specimen_comment", length = 500)
    private String specimenComment;

    /** Avvisat prov */
    @Column(name = "rejected")
    private Boolean rejected = false;

    /** Orsak till avvisning */
    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected LabSpecimen() {
    }

    public LabSpecimen(SpecimenType specimenType) {
        this.specimenType = specimenType;
        this.createdAt = Instant.now();
    }

    public LabSpecimen(SpecimenType specimenType, String barcode) {
        this.specimenType = specimenType;
        this.barcode = barcode;
        this.createdAt = Instant.now();
    }

    /**
     * Registrera att provet tagits.
     */
    public void collect(UUID collectorId, String collectorName) {
        this.collectorId = collectorId;
        this.collectorName = collectorName;
        this.collectedAt = Instant.now();
    }

    /**
     * Registrera mottagning på lab.
     */
    public void receiveAtLab() {
        this.receivedAtLab = Instant.now();
    }

    /**
     * Avvisa prov.
     */
    public void reject(String reason) {
        this.rejected = true;
        this.rejectionReason = reason;
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

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public SpecimenType getSpecimenType() {
        return specimenType;
    }

    public void setSpecimenType(SpecimenType specimenType) {
        this.specimenType = specimenType;
    }

    public String getCollectionMethod() {
        return collectionMethod;
    }

    public void setCollectionMethod(String collectionMethod) {
        this.collectionMethod = collectionMethod;
    }

    public String getBodySite() {
        return bodySite;
    }

    public void setBodySite(String bodySite) {
        this.bodySite = bodySite;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getContainerType() {
        return containerType;
    }

    public void setContainerType(String containerType) {
        this.containerType = containerType;
    }

    public UUID getCollectorId() {
        return collectorId;
    }

    public String getCollectorName() {
        return collectorName;
    }

    public Instant getCollectedAt() {
        return collectedAt;
    }

    public Instant getReceivedAtLab() {
        return receivedAtLab;
    }

    public String getQualityStatus() {
        return qualityStatus;
    }

    public void setQualityStatus(String qualityStatus) {
        this.qualityStatus = qualityStatus;
    }

    public String getSpecimenComment() {
        return specimenComment;
    }

    public void setSpecimenComment(String specimenComment) {
        this.specimenComment = specimenComment;
    }

    public Boolean getRejected() {
        return rejected;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
