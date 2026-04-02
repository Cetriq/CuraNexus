package se.curanexus.triage.api.dto;

import se.curanexus.triage.domain.ConsciousnessLevel;
import se.curanexus.triage.domain.VitalSigns;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record VitalSignsResponse(
        UUID id,
        Integer bloodPressureSystolic,
        Integer bloodPressureDiastolic,
        Integer heartRate,
        Integer respiratoryRate,
        Double temperature,
        Integer oxygenSaturation,
        Integer painLevel,
        ConsciousnessLevel consciousnessLevel,
        Double glucoseLevel,
        List<VitalSignWarningResponse> warnings,
        Instant recordedAt,
        UUID recordedBy
) {
    public record VitalSignWarningResponse(String parameter, String severity, String message) {}

    public static VitalSignsResponse fromEntity(VitalSigns vs) {
        List<VitalSignWarningResponse> warnings = vs.checkWarnings().stream()
                .map(w -> new VitalSignWarningResponse(w.parameter(), w.severity(), w.message()))
                .toList();

        return new VitalSignsResponse(
                vs.getId(),
                vs.getBloodPressureSystolic(),
                vs.getBloodPressureDiastolic(),
                vs.getHeartRate(),
                vs.getRespiratoryRate(),
                vs.getTemperature(),
                vs.getOxygenSaturation(),
                vs.getPainLevel(),
                vs.getConsciousnessLevel(),
                vs.getGlucoseLevel(),
                warnings,
                vs.getRecordedAt(),
                vs.getRecordedBy()
        );
    }
}
