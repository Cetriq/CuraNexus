package se.curanexus.forms.api.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public record AnswerRequest(
        @NotBlank String fieldKey,
        String valueText,
        Double valueNumber,
        Boolean valueBoolean,
        Instant valueDatetime,
        String valueArray,
        String fileReference,
        String codeSystem,
        String code,
        String codeDisplay
) {
}
