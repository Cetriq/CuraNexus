package se.curanexus.journal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.curanexus.journal.domain.Observation;
import se.curanexus.journal.domain.ObservationCategory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ObservationRepository extends JpaRepository<Observation, UUID> {

    List<Observation> findByEncounterIdOrderByObservedAtDesc(UUID encounterId);

    List<Observation> findByPatientIdOrderByObservedAtDesc(UUID patientId);

    List<Observation> findByPatientIdAndCategory(UUID patientId, ObservationCategory category);

    List<Observation> findByPatientIdAndCode(UUID patientId, String code);

    List<Observation> findByEncounterIdAndCategory(UUID encounterId, ObservationCategory category);

    @Query("SELECT o FROM Observation o WHERE o.patientId = :patientId AND o.category = 'VITAL_SIGNS' ORDER BY o.observedAt DESC")
    List<Observation> findVitalSignsByPatient(@Param("patientId") UUID patientId);

    @Query("SELECT o FROM Observation o WHERE o.patientId = :patientId AND o.category = 'LABORATORY' ORDER BY o.observedAt DESC")
    List<Observation> findLabResultsByPatient(@Param("patientId") UUID patientId);

    @Query("SELECT o FROM Observation o WHERE o.patientId = :patientId AND o.code = :code ORDER BY o.observedAt DESC")
    List<Observation> findPatientObservationHistory(@Param("patientId") UUID patientId, @Param("code") String code);

    @Query("SELECT o FROM Observation o WHERE o.patientId = :patientId AND o.observedAt BETWEEN :start AND :end ORDER BY o.observedAt")
    List<Observation> findByPatientAndDateRange(
            @Param("patientId") UUID patientId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT o FROM Observation o WHERE o.patientId = :patientId AND o.interpretation IN ('CRITICAL_LOW', 'CRITICAL_HIGH') ORDER BY o.observedAt DESC")
    List<Observation> findCriticalByPatient(@Param("patientId") UUID patientId);

    @Query("SELECT o FROM Observation o WHERE o.encounterId = :encounterId AND o.category = 'VITAL_SIGNS' ORDER BY o.observedAt DESC")
    List<Observation> findEncounterVitalSigns(@Param("encounterId") UUID encounterId);
}
