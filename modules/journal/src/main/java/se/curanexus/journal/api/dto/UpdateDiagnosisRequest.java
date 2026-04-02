package se.curanexus.journal.api.dto;

import se.curanexus.journal.domain.DiagnosisType;

public record UpdateDiagnosisRequest(
        String code,
        String displayText,
        DiagnosisType type,
        Integer rank
) {}
