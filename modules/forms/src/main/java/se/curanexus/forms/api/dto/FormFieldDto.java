package se.curanexus.forms.api.dto;

import se.curanexus.forms.domain.FieldType;
import se.curanexus.forms.domain.FormField;

import java.util.UUID;

public record FormFieldDto(
        UUID id,
        String fieldKey,
        FieldType fieldType,
        String label,
        String description,
        String placeholder,
        String helpText,
        Integer sortOrder,
        boolean required,
        boolean readOnly,
        boolean hidden,
        String defaultValue,
        String options,
        String validationRules,
        String conditionalRules,
        Integer minValue,
        Integer maxValue,
        Integer stepValue,
        String scaleLabels,
        String codeSystem,
        String code,
        String unit
) {
    public static FormFieldDto from(FormField field) {
        return new FormFieldDto(
                field.getId(),
                field.getFieldKey(),
                field.getFieldType(),
                field.getLabel(),
                field.getDescription(),
                field.getPlaceholder(),
                field.getHelpText(),
                field.getSortOrder(),
                field.isRequired(),
                field.isReadOnly(),
                field.isHidden(),
                field.getDefaultValue(),
                field.getOptions(),
                field.getValidationRules(),
                field.getConditionalRules(),
                field.getMinValue(),
                field.getMaxValue(),
                field.getStepValue(),
                field.getScaleLabels(),
                field.getCodeSystem(),
                field.getCode(),
                field.getUnit()
        );
    }
}
