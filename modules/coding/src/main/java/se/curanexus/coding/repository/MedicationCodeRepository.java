package se.curanexus.coding.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.curanexus.coding.domain.MedicationCode;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MedicationCodeRepository extends JpaRepository<MedicationCode, UUID> {

    Optional<MedicationCode> findByCode(String code);

    Optional<MedicationCode> findByCodeAndActiveTrue(String code);

    List<MedicationCode> findByAnatomicalGroup(String anatomicalGroup);

    List<MedicationCode> findByLevel(Integer level);

    List<MedicationCode> findByParentCode(String parentCode);

    @Query("SELECT m FROM MedicationCode m WHERE m.active = true AND " +
           "(LOWER(m.code) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(m.searchText) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<MedicationCode> search(@Param("query") String query, Pageable pageable);

    @Query("SELECT m FROM MedicationCode m WHERE m.active = true AND m.level = 5 AND " +
           "(LOWER(m.code) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(m.searchText) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<MedicationCode> searchChemicalSubstances(@Param("query") String query, Pageable pageable);

    List<MedicationCode> findByCodeStartingWithAndActiveTrue(String prefix);

    long countByActiveTrue();

    @Query("SELECT DISTINCT m.anatomicalGroup FROM MedicationCode m WHERE m.anatomicalGroup IS NOT NULL ORDER BY m.anatomicalGroup")
    List<String> findAllAnatomicalGroups();
}
