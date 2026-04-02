package se.curanexus.triage.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import se.curanexus.triage.domain.*;
import se.curanexus.triage.repository.TriageProtocolRepository;
import se.curanexus.triage.service.DecisionSupportService.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("DecisionSupportService")
class DecisionSupportServiceTest {

    @Mock
    private TriageProtocolRepository protocolRepository;

    @InjectMocks
    private DecisionSupportService decisionSupportService;

    @Nested
    @DisplayName("Vital Signs Analysis")
    class VitalSignsAnalysis {

        @Test
        @DisplayName("should recommend IMMEDIATE priority for hypertensive crisis")
        void shouldRecommendImmediateForHypertensiveCrisis() {
            var request = new DecisionSupportRequest(
                    50, "male", List.of(),
                    new VitalSignsInput(185, 110, 80, 18, 37.0, 98, 3, ConsciousnessLevel.ALERT, 5.5),
                    List.of(), List.of()
            );

            DecisionSupportResult result = decisionSupportService.getRecommendation(request);

            assertEquals(TriagePriority.IMMEDIATE, result.recommendedPriority());
            assertTrue(result.redFlags().stream().anyMatch(f -> f.contains("Hypertensive crisis")));
            assertTrue(result.warnings().stream().anyMatch(w -> w.severity().equals("CRITICAL")));
        }

        @Test
        @DisplayName("should recommend IMMEDIATE priority for hypotension")
        void shouldRecommendImmediateForHypotension() {
            var request = new DecisionSupportRequest(
                    50, "male", List.of(),
                    new VitalSignsInput(85, 50, 100, 22, 36.5, 95, 5, ConsciousnessLevel.ALERT, 5.5),
                    List.of(), List.of()
            );

            DecisionSupportResult result = decisionSupportService.getRecommendation(request);

            assertEquals(TriagePriority.IMMEDIATE, result.recommendedPriority());
            assertTrue(result.redFlags().stream().anyMatch(f -> f.contains("Hypotension")));
        }

        @Test
        @DisplayName("should recommend IMMEDIATE priority for severe hypoxia")
        void shouldRecommendImmediateForSevereHypoxia() {
            var request = new DecisionSupportRequest(
                    50, "male", List.of(),
                    new VitalSignsInput(120, 80, 90, 28, 37.5, 85, 6, ConsciousnessLevel.ALERT, 5.5),
                    List.of(), List.of()
            );

            DecisionSupportResult result = decisionSupportService.getRecommendation(request);

            assertEquals(TriagePriority.IMMEDIATE, result.recommendedPriority());
            assertTrue(result.redFlags().stream().anyMatch(f -> f.contains("hypoxia")));
        }

        @Test
        @DisplayName("should recommend IMMEDIATE priority for unresponsive patient")
        void shouldRecommendImmediateForUnresponsivePatient() {
            var request = new DecisionSupportRequest(
                    50, "male", List.of(),
                    new VitalSignsInput(120, 80, 70, 16, 37.0, 97, 0, ConsciousnessLevel.UNRESPONSIVE, 5.5),
                    List.of(), List.of()
            );

            DecisionSupportResult result = decisionSupportService.getRecommendation(request);

            assertEquals(TriagePriority.IMMEDIATE, result.recommendedPriority());
            assertTrue(result.redFlags().stream().anyMatch(f -> f.contains("Unresponsive")));
        }

        @Test
        @DisplayName("should recommend EMERGENT priority for severe tachycardia")
        void shouldRecommendEmergentForSevereTachycardia() {
            var request = new DecisionSupportRequest(
                    50, "male", List.of(),
                    new VitalSignsInput(130, 85, 155, 20, 37.0, 96, 4, ConsciousnessLevel.ALERT, 5.5),
                    List.of(), List.of()
            );

            DecisionSupportResult result = decisionSupportService.getRecommendation(request);

            assertEquals(TriagePriority.EMERGENT, result.recommendedPriority());
        }

