package se.curanexus.forms.api.dto;

import se.curanexus.forms.domain.FormStatus;
import se.curanexus.forms.domain.FormTemplate;
import se.curanexus.forms.domain.FormType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record FormTemplateDto(
        UUID id,
        String code,
        Integer version,
        String name,
        String description,
        FormType type,
        FormStatus status,
        String category,
        Integer estimatedDurationMinutes,
        String instructions,
        String scoringFormula,
        UUID ownerUnitId,
        UUID createdBy,
        Instant createdAt,
        Instant updatedAt,
        Instant publishedAt,
        List<FormFieldDto> fields
) {
    public static FormTemplateDto from(FormTemplate template) {
        return new FormTemplateDto(
                template.getId(),
                template.getCode(),
                template.getVersion(),
                template.getName(),
                template.getDescription(),
                template.getType(),
                template.getStatus(),
                template.getCategory(),
                template.getEstimatedDurationMinutes(),
                template.getInstructions(),
                template.getScoringFormula(),
                template.getOwnerUnitId(),
                template.getCreatedBy(),
                template.getCreatedAt(),
                template.getUpdatedAt(),
                template.getPublishedAt(),
                template.getFields().stream().map(FormFieldDto::from).toList()
        );
    }
}
