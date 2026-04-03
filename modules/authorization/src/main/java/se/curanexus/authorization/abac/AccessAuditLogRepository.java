package se.curanexus.authorization.abac;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface AccessAuditLogRepository extends JpaRepository<AccessAuditLog, UUID> {

    /**
     * Find all access logs for a specific user.
     */
    Page<AccessAuditLog> findByUserIdOrderByTimestampDesc(UUID userId, Pageable pageable);

    /**
     * Find all access logs for a specific patient.
     */
    Page<AccessAuditLog> findByPatientIdOrderByTimestampDesc(UUID patientId, Pageable pageable);

    /**
     * Find access logs by user and patient.
     */
    List<AccessAuditLog> findByUserIdAndPatientIdOrderByTimestampDesc(UUID userId, UUID patientId);

    /**
     * Find denied access attempts for a user.
     */
    @Query("SELECT a FROM AccessAuditLog a WHERE a.userId = :userId AND a.granted = false ORDER BY a.timestamp DESC")
    List<AccessAuditLog> findDeniedAccessByUser(@Param("userId") UUID userId);

    /**
     * Find emergency access logs.
     */
    @Query("SELECT a FROM AccessAuditLog a WHERE a.emergencyAccess = true ORDER BY a.timestamp DESC")
    Page<AccessAuditLog> findEmergencyAccess(Pageable pageable);

    /**
     * Find emergency access logs for a specific patient.
     */
    @Query("SELECT a FROM AccessAuditLog a WHERE a.patientId = :patientId AND a.emergencyAccess = true ORDER BY a.timestamp DESC")
    List<AccessAuditLog> findEmergencyAccessByPatient(@Param("patientId") UUID patientId);

    /**
     * Find access logs within a time range.
     */
    @Query("SELECT a FROM AccessAuditLog a WHERE a.timestamp BETWEEN :from AND :to ORDER BY a.timestamp DESC")
    List<AccessAuditLog> findByTimeRange(@Param("from") Instant from, @Param("to") Instant to);

    /**
     * Find access logs by encounter.
     */
    List<AccessAuditLog> findByEncounterIdOrderByTimestampDesc(UUID encounterId);

    /**
     * Count access attempts by user in a time period.
     */
    @Query("SELECT COUNT(a) FROM AccessAuditLog a WHERE a.userId = :userId AND a.timestamp > :since")
    long countByUserSince(@Param("userId") UUID userId, @Param("since") Instant since);

    /**
     * Count denied access attempts by user in a time period.
     */
    @Query("SELECT COUNT(a) FROM AccessAuditLog a WHERE a.userId = :userId AND a.granted = false AND a.timestamp > :since")
    long countDeniedByUserSince(@Param("userId") UUID userId, @Param("since") Instant since);
}
