package se.curanexus.task.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateReminderRequest(
        @NotNull UUID userId,
        @NotBlank String message,
        @NotNull LocalDateTime remindAt,
        UUID patientId,
        UUID encounterId,
        UUID taskId,
        boolean recurring,
        String recurrencePattern
) {}
