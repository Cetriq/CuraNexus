package se.curanexus.triage.service.exception;

import java.util.UUID;

public class AssessmentNotFoundException extends RuntimeException {

    private final UUID assessmentId;

    public AssessmentNotFoundException(UUID assessmentId) {
        super("Triage assessment not found: " + assessmentId);
        this.assessmentId = assessmentId;
    }

    public UUID getAssessmentId() {
        return assessmentId;
    }
}
