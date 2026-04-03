package se.curanexus.events.task;

import se.curanexus.events.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when a new task is created.
 */
public class TaskCreatedEvent extends DomainEvent {

    private final UUID taskId;
    private final String title;
    private final String category;
    private final String priority;
    private final UUID createdById;
    private final UUID patientId;
    private final UUID encounterId;
    private final UUID assigneeId;
    private final LocalDateTime dueAt;

    public TaskCreatedEvent(
            Object source,
            UUID taskId,
            String title,
            String category,
            String priority,
            UUID createdById,
            UUID patientId,
            UUID encounterId,
            UUID assigneeId,
            LocalDateTime dueAt) {
        super(source);
        this.taskId = taskId;
        this.title = title;
        this.category = category;
        this.priority = priority;
        this.createdById = createdById;
        this.patientId = patientId;
        this.encounterId = encounterId;
        this.assigneeId = assigneeId;
        this.dueAt = dueAt;
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
        return "CREATED";
    }

    public UUID getTaskId() {
        return taskId;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public String getPriority() {
        return priority;
    }

    public UUID getCreatedById() {
        return createdById;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public UUID getEncounterId() {
        return encounterId;
    }

    public UUID getAssigneeId() {
        return assigneeId;
    }

    public LocalDateTime getDueAt() {
        return dueAt;
    }
}
