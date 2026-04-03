package se.curanexus.patient.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.curanexus.patient.domain.Patient;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {

    Optional<Patient> findByPersonalIdentityNumber(String personalIdentityNumber);

    boolean existsByPersonalIdentityNumber(String personalIdentityNumber);

    @Query("""
        SELECT p FROM Patient p
        WHERE (:personalIdentityNumber IS NULL OR p.personalIdentityNumber = :personalIdentityNumber)
        AND (:name IS NULL OR LOWER(CAST(p.givenName AS string)) LIKE LOWER(CONCAT('%', CAST(:name AS string), '%'))
            OR LOWER(CAST(p.familyName AS string)) LIKE LOWER(CONCAT('%', CAST(:name AS string), '%')))
        """)
    Page<Patient> searchPatients(
            @Param("personalIdentityNumber") String personalIdentityNumber,
            @Param("name") String name,
            Pageable pageable);
}
