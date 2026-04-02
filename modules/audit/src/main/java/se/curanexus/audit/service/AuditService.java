package se.curanexus.audit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.curanexus.audit.domain.*;
import se.curanexus.audit.repository.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

@Service
@Transactional
public class AuditService {

    private final AuditEventRepository auditEventRepository;
    private final AccessLogRepository accessLogRepository;
    private final ChangeLogRepository changeLogRepository;
    private final SecurityEventRepository securityEventRepository;
    private final ObjectMapper objectMapper;

    public AuditService(
            AuditEventRepository auditEventRepository,
            AccessLogRepository accessLogRepository,
            ChangeLogRepository changeLogRepository,
            SecurityEventRepository securityEventRepository,
            ObjectMapper objectMapper) {
        this.auditEventRepository = auditEventRepository;
        this.accessLogRepository = accessLogRepository;
        this.changeLogRepository = changeLogRepository;
        this.securityEventRepository = securityEventRepository;
        this.objectMapper = objectMapper;
    }

    // ========== Audit Events ==========

    public AuditEvent recordEvent(AuditEventType eventType, UUID userId, ResourceType resourceType,
                                   UUID resourceId, UUID patientId, String action, Map<String, Object> details,
                                   String ipAddress, String userAgent, UUID careRelationId, String reason, String username) {
        AuditEvent event = new AuditEvent(eventType, userId, resourceType);
        event.setResourceId(resourceId);
        event.setPatientId(patientId);
        event.setAction(action);
        event.setUsername(username);
        if (details != null) {
            try {
                event.setDetails(objectMapper.writeValueAsString(details));
            } catch (JsonProcessingException e) {
                event.setDetails("{}");
            }
        }
        event.setIpAddress(ipAddress);
        event.setUserAgent(userAgent);
        event.setCareRelationId(careRelationId);
        event.setReason(reason);
        return auditEventRepository.save(event);
    }

    @Async
    public void recordEventAsync(AuditEventType eventType, UUID userId, ResourceType resourceType,
                                  UUID resourceId, UUID patientId, String action, Map<String, Object> details,
                                  String ipAddress, String userAgent, UUID careRelationId, String reason, String username) {
        recordEvent(eventType, userId, resourceType, resourceId, patientId, action, details, ipAddress, userAgent, careRelationId, reason, username);
    }

    @Transactional(readOnly = true)
    public Optional<AuditEvent> getEvent(UUID eventId) {
        return auditEventRepository.findById(eventId);
    }

    @Transactional(readOnly = true)
    public Page<AuditEvent> searchEvents(UUID userId, ResourceType resourceType, UUID resourceId,
                                          AuditEventType eventType, Instant fromDate, Instant toDate,
                                          Pageable pageable) {
        return auditEventRepository.searchEvents(userId, resourceType, resourceId, eventType, fromDate, toDate, pageable);
    }

    // ========== Access Logs ==========

    public AccessLog recordAccess(UUID userId, String username, UUID patientId, ResourceType resourceType,
                                   UUID resourceId, AccessType accessType, UUID careRelationId,
                                   String careRelationType, String reason, String ipAddress) {
        AccessLog log = new AccessLog(userId, patientId, resourceType, accessType);
        log.setUsername(username);
        log.setResourceId(resourceId);
        log.setCareRelationId(careRelationId);
        log.setCareRelationType(careRelationType);
        log.setReason(reason);
        log.setIpAddress(ipAddress);
        return accessLogRepository.save(log);
    }

    @Async
    public void recordAccessAsync(UUID userId, String username, UUID patientId, ResourceType resourceType,
                                   UUID resourceId, AccessType accessType, UUID careRelationId,
                                   String careRelationType, String reason, String ipAddress) {
        recordAccess(userId, username, patientId, resourceType, resourceId, accessType, careRelationId, careRelationType, reason, ipAddress);
    }

    @Transactional(readOnly = true)
    public Page<AccessLog> searchAccessLogs(UUID patientId, UUID userId, Instant fromDate, Instant toDate, Pageable pageable) {
        return accessLogRepository.searchAccessLogs(patientId, userId, fromDate, toDate, pageable);
    }

