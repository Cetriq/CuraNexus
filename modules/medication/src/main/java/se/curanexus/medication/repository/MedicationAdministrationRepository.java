package se.curanexus.medication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.curanexus.medication.domain.AdministrationStatus;
import se.curanexus.medication.domain.MedicationAdministration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface MedicationAdministrationRepository extends JpaRepository<MedicationAdministration, UUID> {

    List<MedicationAdministration> findByPatientIdOrderByAdministeredAtDesc(UUID patientId);

    List<MedicationAdministration> findByPrescription_IdOrderByAdministeredAtDesc(UUID prescriptionId);

    List<MedicationAdministration> findByEncounterId(UUID encounterId);

    @Query("SELECT ma FROM MedicationAdministration ma " +
            "WHERE ma.patientId = :patientId " +
            "AND ma.administeredAt BETWEEN :from AND :to " +
            "ORDER BY ma.administeredAt DESC")
    List<MedicationAdministration> findByPatientAndDateRange(@Param("patientId") UUID patientId,
                                                              @Param("from") LocalDateTime from,
                                                              @Param("to") LocalDateTime to);

    @Query("SELECT ma FROM MedicationAdministration ma " +
            "WHERE ma.status = 'PLANNED' " +
            "AND ma.scheduledAt BETWEEN :from AND :to " +
            "ORDER BY ma.scheduledAt")
    List<MedicationAdministration> findScheduledBetween(@Param("from") LocalDateTime from,
                                                         @Param("to") LocalDateTime to);

    @Query("SELECT ma FROM MedicationAdministration ma " +
            "WHERE ma.patientId = :patientId " +
            "AND ma.status = 'PLANNED' " +
            "AND ma.scheduledAt <= :until " +
            "ORDER BY ma.scheduledAt")
    List<MedicationAdministration> findPendingByPatient(@Param("patientId") UUID patientId,
                                                         @Param("until") LocalDateTime until);

    @Query("SELECT ma FROM MedicationAdministration ma " +
            "WHERE ma.status = 'PLANNED' " +
            "AND ma.scheduledAt < :threshold")
    List<MedicationAdministration> findOverdueAdministrations(@Param("threshold") LocalDateTime threshold);

    List<MedicationAdministration> findByPerformerIdOrderByAdministeredAtDesc(UUID performerId);

    @Query("SELECT COUNT(ma) FROM MedicationAdministration ma " +
            "WHERE ma.prescription.id = :prescriptionId " +
            "AND ma.status = 'COMPLETED'")
    long countCompletedByPrescription(@Param("prescriptionId") UUID prescriptionId);

    List<MedicationAdministration> findByStatusIn(List<AdministrationStatus> statuses);
}
