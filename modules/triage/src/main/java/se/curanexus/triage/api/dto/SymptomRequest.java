package se.curanexus.triage.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import se.curanexus.triage.domain.Severity;

import java.time.Instant;

public record SymptomRequest(
        @NotBlank(message = "Symptom code is required")
        @Size(max = 20)
        String symptomCode,

        @NotBlank(message = "Description is required")
        @Size(max = 500)
        String description,

        Instant onset,

        String duration,

        Severity severity,

        @Size(max = 100)
        String bodyLocation,

        boolean isChiefComplaint
) {}
