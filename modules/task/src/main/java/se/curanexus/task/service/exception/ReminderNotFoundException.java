package se.curanexus.task.service.exception;

import java.util.UUID;

public class ReminderNotFoundException extends RuntimeException {

    private final UUID reminderId;

    public ReminderNotFoundException(UUID reminderId) {
        super("Reminder not found: " + reminderId);
        this.reminderId = reminderId;
    }

    public UUID getReminderId() {
        return reminderId;
    }
}
