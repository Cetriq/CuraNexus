package se.curanexus.coding.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.curanexus.coding.domain.DiagnosisCode;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DiagnosisCodeRepository extends JpaRepository<DiagnosisCode, UUID> {

    Optional<DiagnosisCode> findByCode(String code);

    Optional<DiagnosisCode> findByCodeAndActiveTrue(String code);

    List<DiagnosisCode> findByChapter(String chapter);

    List<DiagnosisCode> findByParentCode(String parentCode);

    @Query("SELECT d FROM DiagnosisCode d WHERE d.active = true AND " +
           "(LOWER(d.code) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(d.searchText) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<DiagnosisCode> search(@Param("query") String query, Pageable pageable);

    @Query("SELECT d FROM DiagnosisCode d WHERE d.active = true AND d.leaf = true AND " +
           "(LOWER(d.code) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(d.searchText) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<DiagnosisCode> searchLeafCodes(@Param("query") String query, Pageable pageable);

    List<DiagnosisCode> findByCodeStartingWithAndActiveTrue(String prefix);

    long countByActiveTrue();

    @Query("SELECT DISTINCT d.chapter FROM DiagnosisCode d WHERE d.chapter IS NOT NULL ORDER BY d.chapter")
    List<String> findAllChapters();
}
