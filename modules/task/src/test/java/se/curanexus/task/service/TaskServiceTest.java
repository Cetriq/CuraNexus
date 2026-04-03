package se.curanexus.task.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.curanexus.events.DomainEventPublisher;
import se.curanexus.task.domain.*;
import se.curanexus.task.repository.*;
import se.curanexus.task.service.exception.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ReminderRepository reminderRepository;

    @Mock
    private DelegationRepository delegationRepository;

    @Mock
    private WatchRepository watchRepository;

    @Mock
    private DomainEventPublisher eventPublisher;

    @InjectMocks
    private TaskService taskService;

    private UUID createdById;

    @BeforeEach
    void setUp() {
        createdById = UUID.randomUUID();
    }

    @Test
    void shouldCreateTask() {
        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

        Task result = taskService.createTask(
                "Review patient",
                TaskCategory.CLINICAL,
                TaskPriority.HIGH,
                createdById,
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                LocalDateTime.now().plusHours(2),
                null,
                null
        );

        assertNotNull(result);
        assertEquals("Review patient", result.getTitle());
        assertEquals(TaskCategory.CLINICAL, result.getCategory());
        assertEquals(TaskPriority.HIGH, result.getPriority());
        assertEquals(TaskStatus.PENDING, result.getStatus());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void shouldCreateTaskWithAssignee() {
        UUID assigneeId = UUID.randomUUID();
        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

        Task result = taskService.createTask(
                "Review patient",
                TaskCategory.CLINICAL,
                TaskPriority.HIGH,
                createdById,
                null,
                null,
                assigneeId,
                null,
                null,
                null
        );

        assertEquals(TaskStatus.ASSIGNED, result.getStatus());
        assertEquals(assigneeId, result.getAssigneeId());
    }

    @Test
    void shouldGetTask() {
        UUID taskId = UUID.randomUUID();
        Task task = new Task("Test", TaskCategory.CLINICAL, TaskPriority.NORMAL, createdById);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        Task result = taskService.getTask(taskId);

        assertNotNull(result);
        assertEquals("Test", result.getTitle());
    }

    @Test
    void shouldThrowWhenTaskNotFound() {
        UUID taskId = UUID.randomUUID();
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.getTask(taskId));
    }

    @Test
    void shouldAssignTask() {
        UUID taskId = UUID.randomUUID();
        UUID assigneeId = UUID.randomUUID();
        Task task = new Task("Test", TaskCategory.CLINICAL, TaskPriority.NORMAL, createdById);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

        Task result = taskService.assignTask(taskId, assigneeId);

        assertEquals(TaskStatus.ASSIGNED, result.getStatus());
        assertEquals(assigneeId, result.getAssigneeId());
    }

    @Test
    void shouldCompleteTask() {
        UUID taskId = UUID.randomUUID();
        Task task = new Task("Test", TaskCategory.CLINICAL, TaskPriority.NORMAL, createdById);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

        Task result = taskService.completeTask(taskId, "Done", "Success");

        assertEquals(TaskStatus.COMPLETED, result.getStatus());
        assertEquals("Done", result.getCompletionNote());
        assertEquals("Success", result.getOutcome());
    }

    @Test
    void shouldCreateReminder() {
        when(reminderRepository.save(any(Reminder.class))).thenAnswer(i -> i.getArgument(0));

        UUID userId = UUID.randomUUID();
        Reminder result = taskService.createReminder(
                userId,
                "Follow up",
                LocalDateTime.now().plusHours(1),
                null,
                null,
                null,
                false,
                null
        );

        assertNotNull(result);
        assertEquals("Follow up", result.getMessage());
        assertEquals(ReminderStatus.PENDING, result.getStatus());
        verify(reminderRepository).save(any(Reminder.class));
    }

    @Test
    void shouldAcknowledgeReminder() {
        UUID reminderId = UUID.randomUUID();
        Reminder reminder = new Reminder(UUID.randomUUID(), "Test", LocalDateTime.now().plusHours(1));
        reminder.trigger();
        when(reminderRepository.findById(reminderId)).thenReturn(Optional.of(reminder));
        when(reminderRepository.save(any(Reminder.class))).thenAnswer(i -> i.getArgument(0));

        Reminder result = taskService.acknowledgeReminder(reminderId);

        assertEquals(ReminderStatus.ACKNOWLEDGED, result.getStatus());
        assertNotNull(result.getAcknowledgedAt());
    }

    @Test
    void shouldCreateDelegation() {
        when(delegationRepository.save(any(Delegation.class))).thenAnswer(i -> i.getArgument(0));

        Delegation result = taskService.createDelegation(
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(7),
                "All tasks",
                "Vacation coverage"
        );

        assertNotNull(result);
        assertEquals(DelegationStatus.ACTIVE, result.getStatus());
        assertEquals("All tasks", result.getScope());
        verify(delegationRepository).save(any(Delegation.class));
    }

    @Test
    void shouldCreateWatch() {
        when(watchRepository.save(any(Watch.class))).thenAnswer(i -> i.getArgument(0));

        Watch result = taskService.createWatch(
                UUID.randomUUID(),
                WatchType.PATIENT,
                UUID.randomUUID(),
                true,
                "Monitor for discharge"
        );

        assertNotNull(result);
        assertEquals(WatchType.PATIENT, result.getWatchType());
        assertTrue(result.isActive());
        verify(watchRepository).save(any(Watch.class));
    }
}
