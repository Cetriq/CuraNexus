package se.curanexus.journal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.curanexus.journal.domain.Procedure;
import se.curanexus.journal.domain.ProcedureStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProcedureRepository extends JpaRepository<Procedure, UUID> {

    List<Procedure> findByEncounterIdOrderByCreatedAtDesc(UUID encounterId);

    List<Procedure> findByPatientIdOrderByCreatedAtDesc(UUID patientId);

    List<Procedure> findByEncounterIdAndStatus(UUID encounterId, ProcedureStatus status);

    List<Procedure> findByPatientIdAndCode(UUID patientId, String code);

    List<Procedure> findByPerformedByIdOrderByPerformedAtDesc(UUID performedById);

    @Query("SELECT p FROM Procedure p WHERE p.patientId = :patientId AND p.status = 'COMPLETED' ORDER BY p.performedAt DESC")
    List<Procedure> findCompletedByPatient(@Param("patientId") UUID patientId);

    @Query("SELECT p FROM Procedure p WHERE p.encounterId = :encounterId AND p.status IN ('PLANNED', 'IN_PROGRESS')")
    List<Procedure> findPendingByEncounter(@Param("encounterId") UUID encounterId);

    @Query("SELECT p FROM Procedure p WHERE p.patientId = :patientId AND p.performedAt BETWEEN :start AND :end ORDER BY p.performedAt")
    List<Procedure> findByPatientAndDateRange(
            @Param("patientId") UUID patientId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT p FROM Procedure p WHERE p.patientId = :patientId AND p.code LIKE :codePrefix% ORDER BY p.createdAt DESC")
    List<Procedure> findByPatientAndCodePrefix(@Param("patientId") UUID patientId, @Param("codePrefix") String codePrefix);
}
