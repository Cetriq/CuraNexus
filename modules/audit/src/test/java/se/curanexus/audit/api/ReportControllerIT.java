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
import se.curanexus.audit.api.dto.AccessLogRequest;
import se.curanexus.audit.api.dto.AuditEventRequest;
import se.curanexus.audit.api.dto.SecurityEventRequest;
import se.curanexus.audit.domain.*;
import se.curanexus.audit.repository.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@DisplayName("ReportController Integration Tests")
class ReportControllerIT {

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
    private AuditEventRepository auditEventRepository;

    @Autowired
    private AccessLogRepository accessLogRepository;

    @Autowired
    private ChangeLogRepository changeLogRepository;

    @Autowired
    private SecurityEventRepository securityEventRepository;

    private UUID userId;
    private UUID patientId;

    @BeforeEach
    void setUp() {
        auditEventRepository.deleteAll();
        accessLogRepository.deleteAll();
        changeLogRepository.deleteAll();
        securityEventRepository.deleteAll();

        userId = UUID.randomUUID();
        patientId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should generate user activity report")
    void shouldGenerateUserActivityReport() throws Exception {
        // Create some audit events
        for (int i = 0; i < 5; i++) {
            AuditEventRequest request = new AuditEventRequest(
                    AuditEventType.READ,
                    userId,
                    "dr.smith",
                    ResourceType.PATIENT,
                    null, patientId, null, null, null, null, null, null
            );
            mockMvc.perform(post("/api/v1/audit/events")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        // Create a login event
        SecurityEventRequest loginRequest = new SecurityEventRequest(
                userId, "dr.smith", SecurityEventType.LOGIN, true, "192.168.1.1", "Chrome", null
        );
        mockMvc.perform(post("/api/v1/audit/security-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isCreated());

        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(7);

        mockMvc.perform(get("/api/v1/audit/reports/user-activity/{userId}", userId)
                        .param("fromDate", weekAgo.toString())
                        .param("toDate", today.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.totalEvents").value(5))
                .andExpect(jsonPath("$.loginCount").value(1))
                .andExpect(jsonPath("$.patientsAccessed").value(1));
    }

    @Test
    @DisplayName("Should generate patient access report")
    void shouldGeneratePatientAccessReport() throws Exception {
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();

        // Create access logs for patient
        AccessLogRequest request1 = new AccessLogRequest(
                user1, "dr.smith", patientId, ResourceType.PATIENT, null,
                AccessType.VIEW, null, "PHYSICIAN", null, "192.168.1.1"
        );
        AccessLogRequest request2 = new AccessLogRequest(
                user1, "dr.smith", patientId, ResourceType.JOURNAL_ENTRY, null,
                AccessType.VIEW, null, "PHYSICIAN", null, "192.168.1.1"
        );
        AccessLogRequest request3 = new AccessLogRequest(
                user2, "nurse.anna", patientId, ResourceType.PATIENT, null,
                AccessType.VIEW, null, "NURSE", null, "192.168.1.2"
        );

        mockMvc.perform(post("/api/v1/audit/access-logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/audit/access-logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/audit/access-logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request3)))
                .andExpect(status().isCreated());

        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(7);

        mockMvc.perform(get("/api/v1/audit/reports/patient-access/{patientId}", patientId)
                        .param("fromDate", weekAgo.toString())
                        .param("toDate", today.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").value(patientId.toString()))
                .andExpect(jsonPath("$.totalAccesses").value(3))
                .andExpect(jsonPath("$.uniqueUsers").value(2))
                .andExpect(jsonPath("$.accessByUser", hasSize(2)))
                .andExpect(jsonPath("$.accessByResourceType.PATIENT").value(2))
                .andExpect(jsonPath("$.accessByResourceType.JOURNAL_ENTRY").value(1));
    }

    @Test
    @DisplayName("Should generate system audit summary")
    void shouldGenerateSystemAuditSummary() throws Exception {
        // Create various audit events
        for (int i = 0; i < 10; i++) {
            AuditEventRequest request = new AuditEventRequest(
                    i % 2 == 0 ? AuditEventType.READ : AuditEventType.CREATE,
                    UUID.randomUUID(),
                    "user" + i,
                    ResourceType.PATIENT,
                    null, UUID.randomUUID(), null, null, null, null, null, null
            );
            mockMvc.perform(post("/api/v1/audit/events")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        // Create security events
        for (int i = 0; i < 5; i++) {
            SecurityEventRequest loginRequest = new SecurityEventRequest(
                    UUID.randomUUID(), "user" + i, SecurityEventType.LOGIN, true, "192.168.1." + i, "Chrome", null
            );
            mockMvc.perform(post("/api/v1/audit/security-events")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isCreated());
        }

        // Create a failed login
        SecurityEventRequest failedLogin = new SecurityEventRequest(
                null, "hacker", SecurityEventType.LOGIN_FAILED, false, "1.2.3.4", "EvilBot", Map.of("reason", "invalid")
        );
        mockMvc.perform(post("/api/v1/audit/security-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(failedLogin)))
                .andExpect(status().isCreated());

        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(7);

        mockMvc.perform(get("/api/v1/audit/reports/system-summary")
                        .param("fromDate", weekAgo.toString())
                        .param("toDate", today.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEvents").value(10))
                .andExpect(jsonPath("$.totalUsers").value(10))
                .andExpect(jsonPath("$.totalPatientsAccessed").value(10))
                .andExpect(jsonPath("$.eventsByType.READ").value(5))
                .andExpect(jsonPath("$.eventsByType.CREATE").value(5))
                .andExpect(jsonPath("$.securityEventsSummary.totalLogins").value(5))
                .andExpect(jsonPath("$.securityEventsSummary.failedLogins").value(1));
    }

    @Test
    @DisplayName("Should return empty report for no data")
    void shouldReturnEmptyReportForNoData() throws Exception {
        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(7);

        mockMvc.perform(get("/api/v1/audit/reports/user-activity/{userId}", UUID.randomUUID())
                        .param("fromDate", weekAgo.toString())
                        .param("toDate", today.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEvents").value(0))
                .andExpect(jsonPath("$.loginCount").value(0));
    }
}
