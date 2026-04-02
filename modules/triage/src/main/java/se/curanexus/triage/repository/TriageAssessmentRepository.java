package se.curanexus.triage.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.curanexus.triage.domain.AssessmentStatus;
import se.curanexus.triage.domain.TriageAssessment;
import se.curanexus.triage.domain.TriagePriority;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TriageAssessmentRepository extends JpaRepository<TriageAssessment, UUID> {

    Optional<TriageAssessment> findByEncounterId(UUID encounterId);

    Page<TriageAssessment> findByPatientId(UUID patientId, Pageable pageable);

    Page<TriageAssessment> findByPriority(TriagePriority priority, Pageable pageable);

    Page<TriageAssessment> findByStatus(AssessmentStatus status, Pageable pageable);

    @Query("SELECT ta FROM TriageAssessment ta WHERE " +
           "(:patientId IS NULL OR ta.patientId = :patientId) AND " +
           "(:encounterId IS NULL OR ta.encounterId = :encounterId) AND " +
           "(:priority IS NULL OR ta.priority = :priority) AND " +
           "(:status IS NULL OR ta.status = :status) AND " +
           "(:fromDate IS NULL OR ta.arrivalTime >= :fromDate) AND " +
           "(:toDate IS NULL OR ta.arrivalTime <= :toDate)")
    Page<TriageAssessment> searchAssessments(
            @Param("patientId") UUID patientId,
            @Param("encounterId") UUID encounterId,
            @Param("priority") TriagePriority priority,
            @Param("status") AssessmentStatus status,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate,
            Pageable pageable);

    // Queue queries
    @Query("SELECT ta FROM TriageAssessment ta WHERE ta.status = se.curanexus.triage.domain.AssessmentStatus.IN_PROGRESS " +
           "AND (:locationId IS NULL OR ta.locationId = :locationId) " +
           "ORDER BY ta.arrivalTime ASC")
    List<TriageAssessment> findActiveByLocationOrderByPriority(@Param("locationId") UUID locationId);

    @Query("SELECT COUNT(ta) FROM TriageAssessment ta WHERE ta.status = se.curanexus.triage.domain.AssessmentStatus.IN_PROGRESS " +
           "AND (:locationId IS NULL OR ta.locationId = :locationId)")
    long countWaitingByLocation(@Param("locationId") UUID locationId);

    @Query("SELECT ta.priority, COUNT(ta) FROM TriageAssessment ta " +
           "WHERE ta.status = se.curanexus.triage.domain.AssessmentStatus.IN_PROGRESS " +
           "AND (:locationId IS NULL OR ta.locationId = :locationId) " +
           "GROUP BY ta.priority")
    List<Object[]> countWaitingByLocationGroupByPriority(@Param("locationId") UUID locationId);

    /**
     * Get all in-progress assessments for a location to calculate average wait time in service layer.
     * This avoids PostgreSQL-specific EXTRACT(EPOCH...) syntax.
     */
    @Query("SELECT ta FROM TriageAssessment ta WHERE ta.status = se.curanexus.triage.domain.AssessmentStatus.IN_PROGRESS " +
           "AND (:locationId IS NULL OR ta.locationId = :locationId)")
    List<TriageAssessment> findInProgressByLocation(@Param("locationId") UUID locationId);

    // Statistics
    @Query("SELECT COUNT(ta) FROM TriageAssessment ta WHERE ta.triageNurseId = :nurseId " +
           "AND ta.createdAt >= :fromDate AND ta.createdAt <= :toDate")
    long countByNurseInPeriod(@Param("nurseId") UUID nurseId, @Param("fromDate") Instant fromDate, @Param("toDate") Instant toDate);

    @Query("SELECT ta.priority, COUNT(ta) FROM TriageAssessment ta " +
           "WHERE ta.createdAt >= :fromDate AND ta.createdAt <= :toDate " +
           "GROUP BY ta.priority")
    List<Object[]> countByPriorityInPeriod(@Param("fromDate") Instant fromDate, @Param("toDate") Instant toDate);
}
