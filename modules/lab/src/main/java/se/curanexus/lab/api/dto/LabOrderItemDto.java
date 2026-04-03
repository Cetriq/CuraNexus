package se.curanexus.lab.api.dto;

import se.curanexus.lab.domain.LabOrderItem;
import se.curanexus.lab.domain.SpecimenType;

import java.time.Instant;
import java.util.UUID;

public record LabOrderItemDto(
        UUID id,
        String testCode,
        String codeSystem,
        String testName,
        String testDescription,
        SpecimenType specimenType,
        String itemComment,
        Instant createdAt,
        LabResultDto result
) {
    public static LabOrderItemDto from(LabOrderItem item) {
        return new LabOrderItemDto(
                item.getId(),
                item.getTestCode(),
                item.getCodeSystem(),
                item.getTestName(),
                item.getTestDescription(),
                item.getSpecimenType(),
                item.getItemComment(),
                item.getCreatedAt(),
                item.getResult() != null ? LabResultDto.from(item.getResult()) : null
        );
    }
}
