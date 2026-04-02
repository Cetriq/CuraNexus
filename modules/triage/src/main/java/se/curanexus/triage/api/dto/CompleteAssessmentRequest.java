package se.curanexus.triage.api.dto;

import jakarta.validation.constraints.NotNull;
import se.curanexus.triage.domain.CareLevel;
import se.curanexus.triage.domain.Disposition;
import se.curanexus.triage.domain.TriagePriority;

import java.util.UUID;

public record CompleteAssessmentRequest(
        @NotNull(message = "Priority is required")
        TriagePriority priority,

        @NotNull(message = "Care level is required")
        CareLevel careLevel,

        @NotNull(message = "Disposition is required")
        Disposition disposition,

        String notes,

        UUID recommendedProtocolId
) {}
