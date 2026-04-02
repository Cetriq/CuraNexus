package se.curanexus.audit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import se.curanexus.audit.domain.*;
import se.curanexus.audit.repository.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuditService Tests")
class AuditServiceTest {

    @Mock
    private AuditEventRepository auditEventRepository;

    @Mock
    private AccessLogRepository accessLogRepository;

    @Mock
    private ChangeLogRepository changeLogRepository;

    @Mock
    private SecurityEventRepository securityEventRepository;

    private AuditService auditService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        auditService = new AuditService(
                auditEventRepository,
                accessLogRepository,
                changeLogRepository,
                securityEventRepository,
                objectMapper
        );
    }

    @Nested
    @DisplayName("Audit Event Tests")
    class AuditEventTests {

        @Test
        @DisplayName("Should record audit event with all fields")
        void shouldRecordAuditEventWithAllFields() {
            UUID userId = UUID.randomUUID();
            UUID resourceId = UUID.randomUUID();
            UUID patientId = UUID.randomUUID();
            UUID careRelationId = UUID.randomUUID();
            Map<String, Object> details = Map.of("action", "view", "page", 1);

            when(auditEventRepository.save(any(AuditEvent.class))).thenAnswer(inv -> inv.getArgument(0));

            AuditEvent result = auditService.recordEvent(
                    AuditEventType.READ,
                    userId,
                    ResourceType.PATIENT,
                    resourceId,
                    patientId,
                    "VIEW_PATIENT",
                    details,
                    "192.168.1.1",
                    "Mozilla/5.0",
                    careRelationId,
                    "Regular care",
                    "dr.smith"
            );

            assertThat(result.getEventType()).isEqualTo(AuditEventType.READ);
            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getResourceType()).isEqualTo(ResourceType.PATIENT);
            assertThat(result.getResourceId()).isEqualTo(resourceId);
            assertThat(result.getPatientId()).isEqualTo(patientId);
            assertThat(result.getAction()).isEqualTo("VIEW_PATIENT");
            assertThat(result.getDetails()).contains("action");
            assertThat(result.getIpAddress()).isEqualTo("192.168.1.1");
            assertThat(result.getUserAgent()).isEqualTo("Mozilla/5.0");
            assertThat(result.getCareRelationId()).isEqualTo(careRelationId);
            assertThat(result.getReason()).isEqualTo("Regular care");
            assertThat(result.getUsername()).isEqualTo("dr.smith");

            verify(auditEventRepository).save(any(AuditEvent.class));
        }

        @Test
        @DisplayName("Should handle null details gracefully")
        void shouldHandleNullDetailsGracefully() {
            UUID userId = UUID.randomUUID();

            when(auditEventRepository.save(any(AuditEvent.class))).thenAnswer(inv -> inv.getArgument(0));

            AuditEvent result = auditService.recordEvent(
                    AuditEventType.CREATE,
                    userId,
                    ResourceType.TASK,
                    null, null, null, null, null, null, null, null, null
            );

            assertThat(result.getDetails()).isNull();
        }

        @Test
        @DisplayName("Should get event by ID")
        void shouldGetEventById() {
            UUID eventId = UUID.randomUUID();
            AuditEvent event = new AuditEvent(AuditEventType.READ, UUID.randomUUID(), ResourceType.PATIENT);

            when(auditEventRepository.findById(eventId)).thenReturn(Optional.of(event));

            Optional<AuditEvent> result = auditService.getEvent(eventId);

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(event);
        }

        @Test
        @DisplayName("Should search events with filters")
        void shouldSearchEventsWithFilters() {
            UUID userId = UUID.randomUUID();
            var pageable = PageRequest.of(0, 10);
            var events = List.of(new AuditEvent(AuditEventType.READ, userId, ResourceType.PATIENT));

            when(auditEventRepository.searchEvents(eq(userId), any(), any(), any(), any(), any(), eq(pageable)))
                    .thenReturn(new PageImpl<>(events));

            var result = auditService.searchEvents(userId, null, null, null, null, null, pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(auditEventRepository).searchEvents(userId, null, null, null, null, null, pageable);
        }
    }

    @Nested
    @DisplayName("Access Log Tests")
    class AccessLogTests {

        @Test
        @DisplayName("Should record access log")
        void shouldRecordAccessLog() {
            UUID userId = UUID.randomUUID();
            UUID patientId = UUID.randomUUID();
            UUID resourceId = UUID.randomUUID();
            UUID careRelationId = UUID.randomUUID();

            when(accessLogRepository.save(any(AccessLog.class))).thenAnswer(inv -> inv.getArgument(0));

            AccessLog result = auditService.recordAccess(
                    userId,
                    "nurse.anna",
                    patientId,
                    ResourceType.JOURNAL_ENTRY,
                    resourceId,
                    AccessType.VIEW,
                    careRelationId,
                    "PRIMARY_NURSE",
                    "Patient care",
                    "10.0.0.1"
            );

            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getUsername()).isEqualTo("nurse.anna");
            assertThat(result.getPatientId()).isEqualTo(patientId);
            assertThat(result.getResourceType()).isEqualTo(ResourceType.JOURNAL_ENTRY);
            assertThat(result.getAccessType()).isEqualTo(AccessType.VIEW);

            verify(accessLogRepository).save(any(AccessLog.class));
        }

        @Test
        @DisplayName("Should get patient access history")
        void shouldGetPatientAccessHistory() {
            UUID patientId = UUID.randomUUID();
            Instant from = Instant.now().minusSeconds(3600);
            Instant to = Instant.now();

            var logs = List.of(
                    new AccessLog(UUID.randomUUID(), patientId, ResourceType.PATIENT, AccessType.VIEW)
            );

            when(accessLogRepository.findByPatientIdAndTimestampBetween(patientId, from, to))
                    .thenReturn(logs);

            var result = auditService.getPatientAccessHistory(patientId, from, to);

            assertThat(result).hasSize(1);
            verify(accessLogRepository).findByPatientIdAndTimestampBetween(patientId, from, to);
        }
    }

    @Nested
    @DisplayName("Change Log Tests")
    class ChangeLogTests {

        @Test
        @DisplayName("Should record change log")
        void shouldRecordChangeLog() {
            UUID userId = UUID.randomUUID();
            UUID resourceId = UUID.randomUUID();
            UUID patientId = UUID.randomUUID();

            when(changeLogRepository.save(any(ChangeLog.class))).thenAnswer(inv -> inv.getArgument(0));

            ChangeLog result = auditService.recordChange(
                    userId,
                    "admin",
                    ResourceType.PATIENT,
                    resourceId,
                    patientId,
                    ChangeType.FIELD_CHANGE,
                    "phoneNumber",
                    "070-1234567",
                    "070-9876543"
            );

            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getUsername()).isEqualTo("admin");
            assertThat(result.getResourceType()).isEqualTo(ResourceType.PATIENT);
            assertThat(result.getChangeType()).isEqualTo(ChangeType.FIELD_CHANGE);
            assertThat(result.getFieldName()).isEqualTo("phoneNumber");
            assertThat(result.getOldValue()).isEqualTo("070-1234567");
            assertThat(result.getNewValue()).isEqualTo("070-9876543");

            verify(changeLogRepository).save(any(ChangeLog.class));
        }

        @Test
        @DisplayName("Should get resource history")
        void shouldGetResourceHistory() {
            UUID resourceId = UUID.randomUUID();
            var logs = List.of(
                    new ChangeLog(UUID.randomUUID(), ResourceType.PATIENT, resourceId, ChangeType.UPDATE)
            );

            when(changeLogRepository.findByResourceTypeAndResourceIdOrderByTimestampDesc(ResourceType.PATIENT, resourceId))
                    .thenReturn(logs);

            var result = auditService.getResourceHistory(ResourceType.PATIENT, resourceId);

            assertThat(result).hasSize(1);
            verify(changeLogRepository).findByResourceTypeAndResourceIdOrderByTimestampDesc(ResourceType.PATIENT, resourceId);
        }
    }

    @Nested
    @DisplayName("Security Event Tests")
    class SecurityEventTests {

        @Test
        @DisplayName("Should record security event")
        void shouldRecordSecurityEvent() {
            UUID userId = UUID.randomUUID();
            Map<String, Object> details = Map.of("method", "password", "mfa", true);

            when(securityEventRepository.save(any(SecurityEvent.class))).thenAnswer(inv -> inv.getArgument(0));

            SecurityEvent result = auditService.recordSecurityEvent(
                    userId,
                    "user123",
                    SecurityEventType.LOGIN,
                    true,
                    "192.168.1.100",
                    "Chrome/120.0",
                    details
            );

            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getUsername()).isEqualTo("user123");
            assertThat(result.getEventType()).isEqualTo(SecurityEventType.LOGIN);
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getIpAddress()).isEqualTo("192.168.1.100");
            assertThat(result.getUserAgent()).isEqualTo("Chrome/120.0");
            assertThat(result.getDetails()).contains("mfa");

            verify(securityEventRepository).save(any(SecurityEvent.class));
        }

        @Test
        @DisplayName("Should record failed login without user ID")
        void shouldRecordFailedLoginWithoutUserId() {
            when(securityEventRepository.save(any(SecurityEvent.class))).thenAnswer(inv -> inv.getArgument(0));

            SecurityEvent result = auditService.recordSecurityEvent(
                    null,
                    "unknown_user",
                    SecurityEventType.LOGIN_FAILED,
                    false,
                    "1.2.3.4",
                    "EvilBot/1.0",
                    Map.of("reason", "invalid_credentials")
            );

            assertThat(result.getUserId()).isNull();
            assertThat(result.getUsername()).isEqualTo("unknown_user");
            assertThat(result.getEventType()).isEqualTo(SecurityEventType.LOGIN_FAILED);
            assertThat(result.isSuccess()).isFalse();
        }
    }

    @Nested
    @DisplayName("Report Generation Tests")
    class ReportTests {

        @Test
        @DisplayName("Should generate user activity report")
        void shouldGenerateUserActivityReport() {
            UUID userId = UUID.randomUUID();
            LocalDate from = LocalDate.now().minusDays(7);
            LocalDate to = LocalDate.now();

            when(auditEventRepository.countByUserIdAndTimestampBetween(eq(userId), any(), any())).thenReturn(100L);
            when(changeLogRepository.countByUserIdAndTimestampBetween(eq(userId), any(), any())).thenReturn(20L);
            when(auditEventRepository.countDistinctPatientsAccessedByUser(eq(userId), any(), any())).thenReturn(15L);
            when(securityEventRepository.countByUserIdAndEventType(eq(userId), eq(SecurityEventType.LOGIN), any(), any())).thenReturn(5L);
            when(auditEventRepository.countByUserIdGroupByEventType(eq(userId), any(), any())).thenReturn(
                    List.of(new Object[]{AuditEventType.READ, 80L}, new Object[]{AuditEventType.UPDATE, 20L})
            );

            var report = auditService.generateUserActivityReport(userId, from, to);

            assertThat(report.userId()).isEqualTo(userId);
            assertThat(report.totalEvents()).isEqualTo(100L);
            assertThat(report.modificationCount()).isEqualTo(20L);
            assertThat(report.accessCount()).isEqualTo(80L); // totalEvents - modificationCount
            assertThat(report.patientsAccessed()).isEqualTo(15L);
            assertThat(report.loginCount()).isEqualTo(5L);
            assertThat(report.eventsByType()).containsKeys("READ", "UPDATE");
        }

        @Test
        @DisplayName("Should generate patient access report")
        void shouldGeneratePatientAccessReport() {
            UUID patientId = UUID.randomUUID();
            LocalDate from = LocalDate.now().minusDays(30);
            LocalDate to = LocalDate.now();

            when(accessLogRepository.countByPatientIdAndTimestampBetween(eq(patientId), any(), any())).thenReturn(50L);
            when(accessLogRepository.countDistinctUsersByPatientId(eq(patientId), any(), any())).thenReturn(5L);
            List<Object[]> accessStats = new ArrayList<>();
            accessStats.add(new Object[]{UUID.randomUUID(), "dr.smith", 30L, "PHYSICIAN", Instant.now()});
            when(accessLogRepository.getAccessStatsByPatient(eq(patientId), any(), any())).thenReturn(accessStats);

            List<Object[]> resourceTypeStats = new ArrayList<>();
            resourceTypeStats.add(new Object[]{ResourceType.JOURNAL_ENTRY, 40L});
            resourceTypeStats.add(new Object[]{ResourceType.PATIENT, 10L});
            when(accessLogRepository.countByPatientIdGroupByResourceType(eq(patientId), any(), any())).thenReturn(resourceTypeStats);

            var report = auditService.generatePatientAccessReport(patientId, from, to);

            assertThat(report.patientId()).isEqualTo(patientId);
            assertThat(report.totalAccesses()).isEqualTo(50L);
            assertThat(report.uniqueUsers()).isEqualTo(5L);
            assertThat(report.accessByUser()).hasSize(1);
            assertThat(report.accessByResourceType()).containsKeys("JOURNAL_ENTRY", "PATIENT");
        }

        @Test
        @DisplayName("Should generate system audit summary")
        void shouldGenerateSystemAuditSummary() {
            LocalDate from = LocalDate.now().minusDays(7);
            LocalDate to = LocalDate.now();

            when(auditEventRepository.countInPeriod(any(), any())).thenReturn(1000L);
            when(auditEventRepository.countDistinctUsersInPeriod(any(), any())).thenReturn(50L);
            when(auditEventRepository.countDistinctPatientsInPeriod(any(), any())).thenReturn(200L);
            when(auditEventRepository.countByEventTypeInPeriod(any(), any())).thenReturn(
                    List.of(new Object[]{AuditEventType.READ, 800L}, new Object[]{AuditEventType.CREATE, 200L})
            );
            when(securityEventRepository.countByEventTypeInPeriod(eq(SecurityEventType.LOGIN), any(), any())).thenReturn(300L);
            when(securityEventRepository.countFailedByEventTypeInPeriod(eq(SecurityEventType.LOGIN_FAILED), any(), any())).thenReturn(10L);
            when(securityEventRepository.countByEventTypeInPeriod(eq(SecurityEventType.PERMISSION_DENIED), any(), any())).thenReturn(5L);
            when(securityEventRepository.countByEventTypeInPeriod(eq(SecurityEventType.EMERGENCY_ACCESS), any(), any())).thenReturn(2L);

            var summary = auditService.generateSystemSummary(from, to);

            assertThat(summary.totalEvents()).isEqualTo(1000L);
            assertThat(summary.totalUsers()).isEqualTo(50L);
            assertThat(summary.totalPatientsAccessed()).isEqualTo(200L);
            assertThat(summary.eventsByType()).containsKeys("READ", "CREATE");
            assertThat(summary.securityEventsSummary().totalLogins()).isEqualTo(300L);
            assertThat(summary.securityEventsSummary().failedLogins()).isEqualTo(10L);
            assertThat(summary.securityEventsSummary().permissionDenied()).isEqualTo(5L);
            assertThat(summary.securityEventsSummary().emergencyAccess()).isEqualTo(2L);
        }
    }
}
