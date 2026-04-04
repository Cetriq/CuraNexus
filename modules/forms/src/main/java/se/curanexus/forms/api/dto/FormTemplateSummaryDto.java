package se.curanexus.forms.api.dto;

import se.curanexus.forms.domain.FormStatus;
import se.curanexus.forms.domain.FormTemplate;
import se.curanexus.forms.domain.FormType;

import java.util.UUID;

public record FormTemplateSummaryDto(
        UUID id,
        String code,
        Integer version,
        String name,
        String description,
        FormType type,
        FormStatus status,
        String category,
        Integer estimatedDurationMinutes,
        int fieldCount
) {
    public static FormTemplateSummaryDto from(FormTemplate template) {
        return new FormTemplateSummaryDto(
                template.getId(),
                template.getCode(),
                template.getVersion(),
                template.getName(),
                template.getDescription(),
                template.getType(),
                template.getStatus(),
                template.getCategory(),
                template.getEstimatedDurationMinutes(),
                template.getFields().size()
        );
    }
}
