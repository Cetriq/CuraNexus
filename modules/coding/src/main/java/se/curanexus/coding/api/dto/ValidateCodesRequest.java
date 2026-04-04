package se.curanexus.coding.api.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ValidateCodesRequest(
        @NotEmpty(message = "At least one code must be provided")
        List<CodeToValidate> codes
) {
    public record CodeToValidate(
            String code,
            String codeSystem // ICD10_SE, KVA, ATC
    ) {}
}
