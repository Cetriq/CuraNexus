package se.curanexus.encounter.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.curanexus.encounter.domain.Encounter;
import se.curanexus.encounter.domain.EncounterStatus;

import java.util.List;
import java.util.UUID;

@Repository
public interface EncounterRepository extends JpaRepository<Encounter, UUID>, EncounterRepositoryCustom {

    Page<Encounter> findByPatientId(UUID patientId, Pageable pageable);

    Page<Encounter> findByPatientIdAndStatus(UUID patientId, EncounterStatus status, Pageable pageable);

    List<Encounter> findByStatusIn(List<EncounterStatus> statuses);
}
