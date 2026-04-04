package se.curanexus.forms.api.dto;

import se.curanexus.forms.domain.FieldType;
import se.curanexus.forms.domain.FormAnswer;

import java.time.Instant;
import java.util.UUID;

public record FormAnswerDto(
        UUID id,
        String fieldKey,
        FieldType fieldType,
        String valueText,
        Double valueNumber,
        Boolean valueBoolean,
        Instant valueDatetime,
        String valueArray,
        String fileReference,
        String codeSystem,
        String code,
        String codeDisplay,
        Instant answeredAt,
        Instant modifiedAt
) {
    public static FormAnswerDto from(FormAnswer answer) {
        return new FormAnswerDto(
                answer.getId(),
                answer.getFieldKey(),
                answer.getFieldType(),
                answer.getValueText(),
                answer.getValueNumber(),
                answer.getValueBoolean(),
                answer.getValueDatetime(),
                answer.getValueArray(),
                answer.getFileReference(),
                answer.getCodeSystem(),
                answer.getCode(),
                answer.getCodeDisplay(),
                answer.getAnsweredAt(),
                answer.getModifiedAt()
        );
    }
}
