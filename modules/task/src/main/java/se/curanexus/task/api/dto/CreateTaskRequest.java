package se.curanexus.task.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import se.curanexus.task.domain.TaskCategory;
import se.curanexus.task.domain.TaskPriority;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateTaskRequest(
        @NotBlank String title,
        String description,
        @NotNull TaskCategory category,
        @NotNull TaskPriority priority,
        UUID patientId,
        UUID encounterId,
        UUID assigneeId,
        @NotNull UUID createdById,
        LocalDateTime dueAt,
        String sourceType,
        UUID sourceId
) {}
