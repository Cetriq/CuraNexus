package se.curanexus.triage.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import se.curanexus.triage.domain.ConsciousnessLevel;

import java.util.UUID;

public record VitalSignsRequest(
        @Min(0) @Max(300)
        Integer bloodPressureSystolic,

        @Min(0) @Max(200)
        Integer bloodPressureDiastolic,

        @Min(0) @Max(300)
        Integer heartRate,

        @Min(0) @Max(60)
        Integer respiratoryRate,

        @Min(30) @Max(45)
        Double temperature,

        @Min(0) @Max(100)
        Integer oxygenSaturation,

        @Min(0) @Max(10)
        Integer painLevel,

        ConsciousnessLevel consciousnessLevel,

        Double glucoseLevel,

        UUID recordedBy
) {}
