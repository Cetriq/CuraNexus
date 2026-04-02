package se.curanexus.triage.service.exception;

import java.util.UUID;

public class AssessmentAlreadyExistsException extends RuntimeException {

    private final UUID encounterId;

    public AssessmentAlreadyExistsException(UUID encounterId) {
        super("Triage assessment already exists for encounter: " + encounterId);
        this.encounterId = encounterId;
    }

    public UUID getEncounterId() {
        return encounterId;
    }
}
