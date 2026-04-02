package se.curanexus.triage.api.dto;

import jakarta.validation.constraints.NotEmpty;
import se.curanexus.triage.domain.ConsciousnessLevel;
import se.curanexus.triage.domain.Severity;

import java.util.List;

public record DecisionSupportRequest(
        Integer patientAge,

        String patientSex,

        @NotEmpty(message = "At least one symptom is required")
        List<SymptomInput> symptoms,

        VitalSignsInput vitalSigns,

        List<String> medicalHistory,

        List<String> currentMedications
) {
    public record SymptomInput(
            String symptomCode,
            String description,
            Severity severity
    ) {}

    public record VitalSignsInput(
            Integer bloodPressureSystolic,
            Integer bloodPressureDiastolic,
            Integer heartRate,
            Integer respiratoryRate,
            Double temperature,
            Integer oxygenSaturation,
            Integer painLevel,
            ConsciousnessLevel consciousnessLevel,
            Double glucoseLevel
    ) {}
}
