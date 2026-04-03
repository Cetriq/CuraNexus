package se.curanexus.booking.service;

import java.util.UUID;

/**
 * Exception som kastas när en tidslucka inte hittas.
 */
public class TimeSlotNotFoundException extends RuntimeException {

    private final UUID slotId;

    public TimeSlotNotFoundException(UUID slotId) {
        super("Time slot not found: " + slotId);
        this.slotId = slotId;
    }

    public UUID getSlotId() {
        return slotId;
    }
}
