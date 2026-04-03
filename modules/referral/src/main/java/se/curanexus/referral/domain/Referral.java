package se.curanexus.referral.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Remiss - begäran om vård/utredning hos annan vårdgivare.
 */
@Entity
@Table(name = "referrals", indexes = {
    @Index(name = "idx_referral_patient", columnList = "patient_id"),
    @Index(name = "idx_referral_status", columnList = "status"),
    @Index(name = "idx_referral_sender_unit", columnList = "sender_unit_id"),
    @Index(name = "idx_referral_receiver_unit", columnList = "receiver_unit_id"),
    @Index(name = "idx_referral_reference", columnList = "referral_reference")
})
public class Referral {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Unik remissreferens (för spårning) */
    @Column(name = "referral_reference", nullable = false, unique = true, length = 20)
    private String referralReference;

    /** Patient som remissen gäller */
    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    /** Personnummer (för extern kommunikation) */
    @Column(name = "patient_personnummer", length = 12)
    private String patientPersonnummer;

    /** Patientens namn */
    @Column(name = "patient_name", length = 200)
    private String patientName;

    /** Typ av remiss */
    @Enumerated(EnumType.STRING)
    @Column(name = "referral_type", nullable = false, length = 30)
    private ReferralType referralType;

    /** Status */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ReferralStatus status = ReferralStatus.DRAFT;

    /** Prioritet */
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private ReferralPriority priority = ReferralPriority.ROUTINE;

    // === Avsändare ===

    /** Remitterande enhet ID */
    @Column(name = "sender_unit_id", nullable = false)
    private UUID senderUnitId;

    /** Remitterande enhet HSA-ID */
    @Column(name = "sender_unit_hsa_id", length = 50)
    private String senderUnitHsaId;

    /** Remitterande enhet namn */
    @Column(name = "sender_unit_name", length = 200)
    private String senderUnitName;

    /** Remitterande läkare ID */
    @Column(name = "sender_practitioner_id", nullable = false)
    private UUID senderPractitionerId;

    /** Remitterande läkare HSA-ID */
    @Column(name = "sender_practitioner_hsa_id", length = 50)
    private String senderPractitionerHsaId;

    /** Remitterande läkare namn */
    @Column(name = "sender_practitioner_name", length = 200)
    private String senderPractitionerName;

    // === Mottagare ===

    /** Mottagande enhet ID */
    @Column(name = "receiver_unit_id")
    private UUID receiverUnitId;

    /** Mottagande enhet HSA-ID */
    @Column(name = "receiver_unit_hsa_id", length = 50)
    private String receiverUnitHsaId;

    /** Mottagande enhet namn */
    @Column(name = "receiver_unit_name", length = 200)
    private String receiverUnitName;

    /** Önskad specialitet (om ingen specifik mottagare) */
    @Column(name = "requested_specialty", length = 100)
    private String requestedSpecialty;

    // === Remissinnehåll ===

    /** Frågeställning/önskemål */
    @Column(name = "reason", nullable = false, length = 2000)
    private String reason;

    /** Diagnoskod (ICD-10) */
    @Column(name = "diagnosis_code", length = 20)
    private String diagnosisCode;

    /** Diagnostext */
    @Column(name = "diagnosis_text", length = 500)
    private String diagnosisText;

    /** Anamnes/bakgrund */
    @Column(name = "clinical_history", length = 4000)
    private String clinicalHistory;

    /** Aktuellt status */
    @Column(name = "current_status", length = 2000)
    private String currentStatus;

    /** Genomförda undersökningar */
    @Column(name = "examinations_done", length = 2000)
    private String examinationsDone;

    /** Aktuell medicinering */
    @Column(name = "current_medication", length = 2000)
    private String currentMedication;

    /** Allergier */
    @Column(name = "allergies", length = 1000)
    private String allergies;

    /** Övrigt att beakta */
    @Column(name = "additional_info", length = 2000)
    private String additionalInfo;

    // === Bilagor ===

    /** Bifogade dokument-ID:n (kommaseparerade) */
    @Column(name = "attachment_ids", length = 1000)
    private String attachmentIds;

    // === Tidsstämplar och hantering ===

    /** Vårdkontakt där remissen skapades */
    @Column(name = "source_encounter_id")
    private UUID sourceEncounterId;

    /** Vårdkontakt skapad av remissen */
    @Column(name = "resulting_encounter_id")
    private UUID resultingEncounterId;

    /** Önskat datum för besök */
    @Column(name = "requested_date")
    private LocalDate requestedDate;

