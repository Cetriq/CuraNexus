package se.curanexus.journal.service.exception;

import java.util.UUID;

public class DiagnosisNotFoundException extends RuntimeException {

    private final UUID diagnosisId;

    public DiagnosisNotFoundException(UUID diagnosisId) {
        super("Diagnosis not found: " + diagnosisId);
        this.diagnosisId = diagnosisId;
    }

    public UUID getDiagnosisId() {
        return diagnosisId;
    }
}
