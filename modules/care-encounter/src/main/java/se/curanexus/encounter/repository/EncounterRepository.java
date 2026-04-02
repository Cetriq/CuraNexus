package se.curanexus.encounter.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.curanexus.encounter.domain.Encounter;
import se.curanexus.encounter.domain.EncounterClass;
import se.curanexus.encounter.domain.EncounterStatus;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface EncounterRepository extends JpaRepository<Encounter, UUID> {

    Page<Encounter> findByPatientId(UUID patientId, Pageable pageable);

    Page<Encounter> findByPatientIdAndStatus(UUID patientId, EncounterStatus status, Pageable pageable);

    @Query("""
        SELECT e FROM Encounter e
        WHERE (:patientId IS NULL OR e.patientId = :patientId)
        AND (:status IS NULL OR e.status = :status)
        AND (:encounterClass IS NULL OR e.encounterClass = :encounterClass)
        AND (:responsibleUnitId IS NULL OR e.responsibleUnitId = :responsibleUnitId)
        AND (:fromDate IS NULL OR e.plannedStartTime >= :fromDate)
        AND (:toDate IS NULL OR e.plannedStartTime <= :toDate)
        ORDER BY e.plannedStartTime DESC
        """)
    Page<Encounter> searchEncounters(
            @Param("patientId") UUID patientId,
            @Param("status") EncounterStatus status,
            @Param("encounterClass") EncounterClass encounterClass,
            @Param("responsibleUnitId") UUID responsibleUnitId,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate,
            Pageable pageable);
}
