package se.curanexus.coding.api.dto;

import se.curanexus.coding.domain.CodeSystem;

import java.time.LocalDate;
import java.util.UUID;

public record CodeSystemDto(
        UUID id,
        String type,
        String displayName,
        String description,
        String systemUri,
        String version,
        LocalDate validFrom,
        LocalDate validTo,
        boolean active
) {
    public static CodeSystemDto from(CodeSystem entity) {
        return new CodeSystemDto(
                entity.getId(),
                entity.getType().name(),
                entity.getType().getDisplayName(),
                entity.getType().getDescription(),
                entity.getType().getSystemUri(),
                entity.getVersion(),
                entity.getValidFrom(),
                entity.getValidTo(),
                entity.isActive()
        );
    }
}
