package se.curanexus.triage.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import se.curanexus.triage.api.dto.DecisionSupportRequest;
import se.curanexus.triage.domain.ConsciousnessLevel;
import se.curanexus.triage.domain.Severity;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Decision Support Integration Tests")
class DecisionSupportIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("Clinical Scenarios")
    class ClinicalScenarios {

        @Test
        @DisplayName("should identify STEMI scenario as IMMEDIATE")
        void shouldIdentifySTEMIAsImmediate() throws Exception {
            var request = new DecisionSupportRequest(
                    62, "male",
                    List.of(
                            new DecisionSupportRequest.SymptomInput(
                                    "CHEST_PAIN", "Crushing chest pain radiating to left arm", Severity.SEVERE
                            ),
                            new DecisionSupportRequest.SymptomInput(
                                    "DIAPHORESIS", "Profuse sweating", Severity.MODERATE
                            )
                    ),
                    new DecisionSupportRequest.VitalSignsInput(
                            90, 60, 110, 24, 36.8, 93, 9, ConsciousnessLevel.ALERT, 7.2
                    ),
                    List.of("Hypertension", "Diabetes Type 2"),
                    List.of("Metformin", "Lisinopril")
            );

            mockMvc.perform(post("/api/v1/triage/decision-support")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.recommendedPriority").value("IMMEDIATE"))
                    .andExpect(jsonPath("$.recommendedCareLevel").value("INTENSIVE_CARE"))
                    .andExpect(jsonPath("$.redFlags").isArray())
                    .andExpect(jsonPath("$.redFlags.length()").value(org.hamcrest.Matchers.greaterThan(0)))
                    .andExpect(jsonPath("$.recommendedActions").isArray());
        }

        @Test
        @DisplayName("should identify stroke scenario as IMMEDIATE")
        void shouldIdentifyStrokeAsImmediate() throws Exception {
            var request = new DecisionSupportRequest(
                    75, "female",
                    List.of(
                            new DecisionSupportRequest.SymptomInput(
                                    "STROKE_WEAKNESS", "Sudden weakness in right arm and leg", Severity.SEVERE
                            ),
                            new DecisionSupportRequest.SymptomInput(
                                    "SPEECH_DIFFICULTY", "Slurred speech", Severity.MODERATE
                            )
                    ),
                    new DecisionSupportRequest.VitalSignsInput(
                            180, 100, 88, 18, 37.0, 96, 2, ConsciousnessLevel.VERBAL, 5.5
                    ),
                    List.of("Atrial fibrillation"),
                    List.of("Warfarin")
            );

            mockMvc.perform(post("/api/v1/triage/decision-support")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.recommendedPriority").value("IMMEDIATE"))
                    .andExpect(jsonPath("$.redFlags[?(@=~ /.*stroke.*/i)]").exists())
                    .andExpect(jsonPath("$.warnings[?(@.type == 'TIME_SENSITIVE')]").exists());
        }

        @Test
        @DisplayName("should identify respiratory failure as IMMEDIATE")
        void shouldIdentifyRespiratoryFailureAsImmediate() throws Exception {
            var request = new DecisionSupportRequest(
                    55, "male",
                    List.of(
                            new DecisionSupportRequest.SymptomInput(
                                    "DYSPNEA", "Severe shortness of breath", Severity.CRITICAL
                            )
                    ),
                    new DecisionSupportRequest.VitalSignsInput(
                            140, 90, 130, 32, 38.5, 82, 7, ConsciousnessLevel.VERBAL, 5.5
                    ),
                    List.of("COPD"),
                    List.of()
            );

            mockMvc.perform(post("/api/v1/triage/decision-support")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.recommendedPriority").value("IMMEDIATE"))
                    .andExpect(jsonPath("$.warnings[?(@.severity == 'CRITICAL')]").exists());
        }

        @Test
        @DisplayName("should identify minor injury as NON_URGENT")
        void shouldIdentifyMinorInjuryAsNonUrgent() throws Exception {
            var request = new DecisionSupportRequest(
                    25, "female",
                    List.of(
                            new DecisionSupportRequest.SymptomInput(
                                    "ANKLE_PAIN", "Twisted ankle while jogging", Severity.MILD
                            )
                    ),
                    new DecisionSupportRequest.VitalSignsInput(
                            118, 72, 78, 16, 36.6, 99, 3, ConsciousnessLevel.ALERT, 5.0
                    ),
                    List.of(),
                    List.of()
            );

            mockMvc.perform(post("/api/v1/triage/decision-support")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.recommendedPriority").value("NON_URGENT"))
                    .andExpect(jsonPath("$.recommendedCareLevel").value("PRIMARY_CARE"))
                    .andExpect(jsonPath("$.warnings").isEmpty())
                    .andExpect(jsonPath("$.redFlags").isEmpty());
        }

        @Test
        @DisplayName("should handle pediatric patient with high fever (hyperpyrexia)")
        void shouldHandlePediatricPatientWithFever() throws Exception {
            var request = new DecisionSupportRequest(
                    3, "male",
                    List.of(
                            new DecisionSupportRequest.SymptomInput(
                                    "FEVER", "High fever for 2 days", Severity.MODERATE
                            ),
                            new DecisionSupportRequest.SymptomInput(
                                    "LETHARGY", "Unusually tired", Severity.MODERATE
                            )
                    ),
                    new DecisionSupportRequest.VitalSignsInput(
                            null, null, 140, 30, 40.5, 96, null, ConsciousnessLevel.ALERT, null
                    ),
                    List.of(),
                    List.of()
            );

            mockMvc.perform(post("/api/v1/triage/decision-support")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.recommendedPriority").exists())
                    .andExpect(jsonPath("$.warnings[?(@.message =~ /.*Hyperpyrexia.*/)]").exists());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("should return validation error for empty symptoms")
        void shouldReturnValidationErrorForEmptySymptoms() throws Exception {
            var request = new DecisionSupportRequest(
                    40, "female", List.of(), null, List.of(), List.of()
            );

            mockMvc.perform(post("/api/v1/triage/decision-support")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should handle request with minimal symptoms and critical vitals")
        void shouldHandleMinimalSymptomsWithCriticalVitals() throws Exception {
            var request = new DecisionSupportRequest(
                    50, "male",
                    List.of(new DecisionSupportRequest.SymptomInput("GENERAL", "General malaise", Severity.MILD)),
                    new DecisionSupportRequest.VitalSignsInput(
                            85, 55, 120, 26, 35.5, 89, 6, ConsciousnessLevel.PAIN, 2.8
                    ),
                    List.of(), List.of()
            );

            mockMvc.perform(post("/api/v1/triage/decision-support")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.recommendedPriority").value("IMMEDIATE"))
                    .andExpect(jsonPath("$.redFlags.length()").value(org.hamcrest.Matchers.greaterThan(0)));
        }
    }
}
