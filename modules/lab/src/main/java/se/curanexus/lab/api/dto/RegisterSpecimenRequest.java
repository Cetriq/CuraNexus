package se.curanexus.lab.api.dto;

import jakarta.validation.constraints.NotNull;
import se.curanexus.lab.domain.SpecimenType;

public record RegisterSpecimenRequest(
        @NotNull SpecimenType specimenType,
        String barcode,
        String collectionMethod,
        String bodySite,
        String quantity,
        String containerType,
        String comment
) {
}
