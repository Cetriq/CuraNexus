package se.curanexus.triage.api.dto;

import jakarta.validation.constraints.Size;
import se.curanexus.triage.domain.CareLevel;
import se.curanexus.triage.domain.TriagePriority;

public record UpdateAssessmentRequest(
        @Size(max = 500)
        String chiefComplaint,

        String notes,

        TriagePriority priority,

        CareLevel careLevel
) {}
