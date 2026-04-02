package se.curanexus.encounter.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import se.curanexus.encounter.api.dto.*;
import se.curanexus.encounter.domain.*;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class EncounterControllerIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("curanexus_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createEncounter_shouldReturn201() throws Exception {
        UUID patientId = UUID.randomUUID();
        CreateEncounterRequest request = new CreateEncounterRequest(
                patientId,
                EncounterClass.OUTPATIENT,
                EncounterType.INITIAL,
                EncounterPriority.NORMAL,
                "Cardiology",
                null,
                null,
                null,
                null
        );

        mockMvc.perform(post("/api/v1/encounters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.patientId").value(patientId.toString()))
                .andExpect(jsonPath("$.status").value("PLANNED"))
                .andExpect(jsonPath("$.encounterClass").value("OUTPATIENT"));
    }

    @Test
    void getEncounter_shouldReturn404ForNonExistent() throws Exception {
        mockMvc.perform(get("/api/v1/encounters/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound());
    }

    @Test
    void fullEncounterWorkflow_shouldWork() throws Exception {
        UUID patientId = UUID.randomUUID();

        // Create encounter
        CreateEncounterRequest createRequest = new CreateEncounterRequest(
                patientId,
                EncounterClass.OUTPATIENT,
                EncounterType.INITIAL,
                EncounterPriority.NORMAL,
                "General Practice",
                null,
                null,
                null,
                null
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/encounters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        EncounterDto createdEncounter = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                EncounterDto.class
        );
        String encounterId = createdEncounter.id().toString();

        // Get encounter
        mockMvc.perform(get("/api/v1/encounters/" + encounterId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PLANNED"));

        // Update status to ARRIVED
        UpdateStatusRequest arrivedRequest = new UpdateStatusRequest(EncounterStatus.ARRIVED, null);
        mockMvc.perform(patch("/api/v1/encounters/" + encounterId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(arrivedRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ARRIVED"));

        // Add participant
        AddParticipantRequest participantRequest = new AddParticipantRequest(
                ParticipantType.PRACTITIONER,
                UUID.randomUUID(),
                ParticipantRole.PRIMARY,
                null,
                null
        );

        mockMvc.perform(post("/api/v1/encounters/" + encounterId + "/participants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(participantRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("PRACTITIONER"))
                .andExpect(jsonPath("$.role").value("PRIMARY"));

        // Get participants
        mockMvc.perform(get("/api/v1/encounters/" + encounterId + "/participants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("PRACTITIONER"));

        // Add reason
        AddReasonRequest reasonRequest = new AddReasonRequest(
                ReasonType.CHIEF_COMPLAINT,
                "J06.9",
                "ICD-10-SE",
                "Upper respiratory infection",
                true
        );

        mockMvc.perform(post("/api/v1/encounters/" + encounterId + "/reasons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reasonRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("J06.9"));

        // Update status to IN_PROGRESS
        UpdateStatusRequest inProgressRequest = new UpdateStatusRequest(EncounterStatus.IN_PROGRESS, null);
        mockMvc.perform(patch("/api/v1/encounters/" + encounterId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inProgressRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.actualStartTime").isNotEmpty());

        // Update status to FINISHED
        UpdateStatusRequest finishedRequest = new UpdateStatusRequest(EncounterStatus.FINISHED, null);
        mockMvc.perform(patch("/api/v1/encounters/" + encounterId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(finishedRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FINISHED"))
                .andExpect(jsonPath("$.actualEndTime").isNotEmpty());

        // Search patient encounters
        mockMvc.perform(get("/api/v1/patients/" + patientId + "/encounters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("FINISHED"));
    }

    @Test
    void updateStatus_shouldReturn400ForInvalidTransition() throws Exception {
        UUID patientId = UUID.randomUUID();

        // Create encounter
        CreateEncounterRequest createRequest = new CreateEncounterRequest(
                patientId,
                EncounterClass.OUTPATIENT,
                null, null, null, null, null, null, null
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/encounters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        EncounterDto createdEncounter = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                EncounterDto.class
        );

        // Try invalid transition: PLANNED -> FINISHED
        UpdateStatusRequest invalidRequest = new UpdateStatusRequest(EncounterStatus.FINISHED, null);
        mockMvc.perform(patch("/api/v1/encounters/" + createdEncounter.id() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid status transition from PLANNED to FINISHED"));
    }
}
