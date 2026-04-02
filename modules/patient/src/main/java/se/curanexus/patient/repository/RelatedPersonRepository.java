package se.curanexus.patient.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.curanexus.patient.domain.RelatedPerson;

import java.util.List;
import java.util.UUID;

@Repository
public interface RelatedPersonRepository extends JpaRepository<RelatedPerson, UUID> {

    List<RelatedPerson> findByPatientId(UUID patientId);
}
