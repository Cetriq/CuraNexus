package se.curanexus.task.api.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record SnoozeReminderRequest(
        @NotNull LocalDateTime snoozeUntil
) {}
