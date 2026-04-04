package se.curanexus.audit.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.curanexus.audit.api.dto.*;
import se.curanexus.audit.domain.*;
import se.curanexus.audit.repository.AuditEventRepository;
import se.curanexus.audit.repository.DataChangeLogRepository;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AuditService {

    private final AuditEventRepository auditEventRepository;
    private final DataChangeLogRepository dataChangeLogRepository;

    public AuditService(AuditEventRepository auditEventRepository,
                        DataChangeLogRepository dataChangeLogRepository) {
        this.auditEventRepository = auditEventRepository;
        this.dataChangeLogRepository = dataChangeLogRepository;
    }

    public AuditEventDto logEvent(CreateAuditEventRequest request) {
        AuditEvent event = new AuditEvent(request.userId(), request.action(), request.resourceType());
        event.setUserHsaId(request.userHsaId());
        event.setUserName(request.userName());
        event.setUserRole(request.userRole());
        event.setResourceId(request.resourceId());
        event.setResourceDescription(request.resourceDescription());
        event.setPatientId(request.patientId());
        if (request.patientPersonnummer() != null) {
            event.setPatientPersonnummerHash(hashPersonnummer(request.patientPersonnummer()));
        }
        event.setCareUnitId(request.careUnitId());
        event.setCareUnitName(request.careUnitName());
        event.setCareUnitHsaId(request.careUnitHsaId());
        event.setIpAddress(request.ipAddress());
        event.setUserAgent(request.userAgent());
        event.setSessionId(request.sessionId());
        event.setEncounterId(request.encounterId());
        event.setAccessReason(request.accessReason());
        event.setEmergencyAccess(request.emergencyAccess() != null && request.emergencyAccess());
        event.setConsentReference(request.consentReference());
        event.setSuccess(request.success() == null || request.success());
        event.setErrorMessage(request.errorMessage());
        event.setDetails(request.details());
        event.setSourceSystem(request.sourceSystem());
        event.setCorrelationId(request.correlationId());
        AuditEvent saved = auditEventRepository.save(event);
        return toDto(saved);
    }

    public void logDataChanges(UUID auditEventId, List<DataChangeRequest> changes) {
        for (DataChangeRequest change : changes) {
            DataChangeLog log = new DataChangeLog(auditEventId, change.resourceType(), change.resourceId(),
                    change.fieldName(), change.oldValue(), change.newValue(), change.changeType());
            dataChangeLogRepository.save(log);
        }
    }

    @Transactional(readOnly = true)
    public AuditEventDto getEvent(UUID id) {
        return auditEventRepository.findById(id).map(this::toDto)
                .orElseThrow(() -> new AuditEventNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Page<AuditEventSummaryDto> getPatientAuditLog(UUID patientId, Pageable pageable) {
        return auditEventRepository.findByPatientIdOrderByTimestampDesc(patientId, pageable).map(this::toSummaryDto);
    }

    @Transactional(readOnly = true)
    public Page<AuditEventSummaryDto> getUserAuditLog(String userId, Pageable pageable) {
        return auditEventRepository.findByUserIdOrderByTimestampDesc(userId, pageable).map(this::toSummaryDto);
    }

    @Transactional(readOnly = true)
    public Page<AuditEventSummaryDto> getCareUnitAuditLog(UUID careUnitId, Pageable pageable) {
        return auditEventRepository.findByCareUnitIdOrderByTimestampDesc(careUnitId, pageable).map(this::toSummaryDto);
    }

    @Transactional(readOnly = true)
    public List<AuditEventSummaryDto> getResourceHistory(ResourceType resourceType, UUID resourceId) {
        return auditEventRepository.findByResourceTypeAndResourceIdOrderByTimestampDesc(resourceType, resourceId)
                .stream().map(this::toSummaryDto).toList();
    }

    @Transactional(readOnly = true)
    public List<AuditEventSummaryDto> getEncounterAuditLog(UUID encounterId) {
        return auditEventRepository.findByEncounterIdOrderByTimestampDesc(encounterId)
                .stream().map(this::toSummaryDto).toList();
    }

    @Transactional(readOnly = true)
    public Page<AuditEventSummaryDto> getEmergencyAccessEvents(Instant from, Instant to, Pageable pageable) {
        return auditEventRepository.findByEmergencyAccessTrueAndTimestampBetweenOrderByTimestampDesc(from, to, pageable)
                .map(this::toSummaryDto);
    }

    @Transactional(readOnly = true)
    public Page<AuditEventSummaryDto> getFailedAccessAttempts(Instant from, Instant to, Pageable pageable) {
        return auditEventRepository.findBySuccessFalseAndTimestampBetweenOrderByTimestampDesc(from, to, pageable)
                .map(this::toSummaryDto);
    }

    @Transactional(readOnly = true)
    public Page<AuditEventSummaryDto> search(AuditSearchRequest request, Pageable pageable) {
        return auditEventRepository.search(request.patientId(), request.userId(), request.careUnitId(),
                request.resourceType(), request.action(), request.from(), request.to(), pageable).map(this::toSummaryDto);
    }

    @Transactional(readOnly = true)
    public List<DataChangeLogDto> getDataChanges(UUID auditEventId) {
        return dataChangeLogRepository.findByAuditEventIdOrderByTimestampDesc(auditEventId)
                .stream().map(this::toDataChangeDto).toList();
    }

    @Transactional(readOnly = true)
    public List<DataChangeLogDto> getResourceChangeHistory(ResourceType resourceType, UUID resourceId) {
        return dataChangeLogRepository.findByResourceTypeAndResourceIdOrderByTimestampDesc(resourceType, resourceId)
                .stream().map(this::toDataChangeDto).toList();
    }

    @Transactional(readOnly = true)
    public AuditStatisticsDto getStatistics(Instant from, Instant to) {
        if (from == null) from = Instant.now().minus(30, ChronoUnit.DAYS);
        if (to == null) to = Instant.now();
        long totalEvents = auditEventRepository.countByTimestampBetween(from, to);
        long emergencyAccess = auditEventRepository.countByEmergencyAccessTrueAndTimestampBetween(from, to);
        long failedAttempts = auditEventRepository.countBySuccessFalseAndTimestampBetween(from, to);
        return new AuditStatisticsDto(from, to, totalEvents, emergencyAccess, failedAttempts);
    }

    private String hashPersonnummer(String personnummer) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(personnummer.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private AuditEventDto toDto(AuditEvent event) {
        return new AuditEventDto(event.getId(), event.getTimestamp(), event.getUserId(), event.getUserHsaId(),
                event.getUserName(), event.getUserRole(), event.getAction(), event.getResourceType(),
                event.getResourceId(), event.getResourceDescription(), event.getPatientId(), event.getCareUnitId(),
                event.getCareUnitName(), event.getCareUnitHsaId(), event.getIpAddress(), event.getEncounterId(),
                event.getAccessReason(), event.isEmergencyAccess(), event.isSuccess(), event.getErrorMessage(),
                event.getSourceSystem(), event.getCorrelationId());
    }

    private AuditEventSummaryDto toSummaryDto(AuditEvent event) {
        return new AuditEventSummaryDto(event.getId(), event.getTimestamp(),
                event.getUserName() != null ? event.getUserName() : event.getUserId(), event.getUserRole(),
                event.getAction(), event.getResourceType(), event.getResourceId(), event.getPatientId(),
                event.isEmergencyAccess(), event.isSuccess());
    }

    private DataChangeLogDto toDataChangeDto(DataChangeLog log) {
        return new DataChangeLogDto(log.getId(), log.getAuditEventId(), log.getTimestamp(), log.getResourceType(),
                log.getResourceId(), log.getFieldName(), log.getOldValue(), log.getNewValue(), log.getChangeType());
    }
}
