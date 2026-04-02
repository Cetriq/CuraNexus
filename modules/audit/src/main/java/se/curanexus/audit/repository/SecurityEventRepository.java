package se.curanexus.audit.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.curanexus.audit.domain.SecurityEvent;
import se.curanexus.audit.domain.SecurityEventType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface SecurityEventRepository extends JpaRepository<SecurityEvent, UUID> {

    Page<SecurityEvent> findByUserId(UUID userId, Pageable pageable);

    Page<SecurityEvent> findByEventType(SecurityEventType eventType, Pageable pageable);

    Page<SecurityEvent> findBySuccess(boolean success, Pageable pageable);

    @Query("SELECT se FROM SecurityEvent se WHERE " +
           "(:userId IS NULL OR se.userId = :userId) AND " +
           "(:eventType IS NULL OR se.eventType = :eventType) AND " +
           "(:success IS NULL OR se.success = :success) AND " +
           "(:fromDate IS NULL OR se.timestamp >= :fromDate) AND " +
           "(:toDate IS NULL OR se.timestamp <= :toDate)")
    Page<SecurityEvent> searchSecurityEvents(
            @Param("userId") UUID userId,
            @Param("eventType") SecurityEventType eventType,
            @Param("success") Boolean success,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate,
            Pageable pageable);

    @Query("SELECT COUNT(se) FROM SecurityEvent se WHERE se.userId = :userId AND se.eventType = :eventType AND se.timestamp >= :fromDate AND se.timestamp <= :toDate")
    long countByUserIdAndEventType(@Param("userId") UUID userId, @Param("eventType") SecurityEventType eventType, @Param("fromDate") Instant fromDate, @Param("toDate") Instant toDate);

    @Query("SELECT COUNT(se) FROM SecurityEvent se WHERE se.eventType = :eventType AND se.timestamp >= :fromDate AND se.timestamp <= :toDate")
    long countByEventTypeInPeriod(@Param("eventType") SecurityEventType eventType, @Param("fromDate") Instant fromDate, @Param("toDate") Instant toDate);

    @Query("SELECT COUNT(se) FROM SecurityEvent se WHERE se.eventType = :eventType AND se.success = false AND se.timestamp >= :fromDate AND se.timestamp <= :toDate")
    long countFailedByEventTypeInPeriod(@Param("eventType") SecurityEventType eventType, @Param("fromDate") Instant fromDate, @Param("toDate") Instant toDate);

    List<SecurityEvent> findByUserIdAndEventTypeAndTimestampAfter(UUID userId, SecurityEventType eventType, Instant after);
}
