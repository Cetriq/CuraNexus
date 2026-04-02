package se.curanexus.task.api.dto;

import se.curanexus.task.domain.Reminder;
import se.curanexus.task.domain.ReminderStatus;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record ReminderResponse(
        UUID id,
        UUID userId,
        String message,
        ReminderStatus status,
        LocalDateTime remindAt,
        UUID patientId,
        UUID encounterId,
        UUID taskId,
        boolean recurring,
        String recurrencePattern,
        Instant acknowledgedAt,
        LocalDateTime snoozedUntil,
        Instant createdAt
) {
    public static ReminderResponse from(Reminder reminder) {
        return new ReminderResponse(
                reminder.getId(),
                reminder.getUserId(),
                reminder.getMessage(),
                reminder.getStatus(),
                reminder.getRemindAt(),
                reminder.getPatientId(),
                reminder.getEncounterId(),
                reminder.getTaskId(),
                reminder.isRecurring(),
                reminder.getRecurrencePattern(),
                reminder.getAcknowledgedAt(),
                reminder.getSnoozedUntil(),
                reminder.getCreatedAt()
        );
    }
}
