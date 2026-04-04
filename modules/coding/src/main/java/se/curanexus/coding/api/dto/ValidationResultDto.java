package se.curanexus.coding.api.dto;

import java.util.List;

public record ValidationResultDto(
        boolean valid,
        List<CodeValidation> codes
) {
    public record CodeValidation(
            String code,
            String codeSystem,
            boolean valid,
            String displayName,
            String errorMessage
    ) {}
}
