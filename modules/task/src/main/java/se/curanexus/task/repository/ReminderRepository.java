package se.curanexus.task.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.curanexus.task.domain.Reminder;
import se.curanexus.task.domain.ReminderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, UUID> {

    List<Reminder> findByUserIdOrderByRemindAtAsc(UUID userId);

    List<Reminder> findByUserIdAndStatusOrderByRemindAtAsc(UUID userId, ReminderStatus status);

    List<Reminder> findByTaskId(UUID taskId);

    @Query("SELECT r FROM Reminder r WHERE r.status IN ('PENDING', 'SNOOZED') AND r.remindAt <= :now")
    List<Reminder> findDueReminders(@Param("now") LocalDateTime now);

    @Query("SELECT r FROM Reminder r WHERE r.userId = :userId AND r.status IN ('PENDING', 'SNOOZED') ORDER BY r.remindAt ASC")
    List<Reminder> findActiveByUser(@Param("userId") UUID userId);

    @Query("SELECT r FROM Reminder r WHERE r.userId = :userId AND r.status = 'TRIGGERED' ORDER BY r.remindAt DESC")
    List<Reminder> findTriggeredByUser(@Param("userId") UUID userId);

    @Query("SELECT r FROM Reminder r WHERE r.patientId = :patientId AND r.status NOT IN ('ACKNOWLEDGED', 'CANCELLED')")
    List<Reminder> findActiveByPatient(@Param("patientId") UUID patientId);

    @Query("SELECT COUNT(r) FROM Reminder r WHERE r.userId = :userId AND r.status = 'TRIGGERED'")
    long countTriggeredByUser(@Param("userId") UUID userId);
}
