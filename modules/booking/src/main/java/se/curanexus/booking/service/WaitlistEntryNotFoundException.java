package se.curanexus.booking.service;

import java.util.UUID;

/**
 * Exception som kastas när en väntelistpost inte hittas.
 */
public class WaitlistEntryNotFoundException extends RuntimeException {

    private final UUID entryId;

    public WaitlistEntryNotFoundException(UUID entryId) {
        super("Waitlist entry not found: " + entryId);
        this.entryId = entryId;
    }

    public UUID getEntryId() {
        return entryId;
    }
}
