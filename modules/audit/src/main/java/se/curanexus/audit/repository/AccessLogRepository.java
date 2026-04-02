package se.curanexus.audit.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.curanexus.audit.domain.AccessLog;
import se.curanexus.audit.domain.ResourceType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface AccessLogRepository extends JpaRepository<AccessLog, UUID> {

    Page<AccessLog> findByUserId(UUID userId, Pageable pageable);

    Page<AccessLog> findByPatientId(UUID patientId, Pageable pageable);

    List<AccessLog> findByPatientIdAndTimestampBetween(UUID patientId, Instant from, Instant to);

    Page<AccessLog> findByResourceTypeAndResourceId(ResourceType resourceType, UUID resourceId, Pageable pageable);

    @Query("SELECT al FROM AccessLog al WHERE " +
           "(:patientId IS NULL OR al.patientId = :patientId) AND " +
           "(:userId IS NULL OR al.userId = :userId) AND " +
           "(:fromDate IS NULL OR al.timestamp >= :fromDate) AND " +
           "(:toDate IS NULL OR al.timestamp <= :toDate)")
    Page<AccessLog> searchAccessLogs(
            @Param("patientId") UUID patientId,
            @Param("userId") UUID userId,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate,
            Pageable pageable);

    @Query("SELECT COUNT(al) FROM AccessLog al WHERE al.patientId = :patientId AND al.timestamp >= :fromDate AND al.timestamp <= :toDate")
    long countByPatientIdAndTimestampBetween(@Param("patientId") UUID patientId, @Param("fromDate") Instant fromDate, @Param("toDate") Instant toDate);

    @Query("SELECT COUNT(DISTINCT al.userId) FROM AccessLog al WHERE al.patientId = :patientId AND al.timestamp >= :fromDate AND al.timestamp <= :toDate")
    long countDistinctUsersByPatientId(@Param("patientId") UUID patientId, @Param("fromDate") Instant fromDate, @Param("toDate") Instant toDate);

    @Query("SELECT al.userId, al.username, COUNT(al), al.careRelationType, MAX(al.timestamp) " +
           "FROM AccessLog al WHERE al.patientId = :patientId AND al.timestamp >= :fromDate AND al.timestamp <= :toDate " +
           "GROUP BY al.userId, al.username, al.careRelationType")
    List<Object[]> getAccessStatsByPatient(@Param("patientId") UUID patientId, @Param("fromDate") Instant fromDate, @Param("toDate") Instant toDate);

    @Query("SELECT al.resourceType, COUNT(al) FROM AccessLog al WHERE al.patientId = :patientId AND al.timestamp >= :fromDate AND al.timestamp <= :toDate GROUP BY al.resourceType")
    List<Object[]> countByPatientIdGroupByResourceType(@Param("patientId") UUID patientId, @Param("fromDate") Instant fromDate, @Param("toDate") Instant toDate);
}
