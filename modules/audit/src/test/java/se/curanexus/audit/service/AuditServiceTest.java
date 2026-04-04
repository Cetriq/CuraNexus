package se.curanexus.audit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import se.curanexus.audit.api.dto.*;
import se.curanexus.audit.domain.*;
import se.curanexus.audit.repository.AuditEventRepository;
import se.curanexus.audit.repository.DataChangeLogRepository;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditEventRepository auditEventRepository;
    @Mock
    private DataChangeLogRepository dataChangeLogRepository;
    @InjectMocks
    private AuditService auditService;

    private UUID eventId;
    private UUID patientId;
    private AuditEvent auditEvent;

    @BeforeEach
    void setUp() throws Exception {
        eventId = UUID.randomUUID();
        patientId = UUID.randomUUID();
        auditEvent = new AuditEvent("user123", AuditAction.READ, ResourceType.PATIENT);
        setPrivateField(auditEvent, "id", eventId);
        auditEvent.setPatientId(patientId);
        auditEvent.setUserName("Dr. Test");
        auditEvent.setUserRole("DOCTOR");
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void logEvent_ShouldCreateAuditEvent() {
        CreateAuditEventRequest request = new CreateAuditEventRequest(
            "user123", "HSA123", "Dr. Test", "DOCTOR",
            AuditAction.READ, ResourceType.PATIENT, patientId, "Patient record",
            patientId, null, UUID.randomUUID(), "Akuten", "HSA456",
            "192.168.1.1", "Mozilla/5.0", "session123", UUID.randomUUID(),
            "Vårdrelation", false, null, true, null, null, "patient-service", "corr123"
        );
        when(auditEventRepository.save(any(AuditEvent.class))).thenReturn(auditEvent);

        AuditEventDto result = auditService.logEvent(request);

        assertNotNull(result);
        assertEquals(eventId, result.id());
        verify(auditEventRepository).save(any(AuditEvent.class));
    }

    @Test
    void getEvent_ShouldReturnEvent_WhenExists() {
        when(auditEventRepository.findById(eventId)).thenReturn(Optional.of(auditEvent));

        AuditEventDto result = auditService.getEvent(eventId);

        assertNotNull(result);
        assertEquals(eventId, result.id());
    }

    @Test
    void getEvent_ShouldThrowException_WhenNotExists() {
        when(auditEventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThrows(AuditEventNotFoundException.class, () -> auditService.getEvent(eventId));
    }

    @Test
    void getPatientAuditLog_ShouldReturnPage() {
        Page<AuditEvent> page = new PageImpl<>(List.of(auditEvent));
        when(auditEventRepository.findByPatientIdOrderByTimestampDesc(eq(patientId), any(Pageable.class)))
                .thenReturn(page);

        Page<AuditEventSummaryDto> result = auditService.getPatientAuditLog(patientId, Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getUserAuditLog_ShouldReturnPage() {
        Page<AuditEvent> page = new PageImpl<>(List.of(auditEvent));
        when(auditEventRepository.findByUserIdOrderByTimestampDesc(eq("user123"), any(Pageable.class)))
                .thenReturn(page);

        Page<AuditEventSummaryDto> result = auditService.getUserAuditLog("user123", Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getResourceHistory_ShouldReturnList() {
        when(auditEventRepository.findByResourceTypeAndResourceIdOrderByTimestampDesc(
                ResourceType.PATIENT, patientId)).thenReturn(List.of(auditEvent));

        List<AuditEventSummaryDto> result = auditService.getResourceHistory(ResourceType.PATIENT, patientId);

        assertEquals(1, result.size());
    }

    @Test
    void getStatistics_ShouldReturnStats() {
        Instant from = Instant.now().minusSeconds(86400);
        Instant to = Instant.now();
        when(auditEventRepository.countByTimestampBetween(any(), any())).thenReturn(100L);
        when(auditEventRepository.countByEmergencyAccessTrueAndTimestampBetween(any(), any())).thenReturn(5L);
        when(auditEventRepository.countBySuccessFalseAndTimestampBetween(any(), any())).thenReturn(3L);

        AuditStatisticsDto result = auditService.getStatistics(from, to);

        assertEquals(100L, result.totalEvents());
        assertEquals(5L, result.emergencyAccessEvents());
        assertEquals(3L, result.failedAttempts());
    }

    @Test
    void logDataChanges_ShouldSaveChanges() {
        DataChangeRequest change = new DataChangeRequest(
            ResourceType.PATIENT, patientId, "name", "Old Name", "New Name", DataChangeLog.ChangeType.MODIFIED
        );

        auditService.logDataChanges(eventId, List.of(change));

        verify(dataChangeLogRepository).save(any(DataChangeLog.class));
    }
}