        @Test
        @DisplayName("should recommend EMERGENT priority for hyperpyrexia")
        void shouldRecommendEmergentForHyperpyrexia() {
            var request = new DecisionSupportRequest(
                    50, "male", List.of(),
                    new VitalSignsInput(130, 85, 110, 24, 40.5, 96, 4, ConsciousnessLevel.ALERT, 5.5),
                    List.of(), List.of()
            );

            DecisionSupportResult result = decisionSupportService.getRecommendation(request);

            assertEquals(TriagePriority.EMERGENT, result.recommendedPriority());
            assertTrue(result.redFlags().stream().anyMatch(f -> f.contains("Hyperpyrexia")));
        }

        @Test
        @DisplayName("should recommend NON_URGENT for normal vital signs")
        void shouldRecommendNonUrgentForNormalVitalSigns() {
            var request = new DecisionSupportRequest(
                    30, "female", List.of(),
                    new VitalSignsInput(120, 80, 72, 16, 37.0, 98, 2, ConsciousnessLevel.ALERT, 5.5),
                    List.of(), List.of()
            );

            DecisionSupportResult result = decisionSupportService.getRecommendation(request);

            assertEquals(TriagePriority.NON_URGENT, result.recommendedPriority());
            assertTrue(result.warnings().isEmpty());
            assertTrue(result.redFlags().isEmpty());
        }
    }

    @Nested
    @DisplayName("Symptom Analysis")
    class SymptomAnalysis {

        @Test
        @DisplayName("should recommend IMMEDIATE priority for severe chest pain")
        void shouldRecommendImmediateForSevereChestPain() {
            var request = new DecisionSupportRequest(
                    55, "male",
                    List.of(new SymptomInput("CHEST_PAIN", "Crushing chest pain", Severity.SEVERE)),
                    null, List.of(), List.of()
            );

            DecisionSupportResult result = decisionSupportService.getRecommendation(request);

            assertEquals(TriagePriority.IMMEDIATE, result.recommendedPriority());
            assertTrue(result.redFlags().stream().anyMatch(f -> f.contains("chest pain")));
        }

        @Test
        @DisplayName("should recommend EMERGENT priority for moderate chest pain")
        void shouldRecommendEmergentForModerateChestPain() {
            var request = new DecisionSupportRequest(
                    55, "male",
                    List.of(new SymptomInput("CHEST_PAIN", "Mild chest discomfort", Severity.MODERATE)),
                    null, List.of(), List.of()
            );

            DecisionSupportResult result = decisionSupportService.getRecommendation(request);

            assertEquals(TriagePriority.EMERGENT, result.recommendedPriority());
        }

        @Test
        @DisplayName("should recommend IMMEDIATE priority for stroke symptoms")
        void shouldRecommendImmediateForStrokeSymptoms() {
            var request = new DecisionSupportRequest(
                    70, "female",
                    List.of(new SymptomInput("STROKE_SYMPTOMS", "Sudden weakness on left side", Severity.SEVERE)),
                    null, List.of(), List.of()
            );

            DecisionSupportResult result = decisionSupportService.getRecommendation(request);

            assertEquals(TriagePriority.IMMEDIATE, result.recommendedPriority());
            assertTrue(result.redFlags().stream().anyMatch(f -> f.toLowerCase().contains("stroke")));
        }

        @Test
        @DisplayName("should recommend IMMEDIATE priority for severe bleeding")
        void shouldRecommendImmediateForSevereBleed() {
            var request = new DecisionSupportRequest(
                    40, "male",
                    List.of(new SymptomInput("GI_BLEED", "Vomiting blood", Severity.SEVERE)),
                    null, List.of(), List.of()
            );

            DecisionSupportResult result = decisionSupportService.getRecommendation(request);

            assertEquals(TriagePriority.IMMEDIATE, result.recommendedPriority());
            assertTrue(result.redFlags().stream().anyMatch(f -> f.toLowerCase().contains("hemorrhage")));
        }

        @Test
        @DisplayName("should recommend EMERGENT priority for severe respiratory distress")
        void shouldRecommendEmergentForSevereRespiratoryDistress() {
            var request = new DecisionSupportRequest(
                    60, "male",
                    List.of(new SymptomInput("DYSPNEA", "Severe shortness of breath", Severity.SEVERE)),
                    null, List.of(), List.of()
            );

            DecisionSupportResult result = decisionSupportService.getRecommendation(request);

            assertEquals(TriagePriority.EMERGENT, result.recommendedPriority());
            assertTrue(result.redFlags().stream().anyMatch(f -> f.toLowerCase().contains("respiratory")));
        }
    }

