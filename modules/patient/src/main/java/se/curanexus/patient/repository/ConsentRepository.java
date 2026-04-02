package se.curanexus.patient.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.curanexus.patient.domain.Consent;
import se.curanexus.patient.domain.ConsentStatus;

import java.util.List;
import java.util.UUID;

@Repository
public interface ConsentRepository extends JpaRepository<Consent, UUID> {

    List<Consent> findByPatientId(UUID patientId);

    List<Consent> findByPatientIdAndStatus(UUID patientId, ConsentStatus status);
}
