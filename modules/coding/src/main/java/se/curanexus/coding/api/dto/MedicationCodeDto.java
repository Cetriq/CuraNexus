package se.curanexus.coding.api.dto;

import se.curanexus.coding.domain.MedicationCode;

import java.util.UUID;

public record MedicationCodeDto(
        UUID id,
        String code,
        String displayName,
        String swedishName,
        Integer level,
        String parentCode,
        String anatomicalGroup,
        String therapeuticGroup,
        String pharmacologicalGroup,
        String chemicalGroup,
        Double dddValue,
        String dddUnit,
        String administrationRoute
) {
    public static MedicationCodeDto from(MedicationCode entity) {
        return new MedicationCodeDto(
                entity.getId(),
                entity.getCode(),
                entity.getDisplayName(),
                entity.getSwedishName(),
                entity.getLevel(),
                entity.getParentCode(),
                entity.getAnatomicalGroup(),
                entity.getTherapeuticGroup(),
                entity.getPharmacologicalGroup(),
                entity.getChemicalGroup(),
                entity.getDddValue(),
                entity.getDddUnit(),
                entity.getAdministrationRoute()
        );
    }
}
