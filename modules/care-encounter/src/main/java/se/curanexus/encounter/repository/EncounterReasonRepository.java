package se.curanexus.encounter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.curanexus.encounter.domain.EncounterReason;

import java.util.List;
import java.util.UUID;

@Repository
public interface EncounterReasonRepository extends JpaRepository<EncounterReason, UUID> {

    List<EncounterReason> findByEncounterId(UUID encounterId);
}
