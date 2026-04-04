package se.curanexus.coding.api.dto;

import se.curanexus.coding.domain.DiagnosisCode;

import java.util.UUID;

public record DiagnosisCodeDto(
        UUID id,
        String code,
        String displayName,
        String swedishName,
        String chapter,
        String chapterName,
        String block,
        String parentCode,
        Integer level,
        boolean leaf,
        String genderRestriction,
        Integer ageMin,
        Integer ageMax
) {
    public static DiagnosisCodeDto from(DiagnosisCode entity) {
        return new DiagnosisCodeDto(
                entity.getId(),
                entity.getCode(),
                entity.getDisplayName(),
                entity.getSwedishName(),
                entity.getChapter(),
                entity.getChapterName(),
                entity.getBlock(),
                entity.getParentCode(),
                entity.getLevel(),
                entity.isLeaf(),
                entity.getGenderRestriction(),
                entity.getAgeMin(),
                entity.getAgeMax()
        );
    }
}
