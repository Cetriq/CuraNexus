package se.curanexus.medication.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.curanexus.medication.domain.Medication;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MedicationRepository extends JpaRepository<Medication, UUID> {

    Optional<Medication> findByNplId(String nplId);

    List<Medication> findByAtcCode(String atcCode);

    List<Medication> findByAtcCodeStartingWith(String atcCodePrefix);

    List<Medication> findByGenericNameIgnoreCase(String genericName);

    @Query("SELECT m FROM Medication m WHERE m.active = true " +
            "AND (LOWER(m.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(m.genericName) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Medication> searchByName(@Param("query") String query);

    @Query("SELECT m FROM Medication m WHERE m.active = true " +
            "AND (LOWER(m.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(m.genericName) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR m.atcCode LIKE CONCAT(:query, '%'))")
    Page<Medication> search(@Param("query") String query, Pageable pageable);

    List<Medication> findByNarcoticTrueAndActiveTrue();

    List<Medication> findByActiveTrue();

    @Query("SELECT m FROM Medication m WHERE m.atcCode IN :atcCodes AND m.active = true")
    List<Medication> findByAtcCodes(@Param("atcCodes") List<String> atcCodes);
}
