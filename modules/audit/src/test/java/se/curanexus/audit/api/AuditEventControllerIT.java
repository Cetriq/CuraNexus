package se.curanexus.audit.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import se.curanexus.audit.api.dto.AuditEventRequest;
import se.curanexus.audit.domain.AuditEventType;
import se.curanexus.audit.domain.ResourceType;
import se.curanexus.audit.repository.AuditEventRepository;

import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@DisplayName("AuditEventController Integration Tests")
class AuditEventControllerIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("audit_test")
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

    @Autowired
    private AuditEventRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("Should record audit event")
    void shouldRecordAuditEvent() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();

        AuditEventRequest request = new AuditEventRequest(
                AuditEventType.READ,
                userId,
                "dr.smith",
                ResourceType.PATIENT,
                null,
                patientId,
                "VIEW_PATIENT",
                Map.of("page", 1),
                "192.168.1.1",
                "Mozilla/5.0",
                null,
                "Regular care"
        );

        mockMvc.perform(post("/api/v1/audit/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.eventType").value("READ"))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.username").value("dr.smith"))
                .andExpect(jsonPath("$.resourceType").value("PATIENT"))
                .andExpect(jsonPath("$.patientId").value(patientId.toString()))
                .andExpect(jsonPath("$.action").value("VIEW_PATIENT"));
    }

    @Test
    @DisplayName("Should return 400 for invalid request")
    void shouldReturn400ForInvalidRequest() throws Exception {
        // Missing required fields
        String invalidRequest = """
                {
                    "eventType": null,
                    "userId": null,
                    "resourceType": null
                }
                """;

        mockMvc.perform(post("/api/v1/audit/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));
    }

    @Test
    @DisplayName("Should get audit event by ID")
    void shouldGetAuditEventById() throws Exception {
        // First create an event
        UUID userId = UUID.randomUUID();
        AuditEventRequest request = new AuditEventRequest(
                AuditEventType.CREATE,
                userId,
                "admin",
                ResourceType.USER,
                null, null, "CREATE_USER", null, null, null, null, null
        );

        String response = mockMvc.perform(post("/api/v1/audit/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String eventId = objectMapper.readTree(response).get("id").asText();

        // Then retrieve it
        mockMvc.perform(get("/api/v1/audit/events/{eventId}", eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventId))
                .andExpect(jsonPath("$.eventType").value("CREATE"));
    }

    @Test
    @DisplayName("Should return 404 for non-existent event")
    void shouldReturn404ForNonExistentEvent() throws Exception {
        mockMvc.perform(get("/api/v1/audit/events/{eventId}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should search events with filters")
    void shouldSearchEventsWithFilters() throws Exception {
        UUID userId = UUID.randomUUID();

        // Create some events
        for (int i = 0; i < 3; i++) {
            AuditEventRequest request = new AuditEventRequest(
                    AuditEventType.READ,
                    userId,
                    "user" + i,
                    ResourceType.PATIENT,
                    null, null, null, null, null, null, null, null
            );
            mockMvc.perform(post("/api/v1/audit/events")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        // Create an event with different user
        AuditEventRequest otherRequest = new AuditEventRequest(
                AuditEventType.READ,
                UUID.randomUUID(),
                "other_user",
                ResourceType.PATIENT,
                null, null, null, null, null, null, null, null
        );
        mockMvc.perform(post("/api/v1/audit/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(otherRequest)))
                .andExpect(status().isCreated());

        // Search by user ID
        mockMvc.perform(get("/api/v1/audit/events")
                        .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    @DisplayName("Should support pagination")
    void shouldSupportPagination() throws Exception {
        UUID userId = UUID.randomUUID();

        // Create 5 events
        for (int i = 0; i < 5; i++) {
            AuditEventRequest request = new AuditEventRequest(
                    AuditEventType.READ,
                    userId,
                    "user",
                    ResourceType.PATIENT,
                    null, null, null, null, null, null, null, null
            );
            mockMvc.perform(post("/api/v1/audit/events")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        // Get first page with 2 items
        mockMvc.perform(get("/api/v1/audit/events")
                        .param("userId", userId.toString())
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    @Test
    @DisplayName("Should accept async audit event")
    void shouldAcceptAsyncAuditEvent() throws Exception {
        UUID userId = UUID.randomUUID();

        AuditEventRequest request = new AuditEventRequest(
                AuditEventType.READ,
                userId,
                "async_user",
                ResourceType.PATIENT,
                null, null, null, null, null, null, null, null
        );

        mockMvc.perform(post("/api/v1/audit/events/async")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());
    }
}
