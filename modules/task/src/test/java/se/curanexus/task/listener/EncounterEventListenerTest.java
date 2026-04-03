package se.curanexus.task.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.curanexus.events.EventMessage;
import se.curanexus.task.domain.Task;
import se.curanexus.task.domain.TaskCategory;
import se.curanexus.task.domain.TaskPriority;
import se.curanexus.task.domain.TaskTemplate;
import se.curanexus.task.domain.TriggerType;
import se.curanexus.task.service.TaskService;
import se.curanexus.task.service.TaskTemplateService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EncounterEventListenerTest {

    private TestableTaskService taskService;
    private TestableTaskTemplateService templateService;
    private EncounterEventListener listener;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        taskService = new TestableTaskService();
        templateService = new TestableTaskTemplateService();
        listener = new EncounterEventListener(taskService, templateService, objectMapper);
    }

    @Test
    void handleEncounterEvent_createdEvent_shouldCreateDocumentationTask() throws JsonProcessingException {
        // Given
        UUID encounterId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        UUID practitionerId = UUID.randomUUID();

        // Set up templates for OUTPATIENT
        templateService.addTemplate(createAlwaysTemplate("Complete encounter documentation",
                TaskCategory.DOCUMENTATION, TaskPriority.NORMAL));
        templateService.addTemplate(createEncounterClassTemplate("OUTPATIENT", "Review patient history",
                TaskCategory.CLINICAL, TaskPriority.NORMAL));

        EventMessage message = createEncounterCreatedMessage(encounterId, patientId, practitionerId, "OUTPATIENT");

        // When
        listener.handleEncounterEvent(message);

        // Then - documentation task is always created + outpatient task
        assertEquals(2, taskService.createdTasks.size());

        // Verify documentation task
        TaskCreation docTask = taskService.createdTasks.stream()
                .filter(t -> t.title.equals("Complete encounter documentation"))
                .findFirst()
                .orElseThrow();

        assertEquals(TaskCategory.DOCUMENTATION, docTask.category);
        assertEquals(TaskPriority.NORMAL, docTask.priority);
        assertEquals(patientId, docTask.patientId);
        assertEquals(encounterId, docTask.encounterId);
        assertEquals(practitionerId, docTask.assigneeId);
    }

    @Test
    void handleEncounterEvent_inpatientEncounter_shouldCreateInpatientTasks() throws JsonProcessingException {
        // Given
        UUID encounterId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        UUID practitionerId = UUID.randomUUID();

        // Set up templates for INPATIENT
        templateService.addTemplate(createAlwaysTemplate("Complete encounter documentation",
                TaskCategory.DOCUMENTATION, TaskPriority.NORMAL));
        templateService.addTemplate(createEncounterClassTemplate("INPATIENT", "Perform initial assessment",
                TaskCategory.CLINICAL, TaskPriority.HIGH));
        templateService.addTemplate(createEncounterClassTemplate("INPATIENT", "Review medication list",
                TaskCategory.CLINICAL, TaskPriority.HIGH));
        templateService.addTemplate(createEncounterClassTemplate("INPATIENT", "Plan discharge",
                TaskCategory.ADMINISTRATIVE, TaskPriority.LOW));

        EventMessage message = createEncounterCreatedMessage(encounterId, patientId, practitionerId, "INPATIENT");

        // When
        listener.handleEncounterEvent(message);

        // Then - should create 4 tasks: documentation + 3 inpatient tasks
        assertEquals(4, taskService.createdTasks.size());

        // Verify inpatient-specific tasks exist
        assertTrue(taskService.hasTaskWithTitle("Perform initial assessment"));
        assertTrue(taskService.hasTaskWithTitle("Review medication list"));
        assertTrue(taskService.hasTaskWithTitle("Plan discharge"));

        // Verify priorities
        TaskCreation triageTask = taskService.getTaskByTitle("Perform initial assessment");
        assertEquals(TaskPriority.HIGH, triageTask.priority);
        assertEquals(TaskCategory.CLINICAL, triageTask.category);

        // Discharge planning should not be assigned
        TaskCreation dischargeTask = taskService.getTaskByTitle("Plan discharge");
        assertNull(dischargeTask.assigneeId);
    }

    @Test
    void handleEncounterEvent_emergencyEncounter_shouldCreateEmergencyTasks() throws JsonProcessingException {
        // Given
        UUID encounterId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        UUID practitionerId = UUID.randomUUID();

        // Set up templates for EMERGENCY
        templateService.addTemplate(createAlwaysTemplate("Complete encounter documentation",
                TaskCategory.DOCUMENTATION, TaskPriority.NORMAL));
        templateService.addTemplate(createEncounterClassTemplate("EMERGENCY", "Triage patient",
                TaskCategory.CLINICAL, TaskPriority.URGENT));
        templateService.addTemplate(createEncounterClassTemplate("EMERGENCY", "Record vital signs",
                TaskCategory.CLINICAL, TaskPriority.HIGH));

        EventMessage message = createEncounterCreatedMessage(encounterId, patientId, practitionerId, "EMERGENCY");

        // When
        listener.handleEncounterEvent(message);

        // Then - should create 3 tasks: documentation + 2 emergency tasks
        assertEquals(3, taskService.createdTasks.size());

        // Verify emergency-specific tasks
        assertTrue(taskService.hasTaskWithTitle("Triage patient"));
        assertTrue(taskService.hasTaskWithTitle("Record vital signs"));

        // Verify urgent priority for triage
        TaskCreation triageTask = taskService.getTaskByTitle("Triage patient");
        assertEquals(TaskPriority.URGENT, triageTask.priority);
    }

    @Test
    void handleEncounterEvent_outpatientEncounter_shouldCreateOutpatientTasks() throws JsonProcessingException {
        // Given
        UUID encounterId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        UUID practitionerId = UUID.randomUUID();

        // Set up templates for OUTPATIENT
        templateService.addTemplate(createAlwaysTemplate("Complete encounter documentation",
                TaskCategory.DOCUMENTATION, TaskPriority.NORMAL));
        templateService.addTemplate(createEncounterClassTemplate("OUTPATIENT", "Review patient history",
                TaskCategory.CLINICAL, TaskPriority.NORMAL));

        EventMessage message = createEncounterCreatedMessage(encounterId, patientId, practitionerId, "OUTPATIENT");

        // When
        listener.handleEncounterEvent(message);

        // Then - should create 2 tasks: documentation + 1 outpatient task
        assertEquals(2, taskService.createdTasks.size());

        assertTrue(taskService.hasTaskWithTitle("Review patient history"));
    }

    @Test
    void handleEncounterEvent_noTemplatesFound_shouldLogWarning() throws JsonProcessingException {
        // Given - no templates configured
        UUID encounterId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        UUID practitionerId = UUID.randomUUID();

        EventMessage message = createEncounterCreatedMessage(encounterId, patientId, practitionerId, "UNKNOWN_CLASS");

        // When
        listener.handleEncounterEvent(message);

        // Then - no tasks created
        assertTrue(taskService.createdTasks.isEmpty());
    }

    @Test
    void handleEncounterEvent_nonEncounterEvent_shouldBeIgnored() {
        // Given
        EventMessage message = new EventMessage(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "TASK",
                "CREATED",
                Instant.now(),
                "{}"
        );

        // When
        listener.handleEncounterEvent(message);

        // Then - no tasks created
        assertTrue(taskService.createdTasks.isEmpty());
    }

    @Test
    void handleEncounterEvent_statusChangedEvent_shouldBeIgnored() {
        // Given
        EventMessage message = new EventMessage(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "ENCOUNTER",
                "STATUS_CHANGED",
                Instant.now(),
                "{}"
        );

        // When
        listener.handleEncounterEvent(message);

        // Then - no tasks created for status change
        assertTrue(taskService.createdTasks.isEmpty());
    }

    private TaskTemplate createAlwaysTemplate(String title, TaskCategory category, TaskPriority priority) {
        return new TaskTemplate("always-" + title.toLowerCase().replace(" ", "-"),
                title, category, priority, TriggerType.ALWAYS, null);
    }

    private TaskTemplate createEncounterClassTemplate(String encounterClass, String title,
                                                       TaskCategory category, TaskPriority priority) {
        return new TaskTemplate(encounterClass.toLowerCase() + "-" + title.toLowerCase().replace(" ", "-"),
                title, category, priority, TriggerType.ENCOUNTER_CLASS, encounterClass);
    }

    private EventMessage createEncounterCreatedMessage(UUID encounterId, UUID patientId,
                                                        UUID practitionerId, String encounterClass) throws JsonProcessingException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("encounterId", encounterId.toString());
        payload.put("patientId", patientId.toString());
        payload.put("responsiblePractitionerId", practitionerId.toString());
        payload.put("encounterClass", encounterClass);

        return new EventMessage(
                UUID.randomUUID(),
                encounterId,
                "ENCOUNTER",
                "CREATED",
                Instant.now(),
                objectMapper.writeValueAsString(payload)
        );
    }

    // Test helper - captures task creation calls
    static class TaskCreation {
        String title;
        TaskCategory category;
        TaskPriority priority;
        UUID createdById;
        UUID patientId;
        UUID encounterId;
        UUID assigneeId;
        LocalDateTime dueAt;
        String sourceType;
        UUID sourceId;

        TaskCreation(String title, TaskCategory category, TaskPriority priority,
                     UUID createdById, UUID patientId, UUID encounterId, UUID assigneeId,
                     LocalDateTime dueAt, String sourceType, UUID sourceId) {
            this.title = title;
            this.category = category;
            this.priority = priority;
            this.createdById = createdById;
            this.patientId = patientId;
            this.encounterId = encounterId;
            this.assigneeId = assigneeId;
            this.dueAt = dueAt;
            this.sourceType = sourceType;
            this.sourceId = sourceId;
        }
    }

    // Testable TaskService that records calls
    static class TestableTaskService extends TaskService {
        List<TaskCreation> createdTasks = new ArrayList<>();
        private int taskIdCounter = 0;

        TestableTaskService() {
            super(null, null, null, null, null);
        }

        @Override
        public Task createTask(String title, TaskCategory category, TaskPriority priority,
                               UUID createdById, UUID patientId, UUID encounterId,
                               UUID assigneeId, LocalDateTime dueAt,
                               String sourceType, UUID sourceId) {
            createdTasks.add(new TaskCreation(title, category, priority, createdById,
                    patientId, encounterId, assigneeId, dueAt, sourceType, sourceId));
            return new Task(title, category, priority, createdById);
        }

        @Override
        public Task createTaskWithDependency(String title, TaskCategory category, TaskPriority priority,
                                              UUID createdById, UUID patientId, UUID encounterId,
                                              UUID assigneeId, LocalDateTime dueAt,
                                              String sourceType, UUID sourceId,
                                              UUID dependsOnTaskId, UUID templateId) {
            createdTasks.add(new TaskCreation(title, category, priority, createdById,
                    patientId, encounterId, assigneeId, dueAt, sourceType, sourceId));
            Task task = new Task(title, category, priority, createdById);
            // Set a fake ID for dependency tracking
            try {
                var idField = Task.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(task, UUID.randomUUID());
            } catch (Exception e) {
                // Ignore
            }
            return task;
        }

        boolean hasTaskWithTitle(String title) {
            return createdTasks.stream().anyMatch(t -> t.title.equals(title));
        }

        TaskCreation getTaskByTitle(String title) {
            return createdTasks.stream()
                    .filter(t -> t.title.equals(title))
                    .findFirst()
                    .orElse(null);
        }
    }

    // Testable TaskTemplateService that returns configured templates
    static class TestableTaskTemplateService extends TaskTemplateService {
        private final List<TaskTemplate> templates = new ArrayList<>();

        TestableTaskTemplateService() {
            super(null);
        }

        void addTemplate(TaskTemplate template) {
            templates.add(template);
        }

        @Override
        public List<TaskTemplate> getTemplatesForEncounterClass(String encounterClass) {
            return templates.stream()
                    .filter(t -> t.getTriggerType() == TriggerType.ALWAYS ||
                            (t.getTriggerType() == TriggerType.ENCOUNTER_CLASS &&
                                    encounterClass.equals(t.getTriggerValue())))
                    .toList();
        }
    }
}
