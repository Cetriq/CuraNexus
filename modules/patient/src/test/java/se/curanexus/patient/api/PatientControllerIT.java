package se.curanexus.patient.api;

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
import se.curanexus.patient.api.dto.*;
import se.curanexus.patient.domain.ContactType;
import se.curanexus.patient.domain.ConsentType;
import se.curanexus.patient.domain.RelationshipType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class PatientControllerIT {

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
    void createPatient_shouldReturn201() throws Exception {
        CreatePatientRequest request = new CreatePatientRequest(
                "199101011234",
                "Test",
                "Patient",
                null,
                false
        );

        mockMvc.perform(post("/api/v1/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.personalIdentityNumber").value("199101011234"))
                .andExpect(jsonPath("$.givenName").value("Test"))
                .andExpect(jsonPath("$.familyName").value("Patient"));
    }

    @Test
    void createPatient_shouldReturn400ForInvalidPersonnummer() throws Exception {
        CreatePatientRequest request = new CreatePatientRequest(
                "12345",
                "Test",
                "Patient",
                null,
                false
        );

        mockMvc.perform(post("/api/v1/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPatient_shouldReturn409ForDuplicate() throws Exception {
        CreatePatientRequest request = new CreatePatientRequest(
                "199102021234",
                "Test",
                "Patient",
                null,
                false
        );

        // Create first
        mockMvc.perform(post("/api/v1/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Try to create duplicate
        mockMvc.perform(post("/api/v1/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void getPatient_shouldReturn404ForNonExistent() throws Exception {
        mockMvc.perform(get("/api/v1/patients/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound());
    }

    @Test
    void fullPatientWorkflow_shouldWork() throws Exception {
        // Create patient
        CreatePatientRequest createRequest = new CreatePatientRequest(
                "199103031234",
                "Workflow",
                "Test",
                null,
                false
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        PatientDto createdPatient = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                PatientDto.class
        );
        String patientId = createdPatient.id().toString();

        // Get patient
        mockMvc.perform(get("/api/v1/patients/" + patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.givenName").value("Workflow"));

        // Add contact
        CreateContactRequest contactRequest = new CreateContactRequest(
                ContactType.EMAIL,
                "workflow@test.com",
                null,
                true,
                null,
                null
        );

        MvcResult contactResult = mockMvc.perform(post("/api/v1/patients/" + patientId + "/contacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(contactRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        ContactInfoDto createdContact = objectMapper.readValue(
                contactResult.getResponse().getContentAsString(),
                ContactInfoDto.class
        );

        // Get contacts
        mockMvc.perform(get("/api/v1/patients/" + patientId + "/contacts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].value").value("workflow@test.com"));

        // Add related person
        CreateRelatedPersonRequest relatedPersonRequest = new CreateRelatedPersonRequest(
                RelationshipType.SPOUSE,
                null,
                "Related",
                "Person",
                "0701234567",
                null,
                true,
                false,
                null,
                null
        );

        mockMvc.perform(post("/api/v1/patients/" + patientId + "/related-persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(relatedPersonRequest)))
                .andExpect(status().isCreated());

        // Register consent
        CreateConsentRequest consentRequest = new CreateConsentRequest(
                ConsentType.DATA_SHARING,
                "patient",
                null,
                null,
                "Share with primary care"
        );

        MvcResult consentResult = mockMvc.perform(post("/api/v1/patients/" + patientId + "/consents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(consentRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        ConsentDto createdConsent = objectMapper.readValue(
                consentResult.getResponse().getContentAsString(),
                ConsentDto.class
        );

        // Get consents
        mockMvc.perform(get("/api/v1/patients/" + patientId + "/consents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("DATA_SHARING"));

        // Revoke consent
        mockMvc.perform(delete("/api/v1/patients/" + patientId + "/consents/" + createdConsent.id()))
                .andExpect(status().isNoContent());

        // Update patient
        UpdatePatientRequest updateRequest = new UpdatePatientRequest(
                "UpdatedName",
                null,
                null,
                true,
                null,
                null
        );

        mockMvc.perform(put("/api/v1/patients/" + patientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.givenName").value("UpdatedName"))
                .andExpect(jsonPath("$.protectedIdentity").value(true));

        // Search patients
        mockMvc.perform(get("/api/v1/patients")
                        .param("name", "UpdatedName"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].givenName").value("UpdatedName"));

        // Delete contact
        mockMvc.perform(delete("/api/v1/patients/" + patientId + "/contacts/" + createdContact.id()))
                .andExpect(status().isNoContent());
    }
}
