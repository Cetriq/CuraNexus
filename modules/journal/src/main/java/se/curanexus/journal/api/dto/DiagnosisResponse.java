package se.curanexus.journal.api.dto;

import se.curanexus.journal.domain.Diagnosis;
import se.curanexus.journal.domain.DiagnosisType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record DiagnosisResponse(
        UUID id,
        UUID encounterId,
        UUID patientId,
        String code,
        String codeSystem,
        String displayText,
        DiagnosisType type,
        Integer rank,
        LocalDate onsetDate,
        LocalDate resolvedDate,
        Instant recordedAt,
        UUID recordedById
) {
    public static DiagnosisResponse from(Diagnosis diagnosis) {
        return new DiagnosisResponse(
                diagnosis.getId(),
                diagnosis.getEncounterId(),
                diagnosis.getPatientId(),
                diagnosis.getCode(),
                diagnosis.getCodeSystem(),
                diagnosis.getDisplayText(),
                diagnosis.getType(),
                diagnosis.getRank(),
                diagnosis.getOnsetDate(),
                diagnosis.getResolvedDate(),
                diagnosis.getRecordedAt(),
                diagnosis.getRecordedById()
        );
    }
}
