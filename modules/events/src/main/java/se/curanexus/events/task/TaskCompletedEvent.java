package se.curanexus.events.task;

import se.curanexus.events.DomainEvent;

import java.util.UUID;

/**
 * Event published when a task is completed.
 */
public class TaskCompletedEvent extends DomainEvent {

    private final UUID taskId;
    private final UUID patientId;
    private final UUID encounterId;
    private final UUID completedById;
    private final String notes;

    public TaskCompletedEvent(
            Object source,
            UUID taskId,
            UUID patientId,
            UUID encounterId,
            UUID completedById,
            String notes) {
        super(source);
        this.taskId = taskId;
        this.patientId = patientId;
        this.encounterId = encounterId;
        this.completedById = completedById;
        this.notes = notes;
    }

    @Override
    public UUID getAggregateId() {
        return taskId;
    }

    @Override
    public String getAggregateType() {
        return "TASK";
    }

    @Override
    public String getEventType() {
        return "COMPLETED";
    }

    public UUID getTaskId() {
        return taskId;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public UUID getEncounterId() {
        return encounterId;
    }

    public UUID getCompletedById() {
        return completedById;
    }

    public String getNotes() {
        return notes;
    }
}
