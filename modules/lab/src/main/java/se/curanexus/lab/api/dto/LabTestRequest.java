package se.curanexus.lab.api.dto;

import jakarta.validation.constraints.NotBlank;
import se.curanexus.lab.domain.SpecimenType;

public record LabTestRequest(
        @NotBlank String testCode,
        String codeSystem,
        @NotBlank String testName,
        String testDescription,
        SpecimenType specimenType,
        String comment
) {
}
