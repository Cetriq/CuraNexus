package se.curanexus.forms.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import se.curanexus.forms.domain.FieldType;

public record CreateFormFieldRequest(
        @NotBlank @Size(max = 100) String fieldKey,
        @NotNull FieldType fieldType,
        @NotBlank @Size(max = 500) String label,
        @Size(max = 1000) String description,
        @Size(max = 200) String placeholder,
        @Size(max = 500) String helpText,
        Integer sortOrder,
        Boolean required,
        @Size(max = 500) String defaultValue,
        String options,
        String validationRules,
        String conditionalRules,
        Integer minValue,
        Integer maxValue,
        Integer stepValue,
        @Size(max = 500) String scaleLabels,
        @Size(max = 50) String codeSystem,
        @Size(max = 50) String code,
        @Size(max = 50) String unit
) {
}
