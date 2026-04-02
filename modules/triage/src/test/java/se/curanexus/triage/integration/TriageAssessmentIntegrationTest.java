package se.curanexus.triage.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MvcResult;
import se.curanexus.triage.api.dto.*;
import se.curanexus.triage.domain.*;
import se.curanexus.triage.repository.TriageAssessmentRepository;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Triage Assessment Integration Tests")
class TriageAssessmentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TriageAssessmentRepository assessmentRepository;

    private UUID patientId;
    private UUID encounterId;
    private UUID nurseId;
    private UUID locationId;

    @BeforeEach
    void setUp() {
        patientId = UUID.randomUUID();
        encounterId = UUID.randomUUID();
        nurseId = UUID.randomUUID();
        locationId = UUID.randomUUID();
    }


    @Nested
    @DisplayName("Full Triage Workflow")
    class FullTriageWorkflow {

        @Test
        @DisplayName("should complete full triage assessment workflow")
        void shouldCompleteFullTriageWorkflow() throws Exception {
            // 1. Create assessment
            CreateAssessmentRequest createRequest = new CreateAssessmentRequest(
                    patientId, encounterId, nurseId, "Chest pain radiating to left arm",
                    ArrivalMode.AMBULANCE, locationId
            );

            MvcResult createResult = mockMvc.perform(post("/api/v1/triage/assessments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.patientId").value(patientId.toString()))
                    .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                    .andReturn();

            TriageAssessmentResponse createResponse = objectMapper.readValue(
                    createResult.getResponse().getContentAsString(),
                    TriageAssessmentResponse.class
            );
            UUID assessmentId = createResponse.id();

            // 2. Add symptoms
            SymptomRequest symptomRequest = new SymptomRequest(
                    "CHEST_PAIN", "Sharp pain in left chest radiating to arm",
                    null, "30 minutes", Severity.SEVERE, "Left chest", true
            );

            mockMvc.perform(post("/api/v1/triage/assessments/{id}/symptoms", assessmentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(symptomRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.symptomCode").value("CHEST_PAIN"))
                    .andExpect(jsonPath("$.severity").value("SEVERE"));

            // 3. Record vital signs
            VitalSignsRequest vitalsRequest = new VitalSignsRequest(
                    160, 95, 110, 22, 37.2, 94, 8, ConsciousnessLevel.ALERT, 6.5, nurseId
            );

            mockMvc.perform(post("/api/v1/triage/assessments/{id}/vital-signs", assessmentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(vitalsRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.bloodPressureSystolic").value(160))
                    .andExpect(jsonPath("$.heartRate").value(110));

            // 4. Get assessment with all data
            mockMvc.perform(get("/api/v1/triage/assessments/{id}", assessmentId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

            // 5. Complete assessment
            CompleteAssessmentRequest completeRequest = new CompleteAssessmentRequest(
                    TriagePriority.EMERGENT, CareLevel.EMERGENCY_CARE, Disposition.ADMIT,
                    "Suspected acute coronary syndrome", null
            );

            mockMvc.perform(post("/api/v1/triage/assessments/{id}/complete", assessmentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(completeRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("COMPLETED"))
                    .andExpect(jsonPath("$.priority").value("EMERGENT"))
                    .andExpect(jsonPath("$.careLevel").value("EMERGENCY_CARE"))
                    .andExpect(jsonPath("$.disposition").value("ADMIT"));

            // Verify in database
            var savedAssessment = assessmentRepository.findById(assessmentId);
            assertTrue(savedAssessment.isPresent());
            assertEquals(AssessmentStatus.COMPLETED, savedAssessment.get().getStatus());
            assertEquals(TriagePriority.EMERGENT, savedAssessment.get().getPriority());
        }
    }

    @Nested
    @DisplayName("Escalation Workflow")
    class EscalationWorkflow {

        @Test
        @DisplayName("should escalate priority when patient condition worsens")
        void shouldEscalatePriorityWhenConditionWorsens() throws Exception {
            // Create assessment
            CreateAssessmentRequest createRequest = new CreateAssessmentRequest(
                    patientId, encounterId, nurseId, "Abdominal pain",
                    ArrivalMode.WALK_IN, locationId
            );

            MvcResult createResult = mockMvc.perform(post("/api/v1/triage/assessments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andReturn();

            TriageAssessmentResponse createResponse = objectMapper.readValue(
                    createResult.getResponse().getContentAsString(),
                    TriageAssessmentResponse.class
            );
            UUID assessmentId = createResponse.id();

            // Update with initial priority
            UpdateAssessmentRequest updateRequest = new UpdateAssessmentRequest(
                    null, null, TriagePriority.LESS_URGENT, null
            );

            mockMvc.perform(put("/api/v1/triage/assessments/{id}", assessmentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.priority").value("LESS_URGENT"));

            // Escalate due to worsening condition
            UUID seniorNurseId = UUID.randomUUID();
            EscalationRequest escalateRequest = new EscalationRequest(
                    TriagePriority.URGENT, "Patient developed rigidity and guarding", seniorNurseId
            );

            mockMvc.perform(post("/api/v1/triage/assessments/{id}/escalate", assessmentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(escalateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.priority").value("URGENT"));

            // Verify escalation history
            var savedAssessment = assessmentRepository.findById(assessmentId);
            assertTrue(savedAssessment.isPresent());
            assertEquals(1, savedAssessment.get().getEscalationHistory().size());
        }
    }

    @Nested
    @DisplayName("Search and Queue")
    class SearchAndQueue {

        @Test
        @DisplayName("should search assessments by priority")
        void shouldSearchAssessmentsByPriority() throws Exception {
            // Create multiple assessments
            for (int i = 0; i < 3; i++) {
                CreateAssessmentRequest request = new CreateAssessmentRequest(
                        UUID.randomUUID(), UUID.randomUUID(), nurseId,
                        "Complaint " + i, ArrivalMode.WALK_IN, locationId
                );

                mockMvc.perform(post("/api/v1/triage/assessments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isCreated());
            }

            // Search all
            mockMvc.perform(get("/api/v1/triage/assessments"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(3));
        }

        @Test
        @DisplayName("should get triage queue")
        void shouldGetTriageQueue() throws Exception {
            // Create assessment
            CreateAssessmentRequest request = new CreateAssessmentRequest(
                    patientId, encounterId, nurseId, "Minor injury",
                    ArrivalMode.WALK_IN, locationId
            );

            mockMvc.perform(post("/api/v1/triage/assessments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            // Get queue
            mockMvc.perform(get("/api/v1/triage/queue")
                            .param("locationId", locationId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalWaiting").isNumber());
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {

        @Test
        @DisplayName("should return 409 when creating duplicate assessment for encounter")
        void shouldReturn409WhenCreatingDuplicateAssessment() throws Exception {
            CreateAssessmentRequest request = new CreateAssessmentRequest(
                    patientId, encounterId, nurseId, "Chest pain",
                    ArrivalMode.AMBULANCE, locationId
            );

            // First creation succeeds
            mockMvc.perform(post("/api/v1/triage/assessments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            // Second creation fails
            mockMvc.perform(post("/api/v1/triage/assessments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error").value("Conflict"));
        }

        @Test
        @DisplayName("should return 404 for non-existent assessment")
        void shouldReturn404ForNonExistentAssessment() throws Exception {
            UUID nonExistentId = UUID.randomUUID();

            mockMvc.perform(get("/api/v1/triage/assessments/{id}", nonExistentId))
                    .andExpect(status().isNotFound());
        }
    }
}
