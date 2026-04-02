package se.curanexus.audit.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.curanexus.audit.domain.ChangeLog;
import se.curanexus.audit.domain.ChangeType;
import se.curanexus.audit.domain.ResourceType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface ChangeLogRepository extends JpaRepository<ChangeLog, UUID> {

    Page<ChangeLog> findByUserId(UUID userId, Pageable pageable);

    List<ChangeLog> findByResourceTypeAndResourceIdOrderByTimestampDesc(ResourceType resourceType, UUID resourceId);

    Page<ChangeLog> findByResourceTypeAndResourceId(ResourceType resourceType, UUID resourceId, Pageable pageable);

    Page<ChangeLog> findByChangeType(ChangeType changeType, Pageable pageable);

    @Query("SELECT cl FROM ChangeLog cl WHERE " +
           "(:resourceType IS NULL OR cl.resourceType = :resourceType) AND " +
           "(:resourceId IS NULL OR cl.resourceId = :resourceId) AND " +
           "(:userId IS NULL OR cl.userId = :userId) AND " +
           "(:changeType IS NULL OR cl.changeType = :changeType) AND " +
           "(:fromDate IS NULL OR cl.timestamp >= :fromDate) AND " +
           "(:toDate IS NULL OR cl.timestamp <= :toDate)")
    Page<ChangeLog> searchChangeLogs(
            @Param("resourceType") ResourceType resourceType,
            @Param("resourceId") UUID resourceId,
            @Param("userId") UUID userId,
            @Param("changeType") ChangeType changeType,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate,
            Pageable pageable);

    @Query("SELECT COUNT(cl) FROM ChangeLog cl WHERE cl.userId = :userId AND cl.timestamp >= :fromDate AND cl.timestamp <= :toDate")
    long countByUserIdAndTimestampBetween(@Param("userId") UUID userId, @Param("fromDate") Instant fromDate, @Param("toDate") Instant toDate);
}
