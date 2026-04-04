package se.curanexus.audit.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.curanexus.audit.domain.AuditAction;
import se.curanexus.audit.domain.AuditEvent;
import se.curanexus.audit.domain.ResourceType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, UUID> {

    // Find by patient
    Page<AuditEvent> findByPatientIdOrderByTimestampDesc(UUID patientId, Pageable pageable);

    List<AuditEvent> findByPatientIdAndTimestampBetweenOrderByTimestampDesc(
            UUID patientId, Instant from, Instant to);

    // Find by user
    Page<AuditEvent> findByUserIdOrderByTimestampDesc(String userId, Pageable pageable);

    List<AuditEvent> findByUserIdAndTimestampBetweenOrderByTimestampDesc(
            String userId, Instant from, Instant to);

    // Find by resource
    List<AuditEvent> findByResourceTypeAndResourceIdOrderByTimestampDesc(
            ResourceType resourceType, UUID resourceId);

    // Find by care unit
    Page<AuditEvent> findByCareUnitIdOrderByTimestampDesc(UUID careUnitId, Pageable pageable);

    // Find by action
    Page<AuditEvent> findByActionOrderByTimestampDesc(AuditAction action, Pageable pageable);

    // Find emergency access events
    List<AuditEvent> findByEmergencyAccessTrueOrderByTimestampDesc();

    Page<AuditEvent> findByEmergencyAccessTrueAndTimestampBetweenOrderByTimestampDesc(
            Instant from, Instant to, Pageable pageable);

    // Find failed access attempts
    List<AuditEvent> findBySuccessFalseOrderByTimestampDesc();

    Page<AuditEvent> findBySuccessFalseAndTimestampBetweenOrderByTimestampDesc(
            Instant from, Instant to, Pageable pageable);

    // Find by encounter
    List<AuditEvent> findByEncounterIdOrderByTimestampDesc(UUID encounterId);

    // Search with multiple criteria
    @Query("""
        SELECT ae FROM AuditEvent ae
        WHERE (:patientId IS NULL OR ae.patientId = :patientId)
        AND (:userId IS NULL OR ae.userId = :userId)
        AND (:careUnitId IS NULL OR ae.careUnitId = :careUnitId)
        AND (:resourceType IS NULL OR ae.resourceType = :resourceType)
        AND (:action IS NULL OR ae.action = :action)
        AND (:from IS NULL OR ae.timestamp >= :from)
        AND (:to IS NULL OR ae.timestamp <= :to)
        ORDER BY ae.timestamp DESC
        """)
    Page<AuditEvent> search(
            @Param("patientId") UUID patientId,
            @Param("userId") String userId,
            @Param("careUnitId") UUID careUnitId,
            @Param("resourceType") ResourceType resourceType,
            @Param("action") AuditAction action,
            @Param("from") Instant from,
            @Param("to") Instant to,
            Pageable pageable);

    // Statistics
    @Query("""
        SELECT ae.action, COUNT(ae) FROM AuditEvent ae
        WHERE ae.timestamp >= :from AND ae.timestamp <= :to
        GROUP BY ae.action
        """)
    List<Object[]> countByActionInPeriod(@Param("from") Instant from, @Param("to") Instant to);

    @Query("""
        SELECT ae.resourceType, COUNT(ae) FROM AuditEvent ae
        WHERE ae.timestamp >= :from AND ae.timestamp <= :to
        GROUP BY ae.resourceType
        """)
    List<Object[]> countByResourceTypeInPeriod(@Param("from") Instant from, @Param("to") Instant to);

    @Query("""
        SELECT ae.userId, COUNT(ae) FROM AuditEvent ae
        WHERE ae.timestamp >= :from AND ae.timestamp <= :to
        GROUP BY ae.userId
        ORDER BY COUNT(ae) DESC
        """)
    List<Object[]> countByUserInPeriod(@Param("from") Instant from, @Param("to") Instant to, Pageable pageable);

    // Count for reporting
    long countByTimestampBetween(Instant from, Instant to);
    
    long countByEmergencyAccessTrueAndTimestampBetween(Instant from, Instant to);
    
    long countBySuccessFalseAndTimestampBetween(Instant from, Instant to);
}
