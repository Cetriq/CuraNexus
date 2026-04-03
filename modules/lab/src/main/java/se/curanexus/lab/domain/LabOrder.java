package se.curanexus.lab.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Labbeställning - beställning av laboratorieanalyser.
 */
@Entity
@Table(name = "lab_orders", indexes = {
    @Index(name = "idx_lab_order_patient", columnList = "patient_id"),
    @Index(name = "idx_lab_order_status", columnList = "status"),
    @Index(name = "idx_lab_order_reference", columnList = "order_reference"),
    @Index(name = "idx_lab_order_ordered_at", columnList = "ordered_at"),
    @Index(name = "idx_lab_order_encounter", columnList = "encounter_id")
})
public class LabOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Unik beställningsreferens */
    @Column(name = "order_reference", nullable = false, unique = true, length = 20)
    private String orderReference;

    /** Patient som beställningen gäller */
    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    /** Personnummer */
    @Column(name = "patient_personnummer", length = 12)
    private String patientPersonnummer;

    /** Patientens namn */
    @Column(name = "patient_name", length = 200)
    private String patientName;

    /** Status */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private LabOrderStatus status = LabOrderStatus.DRAFT;

    /** Prioritet */
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private LabOrderPriority priority = LabOrderPriority.ROUTINE;

    // === Beställare ===

    /** Beställande enhet ID */
    @Column(name = "ordering_unit_id", nullable = false)
    private UUID orderingUnitId;

    /** Beställande enhet HSA-ID */
    @Column(name = "ordering_unit_hsa_id", length = 50)
    private String orderingUnitHsaId;

    /** Beställande enhet namn */
    @Column(name = "ordering_unit_name", length = 200)
    private String orderingUnitName;

    /** Beställande läkare ID */
    @Column(name = "ordering_practitioner_id", nullable = false)
    private UUID orderingPractitionerId;

    /** Beställande läkare HSA-ID */
    @Column(name = "ordering_practitioner_hsa_id", length = 50)
    private String orderingPractitionerHsaId;

    /** Beställande läkare namn */
    @Column(name = "ordering_practitioner_name", length = 200)
    private String orderingPractitionerName;

    // === Utförande lab ===

    /** Labb-ID */
    @Column(name = "performing_lab_id")
    private UUID performingLabId;

    /** Labb HSA-ID */
    @Column(name = "performing_lab_hsa_id", length = 50)
    private String performingLabHsaId;

    /** Labb namn */
    @Column(name = "performing_lab_name", length = 200)
    private String performingLabName;

    // === Klinisk information ===

    /** Frågeställning/indikation */
    @Column(name = "clinical_indication", length = 2000)
    private String clinicalIndication;

    /** Diagnoskod (ICD-10) */
    @Column(name = "diagnosis_code", length = 20)
    private String diagnosisCode;

    /** Diagnostext */
    @Column(name = "diagnosis_text", length = 500)
    private String diagnosisText;

    /** Relevant medicinering */
    @Column(name = "relevant_medication", length = 1000)
    private String relevantMedication;

    /** Fastande patient */
    @Column(name = "fasting_required")
    private Boolean fastingRequired;

    /** Kommentar till lab */
    @Column(name = "lab_comment", length = 1000)
    private String labComment;

    // === Kopplingar ===

    /** Vårdkontakt där beställningen gjordes */
    @Column(name = "encounter_id")
    private UUID encounterId;

    /** Kopplad remiss (om lab-remiss) */
    @Column(name = "referral_id")
    private UUID referralId;

    // === Tidsstämplar ===

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "ordered_at")
    private Instant orderedAt;

    @Column(name = "received_at")
    private Instant receivedAt;

    @Column(name = "specimen_collected_at")
    private Instant specimenCollectedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    // === Prover ===

    @OneToMany(mappedBy = "labOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LabSpecimen> specimens = new ArrayList<>();

    // === Beställda tester ===

    @OneToMany(mappedBy = "labOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LabOrderItem> orderItems = new ArrayList<>();

    protected LabOrder() {
    }

    public LabOrder(UUID patientId, UUID orderingUnitId, UUID orderingPractitionerId) {
        this.patientId = patientId;
        this.orderingUnitId = orderingUnitId;
        this.orderingPractitionerId = orderingPractitionerId;
        this.status = LabOrderStatus.DRAFT;
        this.priority = LabOrderPriority.ROUTINE;
        this.createdAt = Instant.now();
        this.orderReference = generateOrderReference();
    }

    private String generateOrderReference() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return "LAB-" + datePart + "-" + randomPart;
    }

    // === Affärslogik ===

    /**
     * Skicka beställning till lab.
     */
    public void send() {
        if (status != LabOrderStatus.DRAFT) {
            throw new IllegalStateException("Kan endast skicka beställning i status DRAFT");
        }
        if (orderItems.isEmpty()) {
            throw new IllegalStateException("Beställningen måste innehålla minst ett test");
        }
        this.status = LabOrderStatus.ORDERED;
        this.orderedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Markera som mottagen av lab.
     */
    public void markReceived() {
        if (status != LabOrderStatus.ORDERED) {
            throw new IllegalStateException("Kan endast markera skickad beställning som mottagen");
        }
        this.status = LabOrderStatus.RECEIVED;
        this.receivedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Registrera att prov tagits.
     */
    public void markSpecimenCollected() {
        if (status != LabOrderStatus.RECEIVED && status != LabOrderStatus.ORDERED) {
            throw new IllegalStateException("Kan endast registrera provtagning för mottagen/beställd order");
        }
        this.status = LabOrderStatus.SPECIMEN_COLLECTED;
        this.specimenCollectedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Starta analys.
     */
    public void startAnalysis() {
        if (status != LabOrderStatus.SPECIMEN_COLLECTED && status != LabOrderStatus.RECEIVED) {
            throw new IllegalStateException("Kan endast starta analys efter provtagning");
        }
        this.status = LabOrderStatus.IN_PROGRESS;
        this.updatedAt = Instant.now();
    }

    /**
     * Registrera delresultat.
     */
    public void registerPartialResults() {
        if (status != LabOrderStatus.IN_PROGRESS && status != LabOrderStatus.PARTIAL_RESULTS) {
            throw new IllegalStateException("Kan endast registrera resultat för pågående analys");
        }
        this.status = LabOrderStatus.PARTIAL_RESULTS;
        this.updatedAt = Instant.now();
    }

    /**
     * Slutför beställning - alla resultat klara.
     */
    public void complete() {
        if (status != LabOrderStatus.IN_PROGRESS && status != LabOrderStatus.PARTIAL_RESULTS) {
            throw new IllegalStateException("Kan endast slutföra pågående analys");
        }
        this.status = LabOrderStatus.COMPLETED;
        this.completedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Makulera beställning.
     */
    public void cancel(String reason) {
        if (status == LabOrderStatus.COMPLETED || status == LabOrderStatus.CANCELLED) {
            throw new IllegalStateException("Kan ej makulera slutförd eller redan makulerad beställning");
        }
        this.status = LabOrderStatus.CANCELLED;
        this.updatedAt = Instant.now();
    }

    /**
     * Avvisa beställning.
     */
    public void reject(String reason) {
        if (status != LabOrderStatus.ORDERED && status != LabOrderStatus.RECEIVED) {
            throw new IllegalStateException("Kan endast avvisa beställning som inte påbörjats");
        }
        this.status = LabOrderStatus.REJECTED;
        this.updatedAt = Instant.now();
    }

    /**
     * Lägg till testbeställning.
     */
    public void addOrderItem(LabOrderItem item) {
        if (status != LabOrderStatus.DRAFT) {
            throw new IllegalStateException("Kan endast lägga till test i utkast");
        }
        orderItems.add(item);
        item.setLabOrder(this);
    }

    /**
     * Lägg till prov.
     */
    public void addSpecimen(LabSpecimen specimen) {
        specimens.add(specimen);
        specimen.setLabOrder(this);
    }

    /**
     * Kontrollera om beställningen är redigerbar.
     */
    public boolean isEditable() {
        return status == LabOrderStatus.DRAFT;
    }

    /**
     * Kontrollera om alla resultat är klara.
     */
    public boolean hasAllResults() {
        return orderItems.stream()
                .allMatch(item -> item.getResult() != null &&
                         item.getResult().getStatus() == ResultStatus.FINAL);
    }

    /**
     * Kontrollera om det finns kritiska resultat.
     */
    public boolean hasCriticalResults() {
        return orderItems.stream()
                .filter(item -> item.getResult() != null)
                .anyMatch(item -> {
                    AbnormalFlag flag = item.getResult().getAbnormalFlag();
                    return flag == AbnormalFlag.CRITICAL_HIGH || flag == AbnormalFlag.CRITICAL_LOW;
                });
    }

    // === Getters och setters ===

    public UUID getId() {
        return id;
    }

    public String getOrderReference() {
        return orderReference;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public String getPatientPersonnummer() {
        return patientPersonnummer;
    }

    public void setPatientPersonnummer(String patientPersonnummer) {
        this.patientPersonnummer = patientPersonnummer;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public LabOrderStatus getStatus() {
        return status;
    }

    public LabOrderPriority getPriority() {
        return priority;
    }

    public void setPriority(LabOrderPriority priority) {
        this.priority = priority;
    }

    public UUID getOrderingUnitId() {
        return orderingUnitId;
    }

    public String getOrderingUnitHsaId() {
        return orderingUnitHsaId;
    }

    public void setOrderingUnitHsaId(String orderingUnitHsaId) {
        this.orderingUnitHsaId = orderingUnitHsaId;
    }

    public String getOrderingUnitName() {
        return orderingUnitName;
    }

    public void setOrderingUnitName(String orderingUnitName) {
        this.orderingUnitName = orderingUnitName;
    }

    public UUID getOrderingPractitionerId() {
        return orderingPractitionerId;
    }

    public String getOrderingPractitionerHsaId() {
        return orderingPractitionerHsaId;
    }

    public void setOrderingPractitionerHsaId(String orderingPractitionerHsaId) {
        this.orderingPractitionerHsaId = orderingPractitionerHsaId;
    }

    public String getOrderingPractitionerName() {
        return orderingPractitionerName;
    }

    public void setOrderingPractitionerName(String orderingPractitionerName) {
        this.orderingPractitionerName = orderingPractitionerName;
    }

    public UUID getPerformingLabId() {
        return performingLabId;
    }

    public void setPerformingLabId(UUID performingLabId) {
        this.performingLabId = performingLabId;
    }

    public String getPerformingLabHsaId() {
        return performingLabHsaId;
    }

    public void setPerformingLabHsaId(String performingLabHsaId) {
        this.performingLabHsaId = performingLabHsaId;
    }

    public String getPerformingLabName() {
        return performingLabName;
    }

    public void setPerformingLabName(String performingLabName) {
        this.performingLabName = performingLabName;
    }

    public String getClinicalIndication() {
        return clinicalIndication;
    }

    public void setClinicalIndication(String clinicalIndication) {
        this.clinicalIndication = clinicalIndication;
    }

    public String getDiagnosisCode() {
        return diagnosisCode;
    }

    public void setDiagnosisCode(String diagnosisCode) {
        this.diagnosisCode = diagnosisCode;
    }

    public String getDiagnosisText() {
        return diagnosisText;
    }

    public void setDiagnosisText(String diagnosisText) {
        this.diagnosisText = diagnosisText;
    }

    public String getRelevantMedication() {
        return relevantMedication;
    }

    public void setRelevantMedication(String relevantMedication) {
        this.relevantMedication = relevantMedication;
    }

    public Boolean getFastingRequired() {
        return fastingRequired;
    }

    public void setFastingRequired(Boolean fastingRequired) {
        this.fastingRequired = fastingRequired;
    }

    public String getLabComment() {
        return labComment;
    }

    public void setLabComment(String labComment) {
        this.labComment = labComment;
    }

    public UUID getEncounterId() {
        return encounterId;
    }

    public void setEncounterId(UUID encounterId) {
        this.encounterId = encounterId;
    }

    public UUID getReferralId() {
        return referralId;
    }

    public void setReferralId(UUID referralId) {
        this.referralId = referralId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getOrderedAt() {
        return orderedAt;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public Instant getSpecimenCollectedAt() {
        return specimenCollectedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public List<LabSpecimen> getSpecimens() {
        return specimens;
    }

    public List<LabOrderItem> getOrderItems() {
        return orderItems;
    }
}
