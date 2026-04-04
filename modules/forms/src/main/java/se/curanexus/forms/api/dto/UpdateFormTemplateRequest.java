package se.curanexus.forms.api.dto;

import jakarta.validation.constraints.Size;

public record UpdateFormTemplateRequest(
        @Size(max = 200) String name,
        @Size(max = 1000) String description,
        @Size(max = 100) String category,
        Integer estimatedDurationMinutes,
        @Size(max = 2000) String instructions,
        @Size(max = 1000) String scoringFormula
) {
}
