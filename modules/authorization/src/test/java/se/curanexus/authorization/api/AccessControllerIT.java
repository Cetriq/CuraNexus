package se.curanexus.authorization.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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
import se.curanexus.authorization.api.dto.*;
import se.curanexus.authorization.domain.CareRelationType;
import se.curanexus.authorization.domain.UserType;

import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AccessControllerIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
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

    private String doctorUserId;
    private String nurseUserId;
    private UUID patientId;

    @BeforeEach
    void setUp() throws Exception {
        // Create a doctor user
        CreateUserRequest doctorRequest = new CreateUserRequest(
                "accessdoctor" + System.currentTimeMillis(),
                "accessdoctor" + System.currentTimeMillis() + "@example.com",
                "Access",
                "Doctor",
                UserType.INTERNAL,
                null, null, null
        );

        MvcResult doctorResult = mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(doctorRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        doctorUserId = objectMapper.readTree(doctorResult.getResponse().getContentAsString())
                .get("id").asText();

        // Assign DOCTOR role
        AssignRolesRequest assignDoctorRole = new AssignRolesRequest(Set.of("DOCTOR"));
        mockMvc.perform(post("/api/v1/users/" + doctorUserId + "/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignDoctorRole)))
                .andExpect(status().isOk());

        // Create a nurse user
        CreateUserRequest nurseRequest = new CreateUserRequest(
                "accessnurse" + System.currentTimeMillis(),
                "accessnurse" + System.currentTimeMillis() + "@example.com",
                "Access",
                "Nurse",
                UserType.INTERNAL,
                null, null, null
        );

        MvcResult nurseResult = mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nurseRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        nurseUserId = objectMapper.readTree(nurseResult.getResponse().getContentAsString())
                .get("id").asText();

        // Assign NURSE role
        AssignRolesRequest assignNurseRole = new AssignRolesRequest(Set.of("NURSE"));
        mockMvc.perform(post("/api/v1/users/" + nurseUserId + "/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignNurseRole)))
                .andExpect(status().isOk());

        // Create a patient ID
        patientId = UUID.randomUUID();

        // Create care relation for doctor
        CreateCareRelationRequest careRelationRequest = new CreateCareRelationRequest(
                UUID.fromString(doctorUserId),
                patientId,
                null,
                CareRelationType.PRIMARY_CARE,
                null,
                null,
                "Primary care assignment"
        );

        mockMvc.perform(post("/api/v1/care-relations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(careRelationRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldGrantAccessWhenUserHasPermission() throws Exception {
        mockMvc.perform(get("/api/v1/access/check/permission")
                        .param("userId", doctorUserId)
                        .param("permissionCode", "PATIENT_READ"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.granted").value(true));
    }

    @Test
    void shouldDenyAccessWhenUserLacksPermission() throws Exception {
        mockMvc.perform(get("/api/v1/access/check/permission")
                        .param("userId", nurseUserId)
                        .param("permissionCode", "DIAGNOSIS_DELETE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.granted").value(false))
                .andExpect(jsonPath("$.reason").value("Missing permission: DIAGNOSIS_DELETE"));
    }

    @Test
    void shouldCheckRoleAccess() throws Exception {
        mockMvc.perform(get("/api/v1/access/check/role")
                        .param("userId", doctorUserId)
                        .param("roleCode", "DOCTOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.granted").value(true));

        mockMvc.perform(get("/api/v1/access/check/role")
                        .param("userId", doctorUserId)
                        .param("roleCode", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.granted").value(false));
    }

    @Test
    void shouldCheckCareRelation() throws Exception {
        mockMvc.perform(get("/api/v1/access/check/care-relation")
                        .param("userId", doctorUserId)
                        .param("patientId", patientId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.granted").value(true));

        // Nurse has no care relation
        mockMvc.perform(get("/api/v1/access/check/care-relation")
                        .param("userId", nurseUserId)
                        .param("patientId", patientId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.granted").value(false));
    }

    @Test
    void shouldCheckFullPatientAccess() throws Exception {
        // Doctor has both permission and care relation
        mockMvc.perform(get("/api/v1/access/check/patient-access")
                        .param("userId", doctorUserId)
                        .param("patientId", patientId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.granted").value(true));

        // Nurse has permission but no care relation
        mockMvc.perform(get("/api/v1/access/check/patient-access")
                        .param("userId", nurseUserId)
                        .param("patientId", patientId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.granted").value(false));
    }

    @Test
    void shouldCheckMultiplePermissions() throws Exception {
        CheckAccessRequest request = new CheckAccessRequest(
                UUID.fromString(doctorUserId),
                null,
                null,
                Set.of("PATIENT_READ", "NOTE_CREATE", "DIAGNOSIS_CREATE")
        );

        mockMvc.perform(post("/api/v1/access/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.granted").value(true))
                .andExpect(jsonPath("$.permissionResults.PATIENT_READ").value(true))
                .andExpect(jsonPath("$.permissionResults.NOTE_CREATE").value(true))
                .andExpect(jsonPath("$.permissionResults.DIAGNOSIS_CREATE").value(true));
    }

    @Test
    void shouldCheckCombinedAccess() throws Exception {
        CheckAccessRequest request = new CheckAccessRequest(
                UUID.fromString(doctorUserId),
                patientId,
                "PATIENT_READ",
                null
        );

        mockMvc.perform(post("/api/v1/access/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.granted").value(true));
    }

    @Test
    void shouldDenyCombinedAccessWhenMissingCareRelation() throws Exception {
        CheckAccessRequest request = new CheckAccessRequest(
                UUID.fromString(nurseUserId),
                patientId,
                "PATIENT_READ",
                null
        );

        mockMvc.perform(post("/api/v1/access/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.granted").value(false))
                .andExpect(jsonPath("$.reason").value("No active care relation with patient"));
    }
}
