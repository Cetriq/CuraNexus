package se.curanexus.task.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.curanexus.task.domain.Task;
import se.curanexus.task.domain.TaskPriority;
import se.curanexus.task.domain.TaskStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    List<Task> findByAssigneeIdOrderByPriorityDescCreatedAtDesc(UUID assigneeId);

    List<Task> findByAssigneeIdAndStatusOrderByPriorityDescCreatedAtDesc(UUID assigneeId, TaskStatus status);

    List<Task> findByPatientIdOrderByCreatedAtDesc(UUID patientId);

    List<Task> findByEncounterIdOrderByCreatedAtDesc(UUID encounterId);

    List<Task> findByStatusOrderByPriorityDescCreatedAtDesc(TaskStatus status);

    List<Task> findByStatusAndPriorityOrderByCreatedAtDesc(TaskStatus status, TaskPriority priority);

    @Query("SELECT t FROM Task t WHERE t.assigneeId = :assigneeId AND t.status NOT IN ('COMPLETED', 'CANCELLED') ORDER BY t.priority DESC, t.createdAt DESC")
    List<Task> findActiveByAssignee(@Param("assigneeId") UUID assigneeId);

    @Query("SELECT t FROM Task t WHERE t.status NOT IN ('COMPLETED', 'CANCELLED') AND t.dueAt < :now ORDER BY t.dueAt ASC")
    List<Task> findOverdueTasks(@Param("now") LocalDateTime now);

    @Query("SELECT t FROM Task t WHERE t.assigneeId = :assigneeId AND t.status NOT IN ('COMPLETED', 'CANCELLED') AND t.dueAt < :now")
    List<Task> findOverdueByAssignee(@Param("assigneeId") UUID assigneeId, @Param("now") LocalDateTime now);

    @Query("SELECT t FROM Task t WHERE t.status = 'PENDING' AND t.assigneeId IS NULL ORDER BY t.priority DESC, t.createdAt ASC")
    List<Task> findUnassignedTasks();

    @Query("SELECT t FROM Task t WHERE t.encounterId = :encounterId AND t.status NOT IN ('COMPLETED', 'CANCELLED')")
    List<Task> findActiveByEncounter(@Param("encounterId") UUID encounterId);

    @Query("SELECT t FROM Task t WHERE t.patientId = :patientId AND t.status NOT IN ('COMPLETED', 'CANCELLED')")
    List<Task> findActiveByPatient(@Param("patientId") UUID patientId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.assigneeId = :assigneeId AND t.status NOT IN ('COMPLETED', 'CANCELLED')")
    long countActiveByAssignee(@Param("assigneeId") UUID assigneeId);

    @Query("SELECT t FROM Task t WHERE t.sourceType = :sourceType AND t.sourceId = :sourceId")
    List<Task> findBySource(@Param("sourceType") String sourceType, @Param("sourceId") UUID sourceId);

    @Query("SELECT t FROM Task t WHERE t.dependsOnTaskId = :taskId")
    List<Task> findDependentTasks(@Param("taskId") UUID taskId);

    @Query("SELECT t FROM Task t WHERE t.dependsOnTaskId = :taskId AND t.status = 'BLOCKED'")
    List<Task> findBlockedDependentTasks(@Param("taskId") UUID taskId);

    @Query("SELECT t FROM Task t WHERE t.templateId = :templateId AND t.encounterId = :encounterId")
    List<Task> findByTemplateAndEncounter(@Param("templateId") UUID templateId, @Param("encounterId") UUID encounterId);

    @Query("SELECT t FROM Task t WHERE t.status NOT IN ('COMPLETED', 'CANCELLED') AND t.dueAt < :now AND t.escalated = false")
    List<Task> findOverdueNotEscalated(@Param("now") LocalDateTime now);

    @Query("SELECT t FROM Task t WHERE t.encounterId = :encounterId AND t.status = 'BLOCKED'")
    List<Task> findBlockedByEncounter(@Param("encounterId") UUID encounterId);
}
