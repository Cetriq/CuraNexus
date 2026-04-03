package se.curanexus.medication.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.curanexus.medication.domain.Prescription;
import se.curanexus.medication.domain.PrescriptionStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, UUID> {

    List<Prescription> findByPatientIdOrderByCreatedAtDesc(UUID patientId);

    List<Prescription> findByPatientIdAndStatusIn(UUID patientId, List<PrescriptionStatus> statuses);

    @Query("SELECT p FROM Prescription p WHERE p.patientId = :patientId " +
            "AND p.status = 'ACTIVE' " +
            "AND (p.startDate IS NULL OR p.startDate <= :date) " +
            "AND (p.endDate IS NULL OR p.endDate >= :date)")
    List<Prescription> findActiveByPatientIdOnDate(@Param("patientId") UUID patientId,
                                                    @Param("date") LocalDate date);

    List<Prescription> findByEncounterId(UUID encounterId);

    List<Prescription> findByPrescriberIdOrderByCreatedAtDesc(UUID prescriberId);

    @Query("SELECT p FROM Prescription p WHERE p.prescriberId = :prescriberId " +
            "AND p.createdAt >= :from")
    List<Prescription> findRecentByPrescriber(@Param("prescriberId") UUID prescriberId,
                                               @Param("from") java.time.Instant from);

    @Query("SELECT p FROM Prescription p WHERE p.patientId = :patientId " +
            "AND p.status IN :statuses " +
            "AND p.medication.atcCode LIKE CONCAT(:atcPrefix, '%')")
    List<Prescription> findByPatientAndAtcPrefix(@Param("patientId") UUID patientId,
                                                  @Param("statuses") List<PrescriptionStatus> statuses,
                                                  @Param("atcPrefix") String atcPrefix);

    @Query("SELECT p FROM Prescription p WHERE p.status = 'ACTIVE' " +
            "AND p.endDate IS NOT NULL " +
            "AND p.endDate < :date")
    List<Prescription> findExpiredPrescriptions(@Param("date") LocalDate date);

    @Query("SELECT COUNT(p) FROM Prescription p WHERE p.patientId = :patientId AND p.status = 'ACTIVE'")
    long countActiveByPatient(@Param("patientId") UUID patientId);

    @Query("SELECT p FROM Prescription p " +
            "WHERE (:patientId IS NULL OR p.patientId = :patientId) " +
            "AND (:prescriberId IS NULL OR p.prescriberId = :prescriberId) " +
            "AND (:status IS NULL OR p.status = :status) " +
            "AND (:atcCode IS NULL OR p.atcCode = :atcCode OR p.medication.atcCode = :atcCode)")
    Page<Prescription> search(@Param("patientId") UUID patientId,
                               @Param("prescriberId") UUID prescriberId,
                               @Param("status") PrescriptionStatus status,
                               @Param("atcCode") String atcCode,
                               Pageable pageable);
}
