package se.curanexus.triage.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "triage_assessments", indexes = {
    @Index(name = "idx_triage_patient", columnList = "patient_id"),
    @Index(name = "idx_triage_encounter", columnList = "encounter_id"),
    @Index(name = "idx_triage_priority", columnList = "priority"),
    @Index(name = "idx_triage_status", columnList = "status"),
    @Index(name = "idx_triage_arrival", columnList = "arrival_time")
})
public class TriageAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Column(name = "encounter_id", nullable = false)
    private UUID encounterId;

    @Column(name = "triage_nurse_id", nullable = false)
    private UUID triageNurseId;

    @Column(name = "triage_nurse_name", length = 200)
    private String triageNurseName;

    @Column(name = "chief_complaint", nullable = false, length = 500)
    private String chiefComplaint;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 20)
    private TriagePriority priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "care_level", length = 20)
    private CareLevel careLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "disposition", length = 30)
    private Disposition disposition;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 25)
    private AssessmentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "arrival_mode", length = 20)
    private ArrivalMode arrivalMode;

    @Column(name = "location_id")
    private UUID locationId;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "arrival_time", nullable = false)
    private Instant arrivalTime;

    @Column(name = "triage_start_time")
    private Instant triageStartTime;

    @Column(name = "triage_end_time")
    private Instant triageEndTime;

    @OneToMany(mappedBy = "assessment", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("recordedAt ASC")
    private List<Symptom> symptoms = new ArrayList<>();

    @OneToOne(mappedBy = "assessment", cascade = CascadeType.ALL, orphanRemoval = true)
    private VitalSigns vitalSigns;

    @OneToMany(mappedBy = "assessment", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("escalatedAt DESC")
    private List<EscalationRecord> escalationHistory = new ArrayList<>();

    @Column(name = "recommended_protocol_id")
    private UUID recommendedProtocolId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected TriageAssessment() {
    }

    public TriageAssessment(UUID patientId, UUID encounterId, UUID triageNurseId, String chiefComplaint) {
        if (patientId == null) {
            throw new IllegalArgumentException("Patient ID is required");
        }
        if (encounterId == null) {
            throw new IllegalArgumentException("Encounter ID is required");
        }
        if (triageNurseId == null) {
            throw new IllegalArgumentException("Triage nurse ID is required");
        }
        if (chiefComplaint == null || chiefComplaint.isBlank()) {
            throw new IllegalArgumentException("Chief complaint is required");
        }

        this.patientId = patientId;
        this.encounterId = encounterId;
        this.triageNurseId = triageNurseId;
        this.chiefComplaint = chiefComplaint;
        this.status = AssessmentStatus.IN_PROGRESS;
        this.arrivalTime = Instant.now();
        this.triageStartTime = Instant.now();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public void complete(TriagePriority priority, CareLevel careLevel, Disposition disposition) {
        if (priority == null) {
            throw new IllegalArgumentException("Priority is required to complete assessment");
        }
        if (careLevel == null) {
            throw new IllegalArgumentException("Care level is required to complete assessment");
        }
        if (disposition == null) {
            throw new IllegalArgumentException("Disposition is required to complete assessment");
        }

        this.priority = priority;
        this.careLevel = careLevel;
        this.disposition = disposition;
        this.status = AssessmentStatus.COMPLETED;
        this.triageEndTime = Instant.now();
    }

    public void escalate(TriagePriority newPriority, String reason, UUID escalatedBy) {
        if (newPriority == null) {
            throw new IllegalArgumentException("New priority is required");
        }
        if (!newPriority.isHigherThan(this.priority) && this.priority != null) {
            throw new IllegalArgumentException("Can only escalate to a higher priority");
        }

        EscalationRecord record = new EscalationRecord(this, this.priority, newPriority, reason, escalatedBy);
        this.escalationHistory.add(record);
        this.priority = newPriority;
    }

    public void addSymptom(Symptom symptom) {
        symptoms.add(symptom);
        symptom.setAssessment(this);
    }

    public void setVitalSigns(VitalSigns vitalSigns) {
        this.vitalSigns = vitalSigns;
        vitalSigns.setAssessment(this);
    }

    public int getWaitTimeMinutes() {
        Instant endTime = triageEndTime != null ? triageEndTime : Instant.now();
        return (int) ((endTime.toEpochMilli() - arrivalTime.toEpochMilli()) / 60000);
    }

    public boolean isOverdue() {
        if (priority == null || status == AssessmentStatus.COMPLETED) {
            return false;
        }
        return getWaitTimeMinutes() > priority.getMaxWaitMinutes();
    }

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public UUID getEncounterId() {
        return encounterId;
    }

    public UUID getTriageNurseId() {
        return triageNurseId;
    }

    public String getTriageNurseName() {
        return triageNurseName;
    }

    public void setTriageNurseName(String triageNurseName) {
        this.triageNurseName = triageNurseName;
    }

    public String getChiefComplaint() {
        return chiefComplaint;
    }

    public void setChiefComplaint(String chiefComplaint) {
        this.chiefComplaint = chiefComplaint;
    }

    public TriagePriority getPriority() {
        return priority;
    }

    public void setPriority(TriagePriority priority) {
        this.priority = priority;
    }

    public CareLevel getCareLevel() {
        return careLevel;
    }

    public void setCareLevel(CareLevel careLevel) {
        this.careLevel = careLevel;
    }

    public Disposition getDisposition() {
        return disposition;
    }

    public void setDisposition(Disposition disposition) {
        this.disposition = disposition;
    }

    public AssessmentStatus getStatus() {
        return status;
    }

    public void setStatus(AssessmentStatus status) {
        this.status = status;
    }

    public ArrivalMode getArrivalMode() {
        return arrivalMode;
    }

    public void setArrivalMode(ArrivalMode arrivalMode) {
        this.arrivalMode = arrivalMode;
    }

    public UUID getLocationId() {
        return locationId;
    }

    public void setLocationId(UUID locationId) {
        this.locationId = locationId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Instant getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(Instant arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public Instant getTriageStartTime() {
        return triageStartTime;
    }

    public Instant getTriageEndTime() {
        return triageEndTime;
    }

    public List<Symptom> getSymptoms() {
        return symptoms;
    }

    public VitalSigns getVitalSigns() {
        return vitalSigns;
    }

    public List<EscalationRecord> getEscalationHistory() {
        return escalationHistory;
    }

    public UUID getRecommendedProtocolId() {
        return recommendedProtocolId;
    }

    public void setRecommendedProtocolId(UUID recommendedProtocolId) {
        this.recommendedProtocolId = recommendedProtocolId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
