package se.curanexus.triage.service.exception;

import java.util.UUID;

public class AssessmentAlreadyCompletedException extends RuntimeException {

    private final UUID assessmentId;

    public AssessmentAlreadyCompletedException(UUID assessmentId) {
        super("Triage assessment already completed: " + assessmentId);
        this.assessmentId = assessmentId;
    }

    public UUID getAssessmentId() {
        return assessmentId;
    }
}
