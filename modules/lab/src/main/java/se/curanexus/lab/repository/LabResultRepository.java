package se.curanexus.lab.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.curanexus.lab.domain.AbnormalFlag;
import se.curanexus.lab.domain.LabResult;
import se.curanexus.lab.domain.ResultStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface LabResultRepository extends JpaRepository<LabResult, UUID> {

    /** Hitta resultat för patient */
    @Query("SELECT r FROM LabResult r JOIN r.orderItem oi JOIN oi.labOrder o " +
           "WHERE o.patientId = :patientId " +
           "ORDER BY r.resultedAt DESC NULLS LAST")
    List<LabResult> findByPatientId(@Param("patientId") UUID patientId);

    /** Hitta resultat för specifikt test (för trendanalys) */
    @Query("SELECT r FROM LabResult r JOIN r.orderItem oi JOIN oi.labOrder o " +
           "WHERE o.patientId = :patientId AND oi.testCode = :testCode " +
           "AND r.status IN ('FINAL', 'CORRECTED') " +
           "ORDER BY r.resultedAt DESC")
    List<LabResult> findByPatientAndTestCode(
            @Param("patientId") UUID patientId,
            @Param("testCode") String testCode
    );

    /** Hitta kritiska resultat som inte granskats */
    @Query("SELECT r FROM LabResult r WHERE r.isCritical = true " +
           "AND r.status = 'PRELIMINARY' " +
           "ORDER BY r.analyzedAt ASC")
    List<LabResult> findUnreviewedCriticalResults();

    /** Hitta resultat väntande på granskning för lab */
    @Query("SELECT r FROM LabResult r JOIN r.orderItem oi JOIN oi.labOrder o " +
           "WHERE o.performingLabId = :labId AND r.status = 'PRELIMINARY' " +
           "ORDER BY CASE WHEN r.isCritical = true THEN 0 ELSE 1 END, r.analyzedAt ASC")
    List<LabResult> findPendingReviewByLab(@Param("labId") UUID labId);

    /** Hitta resultat med specifik avvikelse */
    @Query("SELECT r FROM LabResult r JOIN r.orderItem oi JOIN oi.labOrder o " +
           "WHERE o.patientId = :patientId AND r.abnormalFlag = :flag " +
           "AND r.status IN ('FINAL', 'CORRECTED') " +
           "ORDER BY r.resultedAt DESC")
    List<LabResult> findByPatientAndAbnormalFlag(
            @Param("patientId") UUID patientId,
            @Param("flag") AbnormalFlag flag
    );

    /** Räkna väntande granskningar för lab */
    @Query("SELECT COUNT(r) FROM LabResult r JOIN r.orderItem oi JOIN oi.labOrder o " +
           "WHERE o.performingLabId = :labId AND r.status = 'PRELIMINARY'")
    long countPendingReviewByLab(@Param("labId") UUID labId);
}
