package se.curanexus.task.api.dto;

import se.curanexus.task.domain.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        String title,
        String description,
        TaskCategory category,
        TaskStatus status,
        TaskPriority priority,
        UUID patientId,
        UUID encounterId,
        UUID assigneeId,
        UUID createdById,
        String sourceType,
        UUID sourceId,
        LocalDateTime dueAt,
        Instant startedAt,
        Instant completedAt,
        String completionNote,
        String outcome,
        String cancelReason,
        Instant createdAt,
        Instant updatedAt,
        boolean overdue
) {
    public static TaskResponse from(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getCategory(),
                task.getStatus(),
                task.getPriority(),
                task.getPatientId(),
                task.getEncounterId(),
                task.getAssigneeId(),
                task.getCreatedById(),
                task.getSourceType(),
                task.getSourceId(),
                task.getDueAt(),
                task.getStartedAt(),
                task.getCompletedAt(),
                task.getCompletionNote(),
                task.getOutcome(),
                task.getCancelReason(),
                task.getCreatedAt(),
                task.getUpdatedAt(),
                task.isOverdue()
        );
    }
}
