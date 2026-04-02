package se.curanexus.triage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.curanexus.triage.domain.VitalSigns;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VitalSignsRepository extends JpaRepository<VitalSigns, UUID> {

    Optional<VitalSigns> findByAssessmentId(UUID assessmentId);
}
