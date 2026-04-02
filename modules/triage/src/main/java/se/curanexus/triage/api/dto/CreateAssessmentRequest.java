package se.curanexus.triage.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import se.curanexus.triage.domain.ArrivalMode;

import java.util.UUID;

public record CreateAssessmentRequest(
        @NotNull(message = "Patient ID is required")
        UUID patientId,

        @NotNull(message = "Encounter ID is required")
        UUID encounterId,

        @NotNull(message = "Triage nurse ID is required")
        UUID triageNurseId,

        @NotBlank(message = "Chief complaint is required")
        @Size(max = 500)
        String chiefComplaint,

        ArrivalMode arrivalMode,

        UUID locationId
) {}
