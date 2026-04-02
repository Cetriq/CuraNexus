package se.curanexus.triage.api.dto;

import se.curanexus.triage.domain.Severity;
import se.curanexus.triage.domain.Symptom;

import java.time.Instant;
import java.util.UUID;

public record SymptomResponse(
        UUID id,
        String symptomCode,
        String description,
        Instant onset,
        String duration,
        Severity severity,
        String bodyLocation,
        boolean isChiefComplaint,
        Instant recordedAt
) {
    public static SymptomResponse fromEntity(Symptom symptom) {
        return new SymptomResponse(
                symptom.getId(),
                symptom.getSymptomCode(),
                symptom.getDescription(),
                symptom.getOnset(),
                symptom.getDuration(),
                symptom.getSeverity(),
                symptom.getBodyLocation(),
                symptom.isChiefComplaint(),
                symptom.getRecordedAt()
        );
    }
}
