package se.curanexus.medication.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import se.curanexus.medication.domain.DrugAllergy.AllergySeverity;
import se.curanexus.medication.domain.DrugAllergy.ReactionType;

import java.time.LocalDate;
import java.util.UUID;

public record CreateDrugAllergyRequest(
        @NotNull UUID patientId,
        UUID medicationId,
        String atcCode,
        @NotBlank String substanceName,
        @NotNull ReactionType reactionType,
        AllergySeverity severity,
        String reactionDescription,
        LocalDate onsetDate,
        String source,
        String notes
) {
}
