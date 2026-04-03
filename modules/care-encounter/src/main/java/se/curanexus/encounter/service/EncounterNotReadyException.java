package se.curanexus.encounter.service;

import java.util.List;
import java.util.UUID;

/**
 * Exception thrown when attempting to finish an encounter that is not ready.
 */
public class EncounterNotReadyException extends RuntimeException {

    private final UUID encounterId;
    private final List<String> blockers;

    public EncounterNotReadyException(UUID encounterId, List<String> blockers) {
        super(String.format("Encounter %s is not ready to be finished: %s",
                encounterId, String.join("; ", blockers)));
        this.encounterId = encounterId;
        this.blockers = blockers;
    }

    public UUID getEncounterId() {
        return encounterId;
    }

    public List<String> getBlockers() {
        return blockers;
    }
}
