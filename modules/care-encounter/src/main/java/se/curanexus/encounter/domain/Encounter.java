package se.curanexus.encounter.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "encounters", indexes = {
    @Index(name = "idx_encounter_patient", columnList = "patient_id"),
    @Index(name = "idx_encounter_status", columnList = "status"),
    @Index(name = "idx_encounter_responsible_unit", columnList = "responsible_unit_id")
})
public class Encounter {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EncounterStatus status = EncounterStatus.PLANNED;

    @Enumerated(EnumType.STRING)
    @Column(name = "encounter_class", nullable = false, length = 20)
    private EncounterClass encounterClass;

    @Enumerated(EnumType.STRING)
    @Column(name = "encounter_type", length = 20)
    private EncounterType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 20)
    private EncounterPriority priority;

    @Column(name = "service_type", length = 100)
    private String serviceType;

    @Column(name = "responsible_unit_id")
    private UUID responsibleUnitId;

    @Column(name = "responsible_practitioner_id")
    private UUID responsiblePractitionerId;

    @Column(name = "planned_start_time")
    private Instant plannedStartTime;

    @Column(name = "planned_end_time")
    private Instant plannedEndTime;

    @Column(name = "actual_start_time")
    private Instant actualStartTime;

    @Column(name = "actual_end_time")
    private Instant actualEndTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "encounter", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Participant> participants = new ArrayList<>();

    @OneToMany(mappedBy = "encounter", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EncounterReason> reasons = new ArrayList<>();

    protected Encounter() {
    }

    public Encounter(UUID patientId, EncounterClass encounterClass) {
        this.patientId = patientId;
        this.encounterClass = encounterClass;
        this.status = EncounterStatus.PLANNED;
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

    public boolean canTransitionTo(EncounterStatus newStatus) {
        return switch (this.status) {
            case PLANNED -> newStatus == EncounterStatus.ARRIVED
                    || newStatus == EncounterStatus.CANCELLED;
            case ARRIVED -> newStatus == EncounterStatus.TRIAGED
                    || newStatus == EncounterStatus.IN_PROGRESS
                    || newStatus == EncounterStatus.CANCELLED;
            case TRIAGED -> newStatus == EncounterStatus.IN_PROGRESS
                    || newStatus == EncounterStatus.ON_HOLD
                    || newStatus == EncounterStatus.CANCELLED;
            case IN_PROGRESS -> newStatus == EncounterStatus.ON_HOLD
                    || newStatus == EncounterStatus.FINISHED
                    || newStatus == EncounterStatus.CANCELLED;
            case ON_HOLD -> newStatus == EncounterStatus.IN_PROGRESS
                    || newStatus == EncounterStatus.FINISHED
                    || newStatus == EncounterStatus.CANCELLED;
            case FINISHED, CANCELLED -> false;
        };
    }

    public void transitionTo(EncounterStatus newStatus) {
        if (!canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                    "Cannot transition from " + this.status + " to " + newStatus);
        }
        this.status = newStatus;

        if (newStatus == EncounterStatus.IN_PROGRESS && this.actualStartTime == null) {
            this.actualStartTime = Instant.now();
        }
        if (newStatus == EncounterStatus.FINISHED || newStatus == EncounterStatus.CANCELLED) {
            this.actualEndTime = Instant.now();
        }
    }

    public void addParticipant(Participant participant) {
        participants.add(participant);
        participant.setEncounter(this);
    }

    public void removeParticipant(Participant participant) {
        participants.remove(participant);
        participant.setEncounter(null);
    }

    public void addReason(EncounterReason reason) {
        reasons.add(reason);
        reason.setEncounter(this);
    }

    public void removeReason(EncounterReason reason) {
        reasons.remove(reason);
        reason.setEncounter(null);
    }

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public EncounterStatus getStatus() {
        return status;
    }

    public EncounterClass getEncounterClass() {
        return encounterClass;
    }

    public EncounterType getType() {
        return type;
    }

    public void setType(EncounterType type) {
        this.type = type;
    }

    public EncounterPriority getPriority() {
        return priority;
    }

    public void setPriority(EncounterPriority priority) {
        this.priority = priority;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public UUID getResponsibleUnitId() {
        return responsibleUnitId;
    }

    public void setResponsibleUnitId(UUID responsibleUnitId) {
        this.responsibleUnitId = responsibleUnitId;
    }

    public UUID getResponsiblePractitionerId() {
        return responsiblePractitionerId;
    }

    public void setResponsiblePractitionerId(UUID responsiblePractitionerId) {
        this.responsiblePractitionerId = responsiblePractitionerId;
    }

    public Instant getPlannedStartTime() {
        return plannedStartTime;
    }

    public void setPlannedStartTime(Instant plannedStartTime) {
        this.plannedStartTime = plannedStartTime;
    }

    public Instant getPlannedEndTime() {
        return plannedEndTime;
    }

    public void setPlannedEndTime(Instant plannedEndTime) {
        this.plannedEndTime = plannedEndTime;
    }

    public Instant getActualStartTime() {
        return actualStartTime;
    }

    public void setActualStartTime(Instant actualStartTime) {
        this.actualStartTime = actualStartTime;
    }

    public Instant getActualEndTime() {
        return actualEndTime;
    }

    public void setActualEndTime(Instant actualEndTime) {
        this.actualEndTime = actualEndTime;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<Participant> getParticipants() {
        return participants;
    }

    public List<EncounterReason> getReasons() {
        return reasons;
    }
}
