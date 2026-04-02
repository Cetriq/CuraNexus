package se.curanexus.journal.service.exception;

import java.util.UUID;

public class ObservationNotFoundException extends RuntimeException {

    private final UUID observationId;

    public ObservationNotFoundException(UUID observationId) {
        super("Observation not found: " + observationId);
        this.observationId = observationId;
    }

    public UUID getObservationId() {
        return observationId;
    }
}
