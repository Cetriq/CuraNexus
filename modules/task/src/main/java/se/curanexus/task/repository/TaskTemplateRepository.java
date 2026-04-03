package se.curanexus.task.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.curanexus.task.domain.TaskTemplate;
import se.curanexus.task.domain.TriggerType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskTemplateRepository extends JpaRepository<TaskTemplate, UUID> {

    Optional<TaskTemplate> findByName(String name);

    List<TaskTemplate> findByActiveTrue();

    List<TaskTemplate> findByActiveTrueOrderBySortOrderAsc();

    @Query("SELECT t FROM TaskTemplate t WHERE t.active = true AND " +
           "(t.triggerType = 'ALWAYS' OR " +
           "(t.triggerType = :triggerType AND t.triggerValue = :triggerValue)) " +
           "ORDER BY t.sortOrder ASC")
    List<TaskTemplate> findMatchingTemplates(
            @Param("triggerType") TriggerType triggerType,
            @Param("triggerValue") String triggerValue);

    @Query("SELECT t FROM TaskTemplate t WHERE t.active = true AND t.triggerType = 'ALWAYS' ORDER BY t.sortOrder ASC")
    List<TaskTemplate> findAlwaysTemplates();
}
