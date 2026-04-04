package se.curanexus.encounter.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.curanexus.encounter.domain.Encounter;
import se.curanexus.encounter.domain.EncounterStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EncounterRepository extends JpaRepository<Encounter, UUID>, EncounterRepositoryCustom {

    Page<Encounter> findByPatientId(UUID patientId, Pageable pageable);

    Page<Encounter> findByPatientIdAndStatus(UUID patientId, EncounterStatus status, Pageable pageable);

    List<Encounter> findByStatusIn(List<EncounterStatus> statuses);

    @Query("SELECT DISTINCT e FROM Encounter e LEFT JOIN FETCH e.reasons WHERE e.id = :id")
    Optional<Encounter> findByIdWithReasons(@Param("id") UUID id);

    @Query("SELECT DISTINCT e FROM Encounter e LEFT JOIN FETCH e.reasons")
    List<Encounter> findAllWithReasons();

    @Query("SELECT DISTINCT e FROM Encounter e LEFT JOIN FETCH e.reasons WHERE e.patientId = :patientId")
    List<Encounter> findByPatientIdWithReasons(@Param("patientId") UUID patientId);

    @Query("SELECT DISTINCT e FROM Encounter e LEFT JOIN FETCH e.reasons WHERE e.status IN :statuses")
    List<Encounter> findByStatusInWithReasons(@Param("statuses") List<EncounterStatus> statuses);
}
