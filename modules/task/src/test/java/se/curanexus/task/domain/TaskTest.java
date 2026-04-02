package se.curanexus.task.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    void shouldCreateTaskWithPendingStatus() {
        UUID createdById = UUID.randomUUID();

        Task task = new Task("Review lab results", TaskCategory.LAB, TaskPriority.HIGH, createdById);

        assertEquals("Review lab results", task.getTitle());
        assertEquals(TaskCategory.LAB, task.getCategory());
        assertEquals(TaskPriority.HIGH, task.getPriority());
        assertEquals(TaskStatus.PENDING, task.getStatus());
        assertEquals(createdById, task.getCreatedById());
        assertNotNull(task.getCreatedAt());
        assertNull(task.getAssigneeId());
    }

    @Test
    void shouldAssignTaskAndChangeStatusToAssigned() {
        Task task = createTestTask();
        UUID assigneeId = UUID.randomUUID();

        task.assign(assigneeId);

        assertEquals(TaskStatus.ASSIGNED, task.getStatus());
        assertEquals(assigneeId, task.getAssigneeId());
        assertNotNull(task.getUpdatedAt());
    }

    @Test
    void shouldStartAssignedTask() {
        Task task = createTestTask();
        task.assign(UUID.randomUUID());

        task.start();

        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
        assertNotNull(task.getStartedAt());
    }

    @Test
    void shouldStartPendingTask() {
        Task task = createTestTask();

        task.start();

        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
    }

    @Test
    void shouldCompleteTask() {
        Task task = createTestTask();
        task.start();

        task.complete("Done successfully", "Success");

        assertEquals(TaskStatus.COMPLETED, task.getStatus());
        assertEquals("Done successfully", task.getCompletionNote());
        assertEquals("Success", task.getOutcome());
        assertNotNull(task.getCompletedAt());
    }

    @Test
    void shouldNotCompleteAlreadyCompletedTask() {
        Task task = createTestTask();
        task.complete(null, null);

        assertThrows(IllegalStateException.class, () ->
                task.complete("Again", "Retry"));
    }

    @Test
    void shouldCancelTask() {
        Task task = createTestTask();

        task.cancel("No longer needed");

        assertEquals(TaskStatus.CANCELLED, task.getStatus());
        assertEquals("No longer needed", task.getCancelReason());
    }

    @Test
    void shouldNotCancelCompletedTask() {
        Task task = createTestTask();
        task.complete(null, null);

        assertThrows(IllegalStateException.class, () ->
                task.cancel("Too late"));
    }

    @Test
    void shouldNotAssignCompletedTask() {
        Task task = createTestTask();
        task.complete(null, null);

        assertThrows(IllegalStateException.class, () ->
                task.assign(UUID.randomUUID()));
    }

    @Test
    void shouldBlockTask() {
        Task task = createTestTask();
        task.start();

        task.block();

        assertEquals(TaskStatus.BLOCKED, task.getStatus());
    }

    @Test
    void shouldUnblockTask() {
        Task task = createTestTask();
        task.assign(UUID.randomUUID());
        task.block();

        task.unblock();

        assertEquals(TaskStatus.ASSIGNED, task.getStatus());
    }

    @Test
    void shouldDetectOverdueTask() {
        Task task = createTestTask();
        task.setDueAt(LocalDateTime.now().minusDays(1));

        assertTrue(task.isOverdue());
    }

    @Test
    void shouldNotBeOverdueWhenCompleted() {
        Task task = createTestTask();
        task.setDueAt(LocalDateTime.now().minusDays(1));
        task.complete(null, null);

        assertFalse(task.isOverdue());
    }

    @Test
    void shouldNotBeOverdueWhenNotDue() {
        Task task = createTestTask();
        task.setDueAt(LocalDateTime.now().plusDays(1));

        assertFalse(task.isOverdue());
    }

    private Task createTestTask() {
        return new Task("Test task", TaskCategory.CLINICAL, TaskPriority.NORMAL, UUID.randomUUID());
    }
}
