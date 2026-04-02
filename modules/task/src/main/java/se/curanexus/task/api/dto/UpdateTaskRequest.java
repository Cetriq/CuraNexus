package se.curanexus.task.api.dto;

import se.curanexus.task.domain.TaskPriority;

import java.time.LocalDateTime;

public record UpdateTaskRequest(
        String title,
        String description,
        TaskPriority priority,
        LocalDateTime dueAt
) {}
