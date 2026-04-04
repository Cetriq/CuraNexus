package se.curanexus.forms.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.curanexus.forms.domain.FormStatus;
import se.curanexus.forms.domain.FormTemplate;
import se.curanexus.forms.domain.FormType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FormTemplateRepository extends JpaRepository<FormTemplate, UUID> {

    Optional<FormTemplate> findByCodeAndVersion(String code, Integer version);

    @Query("SELECT t FROM FormTemplate t WHERE t.code = :code ORDER BY t.version DESC LIMIT 1")
    Optional<FormTemplate> findLatestByCode(@Param("code") String code);

    @Query("SELECT t FROM FormTemplate t WHERE t.code = :code AND t.status = 'ACTIVE' ORDER BY t.version DESC LIMIT 1")
    Optional<FormTemplate> findActiveByCode(@Param("code") String code);

    List<FormTemplate> findByStatus(FormStatus status);

    List<FormTemplate> findByType(FormType type);

    List<FormTemplate> findByTypeAndStatus(FormType type, FormStatus status);

    List<FormTemplate> findByCategory(String category);

    @Query("""
        SELECT DISTINCT t FROM FormTemplate t
        WHERE t.status = :status
        AND (:type IS NULL OR t.type = :type)
        AND (:category IS NULL OR t.category = :category)
        ORDER BY t.name
        """)
    Page<FormTemplate> searchTemplates(
            @Param("status") FormStatus status,
            @Param("type") FormType type,
            @Param("category") String category,
            Pageable pageable);

    @Query("SELECT DISTINCT t.category FROM FormTemplate t WHERE t.category IS NOT NULL ORDER BY t.category")
    List<String> findAllCategories();

    boolean existsByCode(String code);
}
