package se.curanexus.audit.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.curanexus.audit.domain.AuditEvent;
import se.curanexus.audit.domain.AuditEventType;
import se.curanexus.audit.domain.ResourceType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, UUID> {

    Page<AuditEvent> findByUserId(UUID userId, Pageable pageable);

    Page<AuditEvent> findByPatientId(UUID patientId, Pageable pageable);

    Page<AuditEvent> findByResourceTypeAndResourceId(ResourceType resourceType, UUID resourceId, Pageable pageable);

    Page<AuditEvent> findByEventType(AuditEventType eventType, Pageable pageable);

    Page<AuditEvent> findByTimestampBetween(Instant from, Instant to, Pageable pageable);

    @Query("SELECT ae FROM AuditEvent ae WHERE " +
           "(:userId IS NULL OR ae.userId = :userId) AND " +
           "(:resourceType IS NULL OR ae.resourceType = :resourceType) AND " +
           "(:resourceId IS NULL OR ae.resourceId = :resourceId) AND " +
           "(:eventType IS NULL OR ae.eventType = :eventType) AND " +
           "(:fromDate IS NULL OR ae.timestamp >= :fromDate) AND " +
           "(:toDate IS NULL OR ae.timestamp <= :toDate)")
    Page<AuditEvent> searchEvents(
            @Param("userId") UUID userId,
            @Param("resourceType") ResourceType resourceType,
            @Param("resourceId") UUID resourceId,
            @Param("eventType") AuditEventType eventType,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate,
            Pageable pageable);

    @Query("SELECT COUNT(ae) FROM AuditEvent ae WHERE ae.userId = :userId AND ae.timestamp >= :fromDate AND ae.timestamp <= :toDate")
    long countByUserIdAndTimestampBetween(@Param("userId") UUID userId, @Param("fromDate") Instant fromDate, @Param("toDate") Instant toDate);

    @Query("SELECT ae.eventType, COUNT(ae) FROM AuditEvent ae WHERE ae.userId = :userId AND ae.timestamp >= :fromDate AND ae.timestamp <= :toDate GROUP BY ae.eventType")
    List<Object[]> countByUserIdGroupByEventType(@Param("userId") UUID userId, @Param("fromDate") Instant fromDate, @Param("toDate") Instant toDate);

    @Query("SELECT COUNT(DISTINCT ae.patientId) FROM AuditEvent ae WHERE ae.userId = :userId AND ae.timestamp >= :fromDate AND ae.timestamp <= :toDate AND ae.patientId IS NOT NULL")
    long countDistinctPatientsAccessedByUser(@Param("userId") UUID userId, @Param("fromDate") Instant fromDate, @Param("toDate") Instant toDate);

    @Query("SELECT ae.eventType, COUNT(ae) FROM AuditEvent ae WHERE ae.timestamp >= :fromDate AND ae.timestamp <= :toDate GROUP BY ae.eventType")
    List<Object[]> countByEventTypeInPeriod(@Param("fromDate") Instant fromDate, @Param("toDate") Instant toDate);

    @Query("SELECT COUNT(ae) FROM AuditEvent ae WHERE ae.timestamp >= :fromDate AND ae.timestamp <= :toDate")
    long countInPeriod(@Param("fromDate") Instant fromDate, @Param("toDate") Instant toDate);

    @Query("SELECT COUNT(DISTINCT ae.userId) FROM AuditEvent ae WHERE ae.timestamp >= :fromDate AND ae.timestamp <= :toDate")
    long countDistinctUsersInPeriod(@Param("fromDate") Instant fromDate, @Param("toDate") Instant toDate);

    @Query("SELECT COUNT(DISTINCT ae.patientId) FROM AuditEvent ae WHERE ae.timestamp >= :fromDate AND ae.timestamp <= :toDate AND ae.patientId IS NOT NULL")
    long countDistinctPatientsInPeriod(@Param("fromDate") Instant fromDate, @Param("toDate") Instant toDate);
}
