package se.curanexus.triage.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import se.curanexus.triage.api.dto.*;
import se.curanexus.triage.domain.*;
import se.curanexus.triage.service.TriageService;
import se.curanexus.triage.service.exception.AssessmentNotFoundException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TriageAssessmentController.class)
@DisplayName("TriageAssessmentController")
class TriageAssessmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TriageService triageService;

    private UUID patientId;
    private UUID encounterId;
    private UUID nurseId;
    private UUID assessmentId;

    @BeforeEach
    void setUp() {
        patientId = UUID.randomUUID();
        encounterId = UUID.randomUUID();
        nurseId = UUID.randomUUID();
        assessmentId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("POST /api/v1/triage/assessments")
    class CreateAssessment {

        @Test
        @DisplayName("should create assessment and return 201")
        void shouldCreateAssessmentAndReturn201() throws Exception {
            CreateAssessmentRequest request = new CreateAssessmentRequest(
                    patientId, encounterId, nurseId, "Chest pain", ArrivalMode.AMBULANCE, UUID.randomUUID()
            );

            TriageAssessment assessment = new TriageAssessment(patientId, encounterId, nurseId, "Chest pain");
            when(triageService.createAssessment(any(), any(), any(), any(), any(), any()))
                    .thenReturn(assessment);

            mockMvc.perform(post("/api/v1/triage/assessments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.patientId").value(patientId.toString()))
                    .andExpect(jsonPath("$.encounterId").value(encounterId.toString()))
                    .andExpect(jsonPath("$.chiefComplaint").value("Chest pain"))
                    .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
        }

        @Test
        @DisplayName("should return 400 when patientId is missing")
        void shouldReturn400WhenPatientIdIsMissing() throws Exception {
            String requestBody = """
                {
                    "encounterId": "%s",
                    "triageNurseId": "%s",
                    "chiefComplaint": "Chest pain"
                }
                """.formatted(encounterId, nurseId);

            mockMvc.perform(post("/api/v1/triage/assessments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/triage/assessments/{assessmentId}")
    class GetAssessment {

        @Test
        @DisplayName("should return assessment when found")
        void shouldReturnAssessmentWhenFound() throws Exception {
            TriageAssessment assessment = new TriageAssessment(patientId, encounterId, nurseId, "Chest pain");
            when(triageService.getAssessment(assessmentId)).thenReturn(Optional.of(assessment));

            mockMvc.perform(get("/api/v1/triage/assessments/{assessmentId}", assessmentId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.patientId").value(patientId.toString()))
                    .andExpect(jsonPath("$.chiefComplaint").value("Chest pain"));
        }

        @Test
        @DisplayName("should return 404 when not found")
        void shouldReturn404WhenNotFound() throws Exception {
            when(triageService.getAssessment(assessmentId)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/v1/triage/assessments/{assessmentId}", assessmentId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/triage/assessments/{assessmentId}")
    class UpdateAssessment {

        @Test
        @DisplayName("should update assessment")
        void shouldUpdateAssessment() throws Exception {
            UpdateAssessmentRequest request = new UpdateAssessmentRequest(
                    "Updated complaint", "Some notes", TriagePriority.URGENT, CareLevel.EMERGENCY_CARE
            );

            TriageAssessment assessment = new TriageAssessment(patientId, encounterId, nurseId, "Updated complaint");
            assessment.setNotes("Some notes");
            assessment.setPriority(TriagePriority.URGENT);
            when(triageService.updateAssessment(eq(assessmentId), any(), any(), any(), any()))
                    .thenReturn(assessment);

            mockMvc.perform(put("/api/v1/triage/assessments/{assessmentId}", assessmentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.chiefComplaint").value("Updated complaint"))
                    .andExpect(jsonPath("$.notes").value("Some notes"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/triage/assessments/{assessmentId}/complete")
    class CompleteAssessment {

        @Test
        @DisplayName("should complete assessment")
        void shouldCompleteAssessment() throws Exception {
            CompleteAssessmentRequest request = new CompleteAssessmentRequest(
                    TriagePriority.URGENT, CareLevel.EMERGENCY_CARE, Disposition.ADMIT, "Final notes", null
            );

            TriageAssessment assessment = new TriageAssessment(patientId, encounterId, nurseId, "Chest pain");
            assessment.complete(TriagePriority.URGENT, CareLevel.EMERGENCY_CARE, Disposition.ADMIT);
            when(triageService.completeAssessment(eq(assessmentId), any(), any(), any(), any(), any()))
                    .thenReturn(assessment);

            mockMvc.perform(post("/api/v1/triage/assessments/{assessmentId}/complete", assessmentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("COMPLETED"))
                    .andExpect(jsonPath("$.priority").value("URGENT"))
                    .andExpect(jsonPath("$.disposition").value("ADMIT"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/triage/assessments/{assessmentId}/escalate")
    class EscalatePriority {

        @Test
        @DisplayName("should escalate priority")
        void shouldEscalatePriority() throws Exception {
            UUID escalatedBy = UUID.randomUUID();
            EscalationRequest request = new EscalationRequest(
                    TriagePriority.EMERGENT, "Worsening symptoms", escalatedBy
            );

            TriageAssessment assessment = new TriageAssessment(patientId, encounterId, nurseId, "Chest pain");
            assessment.escalate(TriagePriority.EMERGENT, "Worsening symptoms", escalatedBy);
            when(triageService.escalatePriority(eq(assessmentId), any(), any(), any()))
                    .thenReturn(assessment);

            mockMvc.perform(post("/api/v1/triage/assessments/{assessmentId}/escalate", assessmentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.priority").value("EMERGENT"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/triage/assessments/{assessmentId}/symptoms")
    class AddSymptom {

        @Test
        @DisplayName("should add symptom")
        void shouldAddSymptom() throws Exception {
            SymptomRequest request = new SymptomRequest(
                    "CHEST_PAIN", "Sharp pain in left chest",
                    Instant.now().minusSeconds(3600), "1 hour",
                    Severity.SEVERE, "Left chest", true
            );

            Symptom symptom = new Symptom("CHEST_PAIN", "Sharp pain in left chest");
            symptom.setSeverity(Severity.SEVERE);
            symptom.setChiefComplaint(true);
            when(triageService.addSymptom(eq(assessmentId), anyString(), anyString(), any(), anyString(), any(), anyString(), anyBoolean()))
                    .thenReturn(symptom);

            mockMvc.perform(post("/api/v1/triage/assessments/{assessmentId}/symptoms", assessmentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.symptomCode").value("CHEST_PAIN"))
                    .andExpect(jsonPath("$.severity").value("SEVERE"))
                    .andExpect(jsonPath("$.isChiefComplaint").value(true));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/triage/assessments/{assessmentId}/symptoms")
    class GetSymptoms {

        @Test
        @DisplayName("should return symptoms list")
        void shouldReturnSymptomsList() throws Exception {
            Symptom symptom1 = new Symptom("CHEST_PAIN", "Chest pain");
            Symptom symptom2 = new Symptom("DYSPNEA", "Shortness of breath");
            when(triageService.getSymptoms(assessmentId)).thenReturn(List.of(symptom1, symptom2));

            mockMvc.perform(get("/api/v1/triage/assessments/{assessmentId}/symptoms", assessmentId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].symptomCode").value("CHEST_PAIN"))
                    .andExpect(jsonPath("$[1].symptomCode").value("DYSPNEA"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/triage/assessments/{assessmentId}/vital-signs")
    class RecordVitalSigns {

        @Test
        @DisplayName("should record vital signs")
        void shouldRecordVitalSigns() throws Exception {
            UUID recordedBy = UUID.randomUUID();
            VitalSignsRequest request = new VitalSignsRequest(
                    120, 80, 75, 16, 37.0, 98, 3, ConsciousnessLevel.ALERT, 5.5, recordedBy
            );

            VitalSigns vitalSigns = new VitalSigns(recordedBy);
            vitalSigns.setBloodPressureSystolic(120);
            vitalSigns.setBloodPressureDiastolic(80);
            vitalSigns.setHeartRate(75);
            when(triageService.recordVitalSigns(eq(assessmentId), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(vitalSigns);

            mockMvc.perform(post("/api/v1/triage/assessments/{assessmentId}/vital-signs", assessmentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.bloodPressureSystolic").value(120))
                    .andExpect(jsonPath("$.bloodPressureDiastolic").value(80))
                    .andExpect(jsonPath("$.heartRate").value(75));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/triage/assessments/{assessmentId}/vital-signs")
    class GetVitalSigns {

        @Test
        @DisplayName("should return vital signs when found")
        void shouldReturnVitalSignsWhenFound() throws Exception {
            VitalSigns vitalSigns = new VitalSigns(UUID.randomUUID());
            vitalSigns.setBloodPressureSystolic(120);
            vitalSigns.setHeartRate(75);
            when(triageService.getVitalSigns(assessmentId)).thenReturn(Optional.of(vitalSigns));

            mockMvc.perform(get("/api/v1/triage/assessments/{assessmentId}/vital-signs", assessmentId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.bloodPressureSystolic").value(120))
                    .andExpect(jsonPath("$.heartRate").value(75));
        }

        @Test
        @DisplayName("should return 404 when vital signs not found")
        void shouldReturn404WhenNotFound() throws Exception {
            when(triageService.getVitalSigns(assessmentId)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/v1/triage/assessments/{assessmentId}/vital-signs", assessmentId))
                    .andExpect(status().isNotFound());
        }
    }
}
