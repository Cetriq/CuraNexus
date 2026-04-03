package se.curanexus.lab.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.curanexus.lab.domain.LabOrder;
import se.curanexus.lab.domain.LabOrderPriority;
import se.curanexus.lab.domain.LabOrderStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LabOrderRepository extends JpaRepository<LabOrder, UUID> {

    Optional<LabOrder> findByOrderReference(String orderReference);

    List<LabOrder> findByPatientIdOrderByCreatedAtDesc(UUID patientId);

    List<LabOrder> findByOrderingUnitIdOrderByCreatedAtDesc(UUID orderingUnitId);

    List<LabOrder> findByPerformingLabIdOrderByCreatedAtDesc(UUID performingLabId);

    List<LabOrder> findByStatus(LabOrderStatus status);

    List<LabOrder> findByEncounterId(UUID encounterId);

    /** Hitta beställningar väntande på provtagning */
    @Query("SELECT o FROM LabOrder o WHERE o.status IN ('ORDERED', 'RECEIVED') " +
           "ORDER BY CASE o.priority WHEN 'STAT' THEN 1 WHEN 'URGENT' THEN 2 " +
           "WHEN 'ROUTINE' THEN 3 ELSE 4 END, o.orderedAt ASC")
    List<LabOrder> findPendingSpecimenCollection();

    /** Hitta beställningar väntande på provtagning för enhet */
    @Query("SELECT o FROM LabOrder o WHERE o.orderingUnitId = :unitId " +
           "AND o.status IN ('ORDERED', 'RECEIVED') " +
           "ORDER BY CASE o.priority WHEN 'STAT' THEN 1 WHEN 'URGENT' THEN 2 " +
           "WHEN 'ROUTINE' THEN 3 ELSE 4 END, o.orderedAt ASC")
    List<LabOrder> findPendingSpecimenCollectionByUnit(@Param("unitId") UUID unitId);

    /** Hitta beställningar under analys för lab */
    @Query("SELECT o FROM LabOrder o WHERE o.performingLabId = :labId " +
           "AND o.status IN ('SPECIMEN_COLLECTED', 'IN_PROGRESS', 'PARTIAL_RESULTS') " +
           "ORDER BY CASE o.priority WHEN 'STAT' THEN 1 WHEN 'URGENT' THEN 2 " +
           "WHEN 'ROUTINE' THEN 3 ELSE 4 END, o.specimenCollectedAt ASC")
    List<LabOrder> findInProgressByLab(@Param("labId") UUID labId);

    /** Hitta beställningar med kritiska resultat */
    @Query("SELECT DISTINCT o FROM LabOrder o JOIN o.orderItems oi JOIN oi.result r " +
           "WHERE r.isCritical = true AND r.status = 'FINAL' " +
           "ORDER BY r.resultedAt DESC")
    List<LabOrder> findWithCriticalResults();

    /** Hitta beställningar med kritiska resultat för enhet */
    @Query("SELECT DISTINCT o FROM LabOrder o JOIN o.orderItems oi JOIN oi.result r " +
           "WHERE o.orderingUnitId = :unitId AND r.isCritical = true AND r.status = 'FINAL' " +
           "ORDER BY r.resultedAt DESC")
    List<LabOrder> findWithCriticalResultsByUnit(@Param("unitId") UUID unitId);

    /** Hitta slutförda beställningar med nya resultat (ej sedda) */
    @Query("SELECT o FROM LabOrder o WHERE o.status = 'COMPLETED' " +
           "AND o.orderingUnitId = :unitId " +
           "ORDER BY o.completedAt DESC")
    List<LabOrder> findCompletedByUnit(@Param("unitId") UUID unitId);

    /** Sök beställningar */
    @Query("SELECT o FROM LabOrder o WHERE " +
           "(:patientId IS NULL OR o.patientId = :patientId) AND " +
           "(:orderingUnitId IS NULL OR o.orderingUnitId = :orderingUnitId) AND " +
           "(:performingLabId IS NULL OR o.performingLabId = :performingLabId) AND " +
           "(:status IS NULL OR o.status = :status) AND " +
           "(:priority IS NULL OR o.priority = :priority) AND " +
           "(:fromDate IS NULL OR o.createdAt >= :fromDate) AND " +
           "(:toDate IS NULL OR o.createdAt <= :toDate)")
    Page<LabOrder> search(
            @Param("patientId") UUID patientId,
            @Param("orderingUnitId") UUID orderingUnitId,
            @Param("performingLabId") UUID performingLabId,
            @Param("status") LabOrderStatus status,
            @Param("priority") LabOrderPriority priority,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate,
            Pageable pageable
    );

    /** Räkna väntande beställningar för lab */
    @Query("SELECT COUNT(o) FROM LabOrder o WHERE o.performingLabId = :labId " +
           "AND o.status IN ('RECEIVED', 'SPECIMEN_COLLECTED')")
    long countPendingByLab(@Param("labId") UUID labId);

    /** Räkna beställningar med resultat för enhet (idag) */
    @Query("SELECT COUNT(o) FROM LabOrder o WHERE o.orderingUnitId = :unitId " +
           "AND o.status = 'COMPLETED' AND o.completedAt >= :since")
    long countCompletedTodayByUnit(@Param("unitId") UUID unitId, @Param("since") Instant since);
}