    /** Sista giltighetsdag */
    @Column(name = "valid_until")
    private LocalDate validUntil;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "received_at")
    private Instant receivedAt;

    @Column(name = "assessed_at")
    private Instant assessedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    /** Bedömare ID */
    @Column(name = "assessor_id")
    private UUID assessorId;

    /** Bedömare namn */
    @Column(name = "assessor_name", length = 200)
    private String assessorName;

    /** Remissvar */
    @OneToMany(mappedBy = "referral", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReferralResponse> responses = new ArrayList<>();

    protected Referral() {
    }

    public Referral(UUID patientId, UUID senderUnitId, UUID senderPractitionerId, ReferralType referralType, String reason) {
        this.patientId = patientId;
        this.senderUnitId = senderUnitId;
        this.senderPractitionerId = senderPractitionerId;
        this.referralType = referralType;
        this.reason = reason;
        this.status = ReferralStatus.DRAFT;
        this.priority = ReferralPriority.ROUTINE;
        this.createdAt = Instant.now();
        this.referralReference = generateReferralReference();
    }

    private String generateReferralReference() {
        // Format: REM-YYYYMMDD-XXXX (t.ex. REM-20240403-A7B2)
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return "REM-" + datePart + "-" + randomPart;
    }

    // === Affärslogik ===

    /**
     * Skicka remissen.
     */
    public void send() {
        if (status != ReferralStatus.DRAFT && status != ReferralStatus.PENDING_INFORMATION) {
            throw new IllegalStateException("Kan endast skicka remiss i status DRAFT eller PENDING_INFORMATION");
        }
        if (receiverUnitId == null && requestedSpecialty == null) {
            throw new IllegalStateException("Mottagare eller önskad specialitet måste anges");
        }
        this.status = ReferralStatus.SENT;
        this.sentAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Markera som mottagen.
     */
    public void markReceived() {
        if (status != ReferralStatus.SENT && status != ReferralStatus.FORWARDED) {
            throw new IllegalStateException("Kan endast markera skickad eller vidareskickad remiss som mottagen");
        }
        this.status = ReferralStatus.RECEIVED;
        this.receivedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Starta bedömning.
     */
    public void startAssessment(UUID assessorId, String assessorName) {
        if (status != ReferralStatus.RECEIVED) {
            throw new IllegalStateException("Kan endast starta bedömning av mottagen remiss");
        }
        this.status = ReferralStatus.UNDER_ASSESSMENT;
        this.assessorId = assessorId;
        this.assessorName = assessorName;
        this.updatedAt = Instant.now();
    }

    /**
     * Acceptera remissen.
     */
    public void accept(ReferralPriority assessedPriority, LocalDate plannedDate) {
        if (status != ReferralStatus.UNDER_ASSESSMENT && status != ReferralStatus.RECEIVED) {
            throw new IllegalStateException("Kan endast acceptera remiss under bedömning eller mottagen");
        }
        this.status = ReferralStatus.ACCEPTED;
        this.priority = assessedPriority;
        this.requestedDate = plannedDate;
        this.assessedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Avvisa remissen.
     */
    public void reject(String rejectionReason) {
        if (status != ReferralStatus.UNDER_ASSESSMENT && status != ReferralStatus.RECEIVED) {
            throw new IllegalStateException("Kan endast avvisa remiss under bedömning eller mottagen");
        }
        this.status = ReferralStatus.REJECTED;
        this.assessedAt = Instant.now();
        this.updatedAt = Instant.now();
        // Rejektionsorsak sparas i ReferralResponse
    }

    /**
     * Begär komplettering.
     */
    public void requestMoreInformation(String requestedInfo) {
        if (status != ReferralStatus.UNDER_ASSESSMENT && status != ReferralStatus.RECEIVED) {
            throw new IllegalStateException("Kan endast begära komplettering för remiss under bedömning");
        }
        this.status = ReferralStatus.PENDING_INFORMATION;
        this.updatedAt = Instant.now();
        // Info om begärd komplettering sparas i ReferralResponse
    }

    /**
     * Vidareskicka till annan enhet.
     */
    public void forward(UUID newReceiverUnitId, String newReceiverUnitName, String forwardReason) {
        if (status != ReferralStatus.RECEIVED && status != ReferralStatus.UNDER_ASSESSMENT) {
            throw new IllegalStateException("Kan endast vidareskicka mottagen eller under bedömning");
        }
        this.receiverUnitId = newReceiverUnitId;
        this.receiverUnitName = newReceiverUnitName;
        this.status = ReferralStatus.FORWARDED;
        this.updatedAt = Instant.now();
    }

    /**
     * Avsluta remissen (besök genomfört).
     */
    public void complete(UUID resultingEncounterId) {
        if (status != ReferralStatus.ACCEPTED) {
            throw new IllegalStateException("Kan endast avsluta accepterad remiss");
        }
        this.status = ReferralStatus.COMPLETED;
        this.resultingEncounterId = resultingEncounterId;
        this.completedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Makulera remissen.
     */
    public void cancel(String cancellationReason) {
        if (status == ReferralStatus.COMPLETED || status == ReferralStatus.CANCELLED) {
            throw new IllegalStateException("Kan ej makulera avslutad eller redan makulerad remiss");
        }
        this.status = ReferralStatus.CANCELLED;
        this.updatedAt = Instant.now();
    }

    /**
     * Kontrollera om remissen kan redigeras.
     */
    public boolean isEditable() {
        return status == ReferralStatus.DRAFT || status == ReferralStatus.PENDING_INFORMATION;
    }

    /**
     * Kontrollera om remissen väntar på svar.
     */
    public boolean isAwaitingResponse() {
        return status == ReferralStatus.SENT || status == ReferralStatus.FORWARDED;
    }

    /**
     * Lägg till svar.
     */
    public void addResponse(ReferralResponse response) {
        responses.add(response);
        response.setReferral(this);
    }

    // === Getters och setters ===

    public UUID getId() {
        return id;
    }

    public String getReferralReference() {
        return referralReference;
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

    public ReferralType getReferralType() {
        return referralType;
    }

    public void setReferralType(ReferralType referralType) {
        this.referralType = referralType;
    }

    public ReferralStatus getStatus() {
        return status;
    }

    public ReferralPriority getPriority() {
        return priority;
    }

    public void setPriority(ReferralPriority priority) {
        this.priority = priority;
    }

    public UUID getSenderUnitId() {
        return senderUnitId;
    }

    public String getSenderUnitHsaId() {
        return senderUnitHsaId;
    }

    public void setSenderUnitHsaId(String senderUnitHsaId) {
        this.senderUnitHsaId = senderUnitHsaId;
    }

    public String getSenderUnitName() {
        return senderUnitName;
    }

    public void setSenderUnitName(String senderUnitName) {
        this.senderUnitName = senderUnitName;
    }

    public UUID getSenderPractitionerId() {
        return senderPractitionerId;
    }

    public String getSenderPractitionerHsaId() {
        return senderPractitionerHsaId;
    }

    public void setSenderPractitionerHsaId(String senderPractitionerHsaId) {
        this.senderPractitionerHsaId = senderPractitionerHsaId;
    }

    public String getSenderPractitionerName() {
        return senderPractitionerName;
    }

    public void setSenderPractitionerName(String senderPractitionerName) {
        this.senderPractitionerName = senderPractitionerName;
    }

    public UUID getReceiverUnitId() {
        return receiverUnitId;
    }

    public void setReceiverUnitId(UUID receiverUnitId) {
        this.receiverUnitId = receiverUnitId;
    }

    public String getReceiverUnitHsaId() {
        return receiverUnitHsaId;
    }

    public void setReceiverUnitHsaId(String receiverUnitHsaId) {
        this.receiverUnitHsaId = receiverUnitHsaId;
    }

    public String getReceiverUnitName() {
        return receiverUnitName;
    }

    public void setReceiverUnitName(String receiverUnitName) {
        this.receiverUnitName = receiverUnitName;
    }

    public String getRequestedSpecialty() {
        return requestedSpecialty;
    }

    public void setRequestedSpecialty(String requestedSpecialty) {
        this.requestedSpecialty = requestedSpecialty;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
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

    public String getClinicalHistory() {
        return clinicalHistory;
    }

    public void setClinicalHistory(String clinicalHistory) {
        this.clinicalHistory = clinicalHistory;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }

    public String getExaminationsDone() {
        return examinationsDone;
    }

    public void setExaminationsDone(String examinationsDone) {
        this.examinationsDone = examinationsDone;
    }

    public String getCurrentMedication() {
        return currentMedication;
    }

    public void setCurrentMedication(String currentMedication) {
        this.currentMedication = currentMedication;
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public String getAttachmentIds() {
        return attachmentIds;
    }

    public void setAttachmentIds(String attachmentIds) {
        this.attachmentIds = attachmentIds;
    }

    public UUID getSourceEncounterId() {
        return sourceEncounterId;
    }

    public void setSourceEncounterId(UUID sourceEncounterId) {
        this.sourceEncounterId = sourceEncounterId;
    }

    public UUID getResultingEncounterId() {
        return resultingEncounterId;
    }

    public LocalDate getRequestedDate() {
        return requestedDate;
    }

    public void setRequestedDate(LocalDate requestedDate) {
        this.requestedDate = requestedDate;
    }

    public LocalDate getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(LocalDate validUntil) {
        this.validUntil = validUntil;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public Instant getAssessedAt() {
        return assessedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public UUID getAssessorId() {
        return assessorId;
    }

    public String getAssessorName() {
        return assessorName;
    }

    public List<ReferralResponse> getResponses() {
        return responses;
    }
}