    @Transactional(readOnly = true)
    public List<AccessLog> getPatientAccessHistory(UUID patientId, Instant fromDate, Instant toDate) {
        return accessLogRepository.findByPatientIdAndTimestampBetween(patientId, fromDate, toDate);
    }

    // ========== Change Logs ==========

    public ChangeLog recordChange(UUID userId, String username, ResourceType resourceType, UUID resourceId,
                                   UUID patientId, ChangeType changeType, String fieldName,
                                   String oldValue, String newValue) {
        ChangeLog log = new ChangeLog(userId, resourceType, resourceId, changeType);
        log.setUsername(username);
        log.setPatientId(patientId);
        log.setFieldName(fieldName);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        return changeLogRepository.save(log);
    }

    @Async
    public void recordChangeAsync(UUID userId, String username, ResourceType resourceType, UUID resourceId,
                                   UUID patientId, ChangeType changeType, String fieldName,
                                   String oldValue, String newValue) {
        recordChange(userId, username, resourceType, resourceId, patientId, changeType, fieldName, oldValue, newValue);
    }

    @Transactional(readOnly = true)
    public Page<ChangeLog> searchChangeLogs(ResourceType resourceType, UUID resourceId, UUID userId,
                                             ChangeType changeType, Instant fromDate, Instant toDate,
                                             Pageable pageable) {
        return changeLogRepository.searchChangeLogs(resourceType, resourceId, userId, changeType, fromDate, toDate, pageable);
    }

    @Transactional(readOnly = true)
    public List<ChangeLog> getResourceHistory(ResourceType resourceType, UUID resourceId) {
        return changeLogRepository.findByResourceTypeAndResourceIdOrderByTimestampDesc(resourceType, resourceId);
    }

    // ========== Security Events ==========

    public SecurityEvent recordSecurityEvent(UUID userId, String username, SecurityEventType eventType,
                                              boolean success, String ipAddress, String userAgent,
                                              Map<String, Object> details) {
        SecurityEvent event = new SecurityEvent(userId, eventType, success);
        event.setUsername(username);
        event.setIpAddress(ipAddress);
        event.setUserAgent(userAgent);
        if (details != null) {
            try {
                event.setDetails(objectMapper.writeValueAsString(details));
            } catch (JsonProcessingException e) {
                event.setDetails("{}");
            }
        }
        return securityEventRepository.save(event);
    }

    @Async
    public void recordSecurityEventAsync(UUID userId, String username, SecurityEventType eventType,
                                          boolean success, String ipAddress, String userAgent,
                                          Map<String, Object> details) {
        recordSecurityEvent(userId, username, eventType, success, ipAddress, userAgent, details);
    }

    @Transactional(readOnly = true)
    public Page<SecurityEvent> searchSecurityEvents(UUID userId, SecurityEventType eventType, Boolean success,
                                                     Instant fromDate, Instant toDate, Pageable pageable) {
        return securityEventRepository.searchSecurityEvents(userId, eventType, success, fromDate, toDate, pageable);
    }

    // ========== Reports ==========

    @Transactional(readOnly = true)
    public UserActivityReport generateUserActivityReport(UUID userId, LocalDate fromDate, LocalDate toDate) {
        Instant from = fromDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant to = toDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

        long totalEvents = auditEventRepository.countByUserIdAndTimestampBetween(userId, from, to);
        long modificationCount = changeLogRepository.countByUserIdAndTimestampBetween(userId, from, to);
        long patientsAccessed = auditEventRepository.countDistinctPatientsAccessedByUser(userId, from, to);
        long loginCount = securityEventRepository.countByUserIdAndEventType(userId, SecurityEventType.LOGIN, from, to);

        Map<String, Long> eventsByType = new HashMap<>();
        for (Object[] row : auditEventRepository.countByUserIdGroupByEventType(userId, from, to)) {
            eventsByType.put(((AuditEventType) row[0]).name(), (Long) row[1]);
        }

        return new UserActivityReport(
                userId,
                null, // username would be fetched from user service
                fromDate,
                toDate,
                totalEvents,
                totalEvents - modificationCount,
                modificationCount,
                patientsAccessed,
                loginCount,
                eventsByType
        );
    }

