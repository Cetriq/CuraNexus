package se.curanexus.patient.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.curanexus.patient.domain.ContactInfo;

import java.util.List;
import java.util.UUID;

@Repository
public interface ContactInfoRepository extends JpaRepository<ContactInfo, UUID> {

    List<ContactInfo> findByPatientId(UUID patientId);
}
