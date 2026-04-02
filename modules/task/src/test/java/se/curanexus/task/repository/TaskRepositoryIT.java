package se.curanexus.task.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import se.curanexus.task.domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TaskRepositoryIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("curanexus_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void shouldSaveAndFindTask() {
        UUID createdById = UUID.randomUUID();
        Task task = new Task("Test task", TaskCategory.CLINICAL, TaskPriority.HIGH, createdById);

        Task saved = taskRepository.save(task);

        assertNotNull(saved.getId());
        assertEquals("Test task", saved.getTitle());
        assertEquals(TaskStatus.PENDING, saved.getStatus());
    }

    @Test
    void shouldFindByAssignee() {
        UUID assigneeId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();

        Task task1 = new Task("Task 1", TaskCategory.CLINICAL, TaskPriority.HIGH, createdById);
        task1.assign(assigneeId);
        taskRepository.save(task1);

        Task task2 = new Task("Task 2", TaskCategory.LAB, TaskPriority.NORMAL, createdById);
        task2.assign(assigneeId);
        taskRepository.save(task2);

        Task task3 = new Task("Task 3", TaskCategory.DOCUMENTATION, TaskPriority.LOW, createdById);
        task3.assign(UUID.randomUUID());
        taskRepository.save(task3);

        List<Task> tasks = taskRepository.findByAssigneeIdOrderByPriorityDescCreatedAtDesc(assigneeId);

        assertEquals(2, tasks.size());
        assertEquals(TaskPriority.HIGH, tasks.get(0).getPriority());
    }

    @Test
    void shouldFindActiveByAssignee() {
        UUID assigneeId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();

        Task active = new Task("Active", TaskCategory.CLINICAL, TaskPriority.HIGH, createdById);
        active.assign(assigneeId);
        taskRepository.save(active);

        Task completed = new Task("Completed", TaskCategory.LAB, TaskPriority.NORMAL, createdById);
        completed.assign(assigneeId);
        completed.complete("Done", null);
        taskRepository.save(completed);

        List<Task> tasks = taskRepository.findActiveByAssignee(assigneeId);

        assertEquals(1, tasks.size());
        assertEquals("Active", tasks.get(0).getTitle());
    }

    @Test
    void shouldFindOverdueTasks() {
        UUID createdById = UUID.randomUUID();

        Task overdue = new Task("Overdue", TaskCategory.CLINICAL, TaskPriority.HIGH, createdById);
        overdue.setDueAt(LocalDateTime.now().minusDays(1));
        taskRepository.save(overdue);

        Task notDue = new Task("Not due", TaskCategory.LAB, TaskPriority.NORMAL, createdById);
        notDue.setDueAt(LocalDateTime.now().plusDays(1));
        taskRepository.save(notDue);

        List<Task> tasks = taskRepository.findOverdueTasks(LocalDateTime.now());

        assertEquals(1, tasks.size());
        assertEquals("Overdue", tasks.get(0).getTitle());
    }

    @Test
    void shouldFindUnassignedTasks() {
        UUID createdById = UUID.randomUUID();

        Task unassigned = new Task("Unassigned", TaskCategory.CLINICAL, TaskPriority.HIGH, createdById);
        taskRepository.save(unassigned);

        Task assigned = new Task("Assigned", TaskCategory.LAB, TaskPriority.NORMAL, createdById);
        assigned.assign(UUID.randomUUID());
        taskRepository.save(assigned);

        List<Task> tasks = taskRepository.findUnassignedTasks();

        assertEquals(1, tasks.size());
        assertEquals("Unassigned", tasks.get(0).getTitle());
    }

    @Test
    void shouldFindByPatient() {
        UUID patientId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();

        Task task1 = new Task("Patient task", TaskCategory.CLINICAL, TaskPriority.HIGH, createdById);
        task1.setPatientId(patientId);
        taskRepository.save(task1);

        Task task2 = new Task("Other patient task", TaskCategory.LAB, TaskPriority.NORMAL, createdById);
        task2.setPatientId(UUID.randomUUID());
        taskRepository.save(task2);

        List<Task> tasks = taskRepository.findByPatientIdOrderByCreatedAtDesc(patientId);

        assertEquals(1, tasks.size());
        assertEquals("Patient task", tasks.get(0).getTitle());
    }

    @Test
    void shouldCountActiveByAssignee() {
        UUID assigneeId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();

        Task task1 = new Task("Task 1", TaskCategory.CLINICAL, TaskPriority.HIGH, createdById);
        task1.assign(assigneeId);
        taskRepository.save(task1);

        Task task2 = new Task("Task 2", TaskCategory.LAB, TaskPriority.NORMAL, createdById);
        task2.assign(assigneeId);
        taskRepository.save(task2);

        Task completed = new Task("Completed", TaskCategory.DOCUMENTATION, TaskPriority.LOW, createdById);
        completed.assign(assigneeId);
        completed.complete(null, null);
        taskRepository.save(completed);

        long count = taskRepository.countActiveByAssignee(assigneeId);

        assertEquals(2, count);
    }
}
