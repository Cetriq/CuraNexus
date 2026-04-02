package se.curanexus.triage.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("VitalSigns")
class VitalSignsTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("should create vital signs with recorded by")
        void shouldCreateVitalSignsWithRecordedBy() {
            UUID recordedBy = UUID.randomUUID();

            VitalSigns vitalSigns = new VitalSigns(recordedBy);

            assertEquals(recordedBy, vitalSigns.getRecordedBy());
            assertNotNull(vitalSigns.getRecordedAt());
        }
    }

    @Nested
    @DisplayName("checkWarnings - Blood Pressure")
    class BloodPressureWarnings {

        @Test
        @DisplayName("should detect hypertensive crisis")
        void shouldDetectHypertensiveCrisis() {
            VitalSigns vitalSigns = createVitalSigns();
            vitalSigns.setBloodPressureSystolic(185);

            List<VitalSigns.VitalSignWarning> warnings = vitalSigns.checkWarnings();

            assertTrue(warnings.stream().anyMatch(w ->
                    w.parameter().equals("bloodPressureSystolic") &&
                    w.severity().equals("CRITICAL") &&
                    w.message().contains("Hypertensive crisis")));
        }

        @Test
        @DisplayName("should detect hypotension")
        void shouldDetectHypotension() {
            VitalSigns vitalSigns = createVitalSigns();
            vitalSigns.setBloodPressureSystolic(85);

            List<VitalSigns.VitalSignWarning> warnings = vitalSigns.checkWarnings();

            assertTrue(warnings.stream().anyMatch(w ->
                    w.parameter().equals("bloodPressureSystolic") &&
                    w.severity().equals("HIGH") &&
                    w.message().contains("Hypotension")));
        }

        @Test
        @DisplayName("should not warn for normal blood pressure")
        void shouldNotWarnForNormalBloodPressure() {
            VitalSigns vitalSigns = createVitalSigns();
            vitalSigns.setBloodPressureSystolic(120);

            List<VitalSigns.VitalSignWarning> warnings = vitalSigns.checkWarnings();

            assertTrue(warnings.stream().noneMatch(w ->
                    w.parameter().equals("bloodPressureSystolic")));
        }
    }

    @Nested
    @DisplayName("checkWarnings - Heart Rate")
    class HeartRateWarnings {

        @Test
        @DisplayName("should detect severe tachycardia")
        void shouldDetectSevereTachycardia() {
            VitalSigns vitalSigns = createVitalSigns();
            vitalSigns.setHeartRate(155);

            List<VitalSigns.VitalSignWarning> warnings = vitalSigns.checkWarnings();

            assertTrue(warnings.stream().anyMatch(w ->
                    w.parameter().equals("heartRate") &&
                    w.severity().equals("CRITICAL") &&
                    w.message().contains("Severe tachycardia")));
        }

        @Test
        @DisplayName("should detect tachycardia")
        void shouldDetectTachycardia() {
            VitalSigns vitalSigns = createVitalSigns();
            vitalSigns.setHeartRate(125);

            List<VitalSigns.VitalSignWarning> warnings = vitalSigns.checkWarnings();

            assertTrue(warnings.stream().anyMatch(w ->
                    w.parameter().equals("heartRate") &&
                    w.severity().equals("HIGH") &&
                    w.message().contains("Tachycardia")));
        }

        @Test
        @DisplayName("should detect severe bradycardia")
        void shouldDetectSevereBradycardia() {
            VitalSigns vitalSigns = createVitalSigns();
            vitalSigns.setHeartRate(38);

            List<VitalSigns.VitalSignWarning> warnings = vitalSigns.checkWarnings();

            assertTrue(warnings.stream().anyMatch(w ->
                    w.parameter().equals("heartRate") &&
                    w.severity().equals("CRITICAL") &&
                    w.message().contains("Severe bradycardia")));
        }

        @Test
        @DisplayName("should detect bradycardia")
        void shouldDetectBradycardia() {
            VitalSigns vitalSigns = createVitalSigns();
            vitalSigns.setHeartRate(48);

            List<VitalSigns.VitalSignWarning> warnings = vitalSigns.checkWarnings();

            assertTrue(warnings.stream().anyMatch(w ->
                    w.parameter().equals("heartRate") &&
                    w.severity().equals("MEDIUM") &&
                    w.message().contains("Bradycardia")));
        }
    }

    @Nested
    @DisplayName("checkWarnings - Respiratory Rate")
    class RespiratoryRateWarnings {

        @Test
        @DisplayName("should detect severe tachypnea")
        void shouldDetectSevereTachypnea() {
            VitalSigns vitalSigns = createVitalSigns();
            vitalSigns.setRespiratoryRate(32);

            List<VitalSigns.VitalSignWarning> warnings = vitalSigns.checkWarnings();

            assertTrue(warnings.stream().anyMatch(w ->
                    w.parameter().equals("respiratoryRate") &&
                    w.severity().equals("CRITICAL")));
        }

        @Test
        @DisplayName("should detect bradypnea")
        void shouldDetectBradypnea() {
            VitalSigns vitalSigns = createVitalSigns();
            vitalSigns.setRespiratoryRate(6);

            List<VitalSigns.VitalSignWarning> warnings = vitalSigns.checkWarnings();

            assertTrue(warnings.stream().anyMatch(w ->
                    w.parameter().equals("respiratoryRate") &&
                    w.severity().equals("CRITICAL") &&
                    w.message().contains("Bradypnea")));
        }
    }

    @Nested
    @DisplayName("checkWarnings - Temperature")
    class TemperatureWarnings {

        @Test
        @DisplayName("should detect hyperpyrexia")
        void shouldDetectHyperpyrexia() {
            VitalSigns vitalSigns = createVitalSigns();
            vitalSigns.setTemperature(40.5);

            List<VitalSigns.VitalSignWarning> warnings = vitalSigns.checkWarnings();

            assertTrue(warnings.stream().anyMatch(w ->
                    w.parameter().equals("temperature") &&
                    w.severity().equals("CRITICAL") &&
                    w.message().contains("Hyperpyrexia")));
        }

        @Test
        @DisplayName("should detect high fever")
        void shouldDetectHighFever() {
            VitalSigns vitalSigns = createVitalSigns();
            vitalSigns.setTemperature(39.0);

            List<VitalSigns.VitalSignWarning> warnings = vitalSigns.checkWarnings();

            assertTrue(warnings.stream().anyMatch(w ->
                    w.parameter().equals("temperature") &&
                    w.severity().equals("HIGH") &&
                    w.message().contains("High fever")));
        }

        @Test
        @DisplayName("should detect hypothermia")
        void shouldDetectHypothermia() {
            VitalSigns vitalSigns = createVitalSigns();
            vitalSigns.setTemperature(34.5);

            List<VitalSigns.VitalSignWarning> warnings = vitalSigns.checkWarnings();

            assertTrue(warnings.stream().anyMatch(w ->
                    w.parameter().equals("temperature") &&
                    w.severity().equals("CRITICAL") &&
                    w.message().contains("Hypothermia")));
        }
    }

    @Nested
    @DisplayName("checkWarnings - Oxygen Saturation")
    class OxygenSaturationWarnings {

        @Test
        @DisplayName("should detect severe hypoxia")
        void shouldDetectSevereHypoxia() {
            VitalSigns vitalSigns = createVitalSigns();
            vitalSigns.setOxygenSaturation(85);

            List<VitalSigns.VitalSignWarning> warnings = vitalSigns.checkWarnings();

            assertTrue(warnings.stream().anyMatch(w ->
                    w.parameter().equals("oxygenSaturation") &&
                    w.severity().equals("CRITICAL") &&
                    w.message().contains("Severe hypoxia")));
        }

        @Test
        @DisplayName("should detect hypoxia")
        void shouldDetectHypoxia() {
            VitalSigns vitalSigns = createVitalSigns();
            vitalSigns.setOxygenSaturation(90);

            List<VitalSigns.VitalSignWarning> warnings = vitalSigns.checkWarnings();

            assertTrue(warnings.stream().anyMatch(w ->
                    w.parameter().equals("oxygenSaturation") &&
                    w.severity().equals("HIGH") &&
                    w.message().contains("Hypoxia")));
        }
    }

    @Nested
    @DisplayName("checkWarnings - Consciousness Level")
    class ConsciousnessLevelWarnings {

        @Test
        @DisplayName("should detect unresponsive patient")
        void shouldDetectUnresponsivePatient() {
            VitalSigns vitalSigns = createVitalSigns();
            vitalSigns.setConsciousnessLevel(ConsciousnessLevel.UNRESPONSIVE);

            List<VitalSigns.VitalSignWarning> warnings = vitalSigns.checkWarnings();

            assertTrue(warnings.stream().anyMatch(w ->
                    w.parameter().equals("consciousnessLevel") &&
                    w.severity().equals("CRITICAL")));
        }

        @Test
        @DisplayName("should detect pain response only")
        void shouldDetectPainResponseOnly() {
            VitalSigns vitalSigns = createVitalSigns();
            vitalSigns.setConsciousnessLevel(ConsciousnessLevel.PAIN);

            List<VitalSigns.VitalSignWarning> warnings = vitalSigns.checkWarnings();

            assertTrue(warnings.stream().anyMatch(w ->
                    w.parameter().equals("consciousnessLevel") &&
                    w.severity().equals("HIGH")));
        }

        @Test
        @DisplayName("should not warn for alert patient")
        void shouldNotWarnForAlertPatient() {
            VitalSigns vitalSigns = createVitalSigns();
            vitalSigns.setConsciousnessLevel(ConsciousnessLevel.ALERT);

            List<VitalSigns.VitalSignWarning> warnings = vitalSigns.checkWarnings();

            assertTrue(warnings.stream().noneMatch(w ->
                    w.parameter().equals("consciousnessLevel")));
        }
    }

    @Nested
    @DisplayName("checkWarnings - Glucose Level")
    class GlucoseLevelWarnings {

        @Test
        @DisplayName("should detect severe hypoglycemia")
        void shouldDetectSevereHypoglycemia() {
            VitalSigns vitalSigns = createVitalSigns();
            vitalSigns.setGlucoseLevel(2.5);

            List<VitalSigns.VitalSignWarning> warnings = vitalSigns.checkWarnings();

            assertTrue(warnings.stream().anyMatch(w ->
                    w.parameter().equals("glucoseLevel") &&
                    w.severity().equals("CRITICAL") &&
                    w.message().contains("Severe hypoglycemia")));
        }

        @Test
        @DisplayName("should detect hypoglycemia")
        void shouldDetectHypoglycemia() {
            VitalSigns vitalSigns = createVitalSigns();
            vitalSigns.setGlucoseLevel(3.5);

            List<VitalSigns.VitalSignWarning> warnings = vitalSigns.checkWarnings();

            assertTrue(warnings.stream().anyMatch(w ->
                    w.parameter().equals("glucoseLevel") &&
                    w.severity().equals("HIGH") &&
                    w.message().contains("Hypoglycemia")));
        }

        @Test
        @DisplayName("should detect severe hyperglycemia")
        void shouldDetectSevereHyperglycemia() {
            VitalSigns vitalSigns = createVitalSigns();
            vitalSigns.setGlucoseLevel(25.0);

            List<VitalSigns.VitalSignWarning> warnings = vitalSigns.checkWarnings();

            assertTrue(warnings.stream().anyMatch(w ->
                    w.parameter().equals("glucoseLevel") &&
                    w.severity().equals("CRITICAL") &&
                    w.message().contains("Severe hyperglycemia")));
        }
    }

    @Nested
    @DisplayName("checkWarnings - Combined")
    class CombinedWarnings {

        @Test
        @DisplayName("should return empty list for normal vital signs")
        void shouldReturnEmptyListForNormalVitalSigns() {
            VitalSigns vitalSigns = createVitalSigns();
            vitalSigns.setBloodPressureSystolic(120);
            vitalSigns.setBloodPressureDiastolic(80);
            vitalSigns.setHeartRate(70);
            vitalSigns.setRespiratoryRate(16);
            vitalSigns.setTemperature(37.0);
            vitalSigns.setOxygenSaturation(98);
            vitalSigns.setConsciousnessLevel(ConsciousnessLevel.ALERT);
            vitalSigns.setGlucoseLevel(5.5);

            List<VitalSigns.VitalSignWarning> warnings = vitalSigns.checkWarnings();

            assertTrue(warnings.isEmpty());
        }

        @Test
        @DisplayName("should return multiple warnings for critical patient")
        void shouldReturnMultipleWarningsForCriticalPatient() {
            VitalSigns vitalSigns = createVitalSigns();
            vitalSigns.setBloodPressureSystolic(80);
            vitalSigns.setHeartRate(160);
            vitalSigns.setOxygenSaturation(80);
            vitalSigns.setConsciousnessLevel(ConsciousnessLevel.PAIN);

            List<VitalSigns.VitalSignWarning> warnings = vitalSigns.checkWarnings();

            assertTrue(warnings.size() >= 4);
        }
    }

    private VitalSigns createVitalSigns() {
        return new VitalSigns(UUID.randomUUID());
    }
}