    @Nested
    @DisplayName("Combined Analysis")
    class CombinedAnalysis {

        @Test
        @DisplayName("should take highest priority from vital signs and symptoms")
        void shouldTakeHighestPriorityFromVitalSignsAndSymptoms() {
            var request = new DecisionSupportRequest(
                    60, "male",
                    List.of(new SymptomInput("HEADACHE", "Mild headache", Severity.MILD)),
                    new VitalSignsInput(85, 50, 110, 22, 36.0, 93, 3, ConsciousnessLevel.VERBAL, 5.5),
                    List.of(), List.of()
            );

            DecisionSupportResult result = decisionSupportService.getRecommendation(request);

            assertEquals(TriagePriority.IMMEDIATE, result.recommendedPriority());
        }

        @Test
        @DisplayName("should generate appropriate recommended actions for IMMEDIATE priority")
        void shouldGenerateAppropriateActionsForImmediatePriority() {
            var request = new DecisionSupportRequest(
                    50, "male", List.of(),
                    new VitalSignsInput(80, 50, 120, 28, 36.5, 88, 8, ConsciousnessLevel.PAIN, 2.5),
                    List.of(), List.of()
            );

            DecisionSupportResult result = decisionSupportService.getRecommendation(request);

            assertTrue(result.recommendedActions().stream()
                    .anyMatch(a -> a.toLowerCase().contains("immediate")));
            assertTrue(result.recommendedActions().stream()
                    .anyMatch(a -> a.toLowerCase().contains("resuscitation") || a.toLowerCase().contains("iv")));
        }

        @Test
        @DisplayName("should determine appropriate care level based on priority")
        void shouldDetermineAppropriateCareLevel() {
            // IMMEDIATE -> ICU
            var immediateRequest = new DecisionSupportRequest(
                    50, "male", List.of(),
                    new VitalSignsInput(80, 50, 120, 28, 36.5, 85, 8, ConsciousnessLevel.UNRESPONSIVE, 5.5),
                    List.of(), List.of()
            );
            assertEquals(CareLevel.INTENSIVE_CARE, decisionSupportService.getRecommendation(immediateRequest).recommendedCareLevel());

            // NON_URGENT -> Primary care
            var nonUrgentRequest = new DecisionSupportRequest(
                    30, "female", List.of(),
                    new VitalSignsInput(120, 80, 70, 16, 37.0, 98, 1, ConsciousnessLevel.ALERT, 5.5),
                    List.of(), List.of()
            );
            assertEquals(CareLevel.PRIMARY_CARE, decisionSupportService.getRecommendation(nonUrgentRequest).recommendedCareLevel());
        }
    }

    @Nested
    @DisplayName("Confidence Calculation")
    class ConfidenceCalculation {

        @Test
        @DisplayName("should have base confidence for simple cases")
        void shouldHaveBaseConfidenceForSimpleCases() {
            var request = new DecisionSupportRequest(
                    30, "female", List.of(), null, List.of(), List.of()
            );

            DecisionSupportResult result = decisionSupportService.getRecommendation(request);

            assertTrue(result.confidence() >= 0.5 && result.confidence() <= 1.0);
        }

        @Test
        @DisplayName("should have confidence within valid range when red flags present")
        void shouldHaveConfidenceWithinValidRangeWhenRedFlagsPresent() {
            var request = new DecisionSupportRequest(
                    50, "male",
                    List.of(new SymptomInput("CHEST_PAIN", "Severe chest pain", Severity.SEVERE)),
                    new VitalSignsInput(80, 50, 120, 28, 36.5, 88, 8, ConsciousnessLevel.PAIN, 2.5),
                    List.of(), List.of()
            );

            DecisionSupportResult result = decisionSupportService.getRecommendation(request);

            assertFalse(result.redFlags().isEmpty());
            assertTrue(result.confidence() >= 0.5 && result.confidence() <= 1.0);
        }
    }
}
