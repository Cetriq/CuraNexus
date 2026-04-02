package se.curanexus.journal.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import se.curanexus.journal.domain.DiagnosisType;

import java.time.LocalDate;
import java.util.UUID;

public record CreateDiagnosisRequest(
        @NotNull UUID encounterId,
        @NotNull UUID patientId,
        @NotBlank String code,
        String codeSystem,
        String displayText,
        DiagnosisType type,
        Integer rank,
        LocalDate onsetDate,
        UUID recordedById
) {}
