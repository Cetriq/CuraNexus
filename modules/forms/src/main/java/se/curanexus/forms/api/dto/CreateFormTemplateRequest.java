package se.curanexus.forms.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import se.curanexus.forms.domain.FormType;

import java.util.List;
import java.util.UUID;

public record CreateFormTemplateRequest(
        @NotBlank @Size(max = 50) String code,
        @NotBlank @Size(max = 200) String name,
        @Size(max = 1000) String description,
        @NotNull FormType type,
        @Size(max = 100) String category,
        Integer estimatedDurationMinutes,
        @Size(max = 2000) String instructions,
        @Size(max = 1000) String scoringFormula,
        UUID ownerUnitId,
        UUID createdBy,
        List<CreateFormFieldRequest> fields
) {
}
