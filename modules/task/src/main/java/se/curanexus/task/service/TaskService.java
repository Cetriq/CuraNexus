package se.curanexus.task.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.curanexus.task.domain.*;
import se.curanexus.task.repository.*;
import se.curanexus.task.service.exception.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final ReminderRepository reminderRepository;
    private final DelegationRepository delegationRepository;
    private final WatchRepository watchRepository;

    public TaskService(TaskRepository taskRepository,
                       ReminderRepository reminderRepository,
                       DelegationRepository delegationRepository,
                       WatchRepository watchRepository) {
        this.taskRepository = taskRepository;
        this.reminderRepository = reminderRepository;
        this.delegationRepository = delegationRepository;
        this.watchRepository = watchRepository;
    }

    // === Tasks ===

    public Task createTask(String title, TaskCategory category, TaskPriority priority,
                           UUID createdById, UUID patientId, UUID encounterId,
                           UUID assigneeId, LocalDateTime dueAt,
                           String sourceType, UUID sourceId) {
        Task task = new Task(title, category, priority, createdById);
        task.setPatientId(patientId);
        task.setEncounterId(encounterId);
        task.setDueAt(dueAt);
        task.setSourceType(sourceType);
        task.setSourceId(sourceId);

        if (assigneeId != null) {
            task.assign(assigneeId);
        }

        return taskRepository.save(task);
    }

    @Transactional(readOnly = true)
    public Task getTask(UUID taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));
    }

    @Transactional(readOnly = true)
    public List<Task> getTasksByAssignee(UUID assigneeId) {
        return taskRepository.findByAssigneeIdOrderByPriorityDescCreatedAtDesc(assigneeId);
    }

    @Transactional(readOnly = true)
    public List<Task> getActiveTasksByAssignee(UUID assigneeId) {
        return taskRepository.findActiveByAssignee(assigneeId);
    }

    @Transactional(readOnly = true)
    public List<Task> getTasksByPatient(UUID patientId) {
        return taskRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
    }

    @Transactional(readOnly = true)
    public List<Task> getTasksByEncounter(UUID encounterId) {
        return taskRepository.findByEncounterIdOrderByCreatedAtDesc(encounterId);
    }

    @Transactional(readOnly = true)
    public List<Task> getOverdueTasks() {
        return taskRepository.findOverdueTasks(LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public List<Task> getUnassignedTasks() {
        return taskRepository.findUnassignedTasks();
    }

    public Task updateTask(UUID taskId, String title, String description,
                           TaskPriority priority, LocalDateTime dueAt) {
        Task task = getTask(taskId);
        if (title != null) task.setTitle(title);
        if (description != null) task.setDescription(description);
        if (priority != null) task.setPriority(priority);
        if (dueAt != null) task.setDueAt(dueAt);
        return taskRepository.save(task);
    }

    public Task assignTask(UUID taskId, UUID assigneeId) {
        Task task = getTask(taskId);
        task.assign(assigneeId);
        return taskRepository.save(task);
    }

    public Task startTask(UUID taskId) {
        Task task = getTask(taskId);
        task.start();
        return taskRepository.save(task);
    }

    public Task completeTask(UUID taskId, String completionNote, String outcome) {
        Task task = getTask(taskId);
        task.complete(completionNote, outcome);
        return taskRepository.save(task);
    }

    public Task cancelTask(UUID taskId, String reason) {
        Task task = getTask(taskId);
        task.cancel(reason);
        return taskRepository.save(task);
    }

    // === Reminders ===

    public Reminder createReminder(UUID userId, String message, LocalDateTime remindAt,
                                    UUID patientId, UUID encounterId, UUID taskId,
                                    boolean recurring, String recurrencePattern) {
        Reminder reminder = new Reminder(userId, message, remindAt);
        reminder.setPatientId(patientId);
        reminder.setEncounterId(encounterId);
        reminder.setTaskId(taskId);
        reminder.setRecurring(recurring);
        reminder.setRecurrencePattern(recurrencePattern);
        return reminderRepository.save(reminder);
    }

    @Transactional(readOnly = true)
    public Reminder getReminder(UUID reminderId) {
        return reminderRepository.findById(reminderId)
                .orElseThrow(() -> new ReminderNotFoundException(reminderId));
    }

    @Transactional(readOnly = true)
    public List<Reminder> getRemindersByUser(UUID userId) {
        return reminderRepository.findByUserIdOrderByRemindAtAsc(userId);
    }

    @Transactional(readOnly = true)
    public List<Reminder> getActiveRemindersByUser(UUID userId) {
        return reminderRepository.findActiveByUser(userId);
    }

    @Transactional(readOnly = true)
    public List<Reminder> getDueReminders() {
        return reminderRepository.findDueReminders(LocalDateTime.now());
    }

    public Reminder acknowledgeReminder(UUID reminderId) {
        Reminder reminder = getReminder(reminderId);
        reminder.acknowledge();
        return reminderRepository.save(reminder);
    }

    public Reminder snoozeReminder(UUID reminderId, LocalDateTime until) {
        Reminder reminder = getReminder(reminderId);
        reminder.snooze(until);
        return reminderRepository.save(reminder);
    }

    public void deleteReminder(UUID reminderId) {
        if (!reminderRepository.existsById(reminderId)) {
            throw new ReminderNotFoundException(reminderId);
        }
        reminderRepository.deleteById(reminderId);
    }

    // === Delegations ===

    public Delegation createDelegation(UUID fromUserId, UUID toUserId,
                                        LocalDateTime validFrom, LocalDateTime validUntil,
                                        String scope, String note) {
        Delegation delegation = new Delegation(fromUserId, toUserId, validFrom, validUntil);
        delegation.setScope(scope);
        delegation.setNote(note);
        return delegationRepository.save(delegation);
    }

    @Transactional(readOnly = true)
    public Delegation getDelegation(UUID delegationId) {
        return delegationRepository.findById(delegationId)
                .orElseThrow(() -> new DelegationNotFoundException(delegationId));
    }

    @Transactional(readOnly = true)
    public List<Delegation> getActiveDelegationsFrom(UUID userId) {
        return delegationRepository.findActiveDelegationsFrom(userId, LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public List<Delegation> getActiveDelegationsTo(UUID userId) {
        return delegationRepository.findActiveDelegationsTo(userId, LocalDateTime.now());
    }

    public Delegation revokeDelegation(UUID delegationId, UUID revokedById) {
        Delegation delegation = getDelegation(delegationId);
        delegation.revoke(revokedById);
        return delegationRepository.save(delegation);
    }

    public void processExpiredDelegations() {
        List<Delegation> expired = delegationRepository.findExpiredDelegations(LocalDateTime.now());
        for (Delegation delegation : expired) {
            delegation.checkExpiration();
            delegationRepository.save(delegation);
        }
    }

    // === Watches ===

    public Watch createWatch(UUID userId, WatchType watchType, UUID targetId,
                              boolean notifyOnChange, String note) {
        Watch watch = new Watch(userId, watchType, targetId);
        watch.setNotifyOnChange(notifyOnChange);
        watch.setNote(note);
        return watchRepository.save(watch);
    }

    @Transactional(readOnly = true)
    public Watch getWatch(UUID watchId) {
        return watchRepository.findById(watchId)
                .orElseThrow(() -> new WatchNotFoundException(watchId));
    }

    @Transactional(readOnly = true)
    public List<Watch> getWatchesByUser(UUID userId) {
        return watchRepository.findByUserIdAndActiveTrue(userId);
    }

    @Transactional(readOnly = true)
    public List<Watch> getWatchersToNotify(WatchType watchType, UUID targetId) {
        return watchRepository.findWatchersToNotify(watchType, targetId);
    }

    public void deleteWatch(UUID watchId) {
        Watch watch = getWatch(watchId);
        watch.deactivate();
        watchRepository.save(watch);
    }

    public Watch recordWatchNotification(UUID watchId) {
        Watch watch = getWatch(watchId);
        watch.recordNotification();
        return watchRepository.save(watch);
    }
}
