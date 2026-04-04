package se.curanexus.coding.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.curanexus.coding.domain.ProcedureCode;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProcedureCodeRepository extends JpaRepository<ProcedureCode, UUID> {

    Optional<ProcedureCode> findByCode(String code);

    Optional<ProcedureCode> findByCodeAndActiveTrue(String code);

    List<ProcedureCode> findByCategory(String category);

    List<ProcedureCode> findByParentCode(String parentCode);

    @Query("SELECT p FROM ProcedureCode p WHERE p.active = true AND " +
           "(LOWER(p.code) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.searchText) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<ProcedureCode> search(@Param("query") String query, Pageable pageable);

    @Query("SELECT p FROM ProcedureCode p WHERE p.active = true AND p.leaf = true AND " +
           "(LOWER(p.code) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.searchText) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<ProcedureCode> searchLeafCodes(@Param("query") String query, Pageable pageable);

    List<ProcedureCode> findByCodeStartingWithAndActiveTrue(String prefix);

    long countByActiveTrue();

    @Query("SELECT DISTINCT p.category FROM ProcedureCode p WHERE p.category IS NOT NULL ORDER BY p.category")
    List<String> findAllCategories();
}
