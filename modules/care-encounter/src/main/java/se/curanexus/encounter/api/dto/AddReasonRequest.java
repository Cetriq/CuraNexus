package se.curanexus.encounter.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import se.curanexus.encounter.domain.ReasonType;

public record AddReasonRequest(
        @NotNull(message = "Reason type is required")
        ReasonType type,

        @Size(max = 20, message = "Code must not exceed 20 characters")
        String code,

        @Size(max = 50, message = "Code system must not exceed 50 characters")
        String codeSystem,

        @Size(max = 500, message = "Display text must not exceed 500 characters")
        String displayText,

        Boolean isPrimary
) {
}
