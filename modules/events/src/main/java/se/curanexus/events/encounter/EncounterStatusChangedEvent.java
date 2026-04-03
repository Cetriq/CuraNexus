package se.curanexus.events.encounter;

import se.curanexus.events.DomainEvent;

import java.util.UUID;

/**
 * Event published when an encounter's status changes.
 */
public class EncounterStatusChangedEvent extends DomainEvent {

    private final UUID encounterId;
    private final UUID patientId;
    private final String oldStatus;
    private final String newStatus;
    private final UUID changedById;

    public EncounterStatusChangedEvent(
            Object source,
            UUID encounterId,
            UUID patientId,
            String oldStatus,
            String newStatus,
            UUID changedById) {
        super(source);
        this.encounterId = encounterId;
        this.patientId = patientId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.changedById = changedById;
    }

    @Override
    public UUID getAggregateId() {
        return encounterId;
    }

    @Override
    public String getAggregateType() {
        return "ENCOUNTER";
    }

    @Override
    public String getEventType() {
        return "STATUS_CHANGED";
    }

    public UUID getEncounterId() {
        return encounterId;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public String getOldStatus() {
        return oldStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public UUID getChangedById() {
        return changedById;
    }

    /**
     * Check if the encounter was completed.
     */
    public boolean wasCompleted() {
        return "FINISHED".equals(newStatus);
    }

    /**
     * Check if the encounter was cancelled.
     */
    public boolean wasCancelled() {
        return "CANCELLED".equals(newStatus);
    }
}
