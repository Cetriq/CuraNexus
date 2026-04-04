package se.curanexus.coding.api.dto;

import se.curanexus.coding.domain.ProcedureCode;

import java.util.UUID;

public record ProcedureCodeDto(
        UUID id,
        String code,
        String displayName,
        String swedishName,
        String category,
        String categoryName,
        String parentCode,
        Integer level,
        boolean leaf,
        String performerType,
        boolean requiresLaterality
) {
    public static ProcedureCodeDto from(ProcedureCode entity) {
        return new ProcedureCodeDto(
                entity.getId(),
                entity.getCode(),
                entity.getDisplayName(),
                entity.getSwedishName(),
                entity.getCategory(),
                entity.getCategoryName(),
                entity.getParentCode(),
                entity.getLevel(),
                entity.isLeaf(),
                entity.getPerformerType() != null ? entity.getPerformerType().name() : null,
                entity.isRequiresLaterality()
        );
    }
}
