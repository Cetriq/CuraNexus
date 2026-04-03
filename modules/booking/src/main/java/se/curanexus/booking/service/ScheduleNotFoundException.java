package se.curanexus.booking.service;

import java.util.UUID;

/**
 * Exception som kastas när ett schema inte hittas.
 */
public class ScheduleNotFoundException extends RuntimeException {

    private final UUID scheduleId;

    public ScheduleNotFoundException(UUID scheduleId) {
        super("Schedule not found: " + scheduleId);
        this.scheduleId = scheduleId;
    }

    public UUID getScheduleId() {
        return scheduleId;
    }
}
