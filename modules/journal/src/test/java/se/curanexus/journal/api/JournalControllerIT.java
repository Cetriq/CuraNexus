package se.curanexus.journal.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import se.curanexus.journal.api.dto.*;
import se.curanexus.journal.domain.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class JournalControllerIT {

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
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // === Clinical Note Tests ===

    @Test
    void shouldCreateAndGetNote() throws Exception {
        CreateNoteRequest request = new CreateNoteRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                NoteType.PROGRESS,
                UUID.randomUUID(),
                "Dr. Smith",
                "Progress Note",
                "Patient is improving"
        );

        MvcResult result = mockMvc.perform(post("/api/v1/journal/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.type").value("PROGRESS"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.title").value("Progress Note"))
                .andReturn();

        NoteResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), NoteResponse.class);

        mockMvc.perform(get("/api/v1/journal/notes/{noteId}", response.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.id().toString()));
    }

    @Test
    void shouldSignNote() throws Exception {
        CreateNoteRequest createRequest = new CreateNoteRequest(
                UUID.randomUUID(), UUID.randomUUID(), NoteType.PROGRESS,
                UUID.randomUUID(), "Dr. Smith", "Title", "Content"
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/journal/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        NoteResponse created = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), NoteResponse.class);

        SignNoteRequest signRequest = new SignNoteRequest(UUID.randomUUID(), "Dr. Johnson");

        mockMvc.perform(post("/api/v1/journal/notes/{noteId}/sign", created.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FINAL"))
                .andExpect(jsonPath("$.signedByName").value("Dr. Johnson"))
                .andExpect(jsonPath("$.signedAt").exists());
    }

    @Test
    void shouldReturnNotFoundForNonExistentNote() throws Exception {
        mockMvc.perform(get("/api/v1/journal/notes/{noteId}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Clinical Note Not Found"));
    }

    // === Diagnosis Tests ===

    @Test
    void shouldCreateAndGetDiagnosis() throws Exception {
        CreateDiagnosisRequest request = new CreateDiagnosisRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "J06.9",
                "ICD-10-SE",
                "Acute upper respiratory infection",
                DiagnosisType.PRINCIPAL,
                1,
                null,
                UUID.randomUUID()
        );

        MvcResult result = mockMvc.perform(post("/api/v1/journal/diagnoses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.code").value("J06.9"))
                .andExpect(jsonPath("$.type").value("PRINCIPAL"))
                .andReturn();

        DiagnosisResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), DiagnosisResponse.class);

        mockMvc.perform(get("/api/v1/journal/diagnoses/{diagnosisId}", response.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("J06.9"));
    }

    // === Procedure Tests ===

    @Test
    void shouldCreateAndStartProcedure() throws Exception {
        CreateProcedureRequest createRequest = new CreateProcedureRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "AA123",
                "KVÅ",
                "Appendectomy",
                "Abdomen",
                "Right",
                null
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/journal/procedures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PLANNED"))
                .andReturn();

        ProcedureResponse created = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), ProcedureResponse.class);

        StartProcedureRequest startRequest = new StartProcedureRequest(UUID.randomUUID(), "Dr. Surgeon");

        mockMvc.perform(post("/api/v1/journal/procedures/{procedureId}/start", created.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(startRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.performedByName").value("Dr. Surgeon"));
    }

    // === Observation Tests ===

    @Test
    void shouldCreateNumericObservation() throws Exception {
        CreateObservationRequest request = new CreateObservationRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "8867-4",
                "LOINC",
                "Heart rate",
                ObservationCategory.VITAL_SIGNS,
                new BigDecimal("72"),
                null,
                null,
                "bpm",
                new BigDecimal("60"),
                new BigDecimal("100"),
                ObservationInterpretation.NORMAL,
                LocalDateTime.now(),
                UUID.randomUUID(),
                "Nurse Jane",
                "Manual",
                null,
                null,
                null
        );

        mockMvc.perform(post("/api/v1/journal/observations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("8867-4"))
                .andExpect(jsonPath("$.category").value("VITAL_SIGNS"))
                .andExpect(jsonPath("$.valueNumeric").value(72))
                .andExpect(jsonPath("$.unit").value("bpm"))
                .andExpect(jsonPath("$.withinReferenceRange").value(true));
    }

    @Test
    void shouldGetObservationsByPatient() throws Exception {
        UUID patientId = UUID.randomUUID();

        CreateObservationRequest request = new CreateObservationRequest(
                patientId,
                UUID.randomUUID(),
                "8867-4",
                null,
                "Heart rate",
                ObservationCategory.VITAL_SIGNS,
                new BigDecimal("72"),
                null,
                null,
                "bpm",
                null,
                null,
                null,
                LocalDateTime.now(),
                UUID.randomUUID(),
                "Nurse",
                null,
                null,
                null,
                null
        );

        mockMvc.perform(post("/api/v1/journal/observations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/journal/patients/{patientId}/observations", patientId)
                        .param("category", "VITAL_SIGNS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code").value("8867-4"));
    }
}