    @Transactional(readOnly = true)
    public PatientAccessReport generatePatientAccessReport(UUID patientId, LocalDate fromDate, LocalDate toDate) {
        Instant from = fromDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant to = toDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

        long totalAccesses = accessLogRepository.countByPatientIdAndTimestampBetween(patientId, from, to);
        long uniqueUsers = accessLogRepository.countDistinctUsersByPatientId(patientId, from, to);

        List<PatientAccessReport.UserAccessStats> accessByUser = new ArrayList<>();
        for (Object[] row : accessLogRepository.getAccessStatsByPatient(patientId, from, to)) {
            accessByUser.add(new PatientAccessReport.UserAccessStats(
                    (UUID) row[0],
                    (String) row[1],
                    ((Long) row[2]).intValue(),
                    (String) row[3],
                    (Instant) row[4]
            ));
        }

        Map<String, Long> accessByResourceType = new HashMap<>();
        for (Object[] row : accessLogRepository.countByPatientIdGroupByResourceType(patientId, from, to)) {
            accessByResourceType.put(((ResourceType) row[0]).name(), (Long) row[1]);
        }

        return new PatientAccessReport(
                patientId,
                fromDate,
                toDate,
                totalAccesses,
                uniqueUsers,
                accessByUser,
                accessByResourceType
        );
    }

    @Transactional(readOnly = true)
    public SystemAuditSummary generateSystemSummary(LocalDate fromDate, LocalDate toDate) {
        Instant from = fromDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant to = toDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

        long totalEvents = auditEventRepository.countInPeriod(from, to);
        long totalUsers = auditEventRepository.countDistinctUsersInPeriod(from, to);
        long totalPatientsAccessed = auditEventRepository.countDistinctPatientsInPeriod(from, to);

        Map<String, Long> eventsByType = new HashMap<>();
        for (Object[] row : auditEventRepository.countByEventTypeInPeriod(from, to)) {
            eventsByType.put(((AuditEventType) row[0]).name(), (Long) row[1]);
        }

        long totalLogins = securityEventRepository.countByEventTypeInPeriod(SecurityEventType.LOGIN, from, to);
        long failedLogins = securityEventRepository.countFailedByEventTypeInPeriod(SecurityEventType.LOGIN_FAILED, from, to);
        long permissionDenied = securityEventRepository.countByEventTypeInPeriod(SecurityEventType.PERMISSION_DENIED, from, to);
        long emergencyAccess = securityEventRepository.countByEventTypeInPeriod(SecurityEventType.EMERGENCY_ACCESS, from, to);

        return new SystemAuditSummary(
                fromDate,
                toDate,
                totalEvents,
                totalUsers,
                totalPatientsAccessed,
                eventsByType,
                new SystemAuditSummary.SecuritySummary(totalLogins, failedLogins, permissionDenied, emergencyAccess)
        );
    }

    // ========== Report Records ==========

    public record UserActivityReport(
            UUID userId,
            String username,
            LocalDate fromDate,
            LocalDate toDate,
            long totalEvents,
            long accessCount,
            long modificationCount,
            long patientsAccessed,
            long loginCount,
            Map<String, Long> eventsByType
    ) {}

    public record PatientAccessReport(
            UUID patientId,
            LocalDate fromDate,
            LocalDate toDate,
            long totalAccesses,
            long uniqueUsers,
            List<UserAccessStats> accessByUser,
            Map<String, Long> accessByResourceType
    ) {
        public record UserAccessStats(
                UUID userId,
                String username,
                int accessCount,
                String careRelationType,
                Instant lastAccess
        ) {}
    }

    public record SystemAuditSummary(
            LocalDate fromDate,
            LocalDate toDate,
            long totalEvents,
            long totalUsers,
            long totalPatientsAccessed,
            Map<String, Long> eventsByType,
            SecuritySummary securityEventsSummary
    ) {
        public record SecuritySummary(
                long totalLogins,
                long failedLogins,
                long permissionDenied,
                long emergencyAccess
        ) {}
    }
}
