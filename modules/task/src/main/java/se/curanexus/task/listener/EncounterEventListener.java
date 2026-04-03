package se.curanexus.task.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import se.curanexus.events.EventMessage;
import se.curanexus.events.config.RabbitMQConfig;
import se.curanexus.task.domain.Task;
import se.curanexus.task.domain.TaskTemplate;
import se.curanexus.task.service.TaskService;
import se.curanexus.task.service.TaskTemplateService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Listens for encounter events and automatically creates tasks when encounters are created.
 * Tasks are created based on configurable templates stored in the database.
 */
@Component
public class EncounterEventListener {

    private static final Logger log = LoggerFactory.getLogger(EncounterEventListener.class);

    // System user ID for auto-created tasks
    private static final UUID SYSTEM_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final TaskService taskService;
    private final TaskTemplateService templateService;
    private final ObjectMapper objectMapper;

    public EncounterEventListener(TaskService taskService, TaskTemplateService templateService,
                                   ObjectMapper objectMapper) {
        this.taskService = taskService;
        this.templateService = templateService;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = RabbitMQConfig.TASK_MODULE_QUEUE)
    @Transactional
    public void handleEncounterEvent(EventMessage message) {
        try {
            if (!"ENCOUNTER".equals(message.getAggregateType())) {
                log.debug("Ignoring non-encounter event: {}", message.getEventType());
                return;
            }

            switch (message.getEventType()) {
                case "CREATED" -> handleEncounterCreated(message);
                default -> log.debug("Ignoring encounter event type: {}", message.getEventType());
            }
        } catch (Exception e) {
            log.error("Failed to process encounter event: {} for encounter {}",
                    message.getEventType(), message.getAggregateId(), e);
            throw e;
        }
    }

    private void handleEncounterCreated(EventMessage message) {
        try {
            JsonNode payload = objectMapper.readTree(message.getPayload());

            UUID encounterId = message.getAggregateId();
            UUID patientId = extractUUID(payload, "patientId");
            UUID responsiblePractitionerId = extractUUID(payload, "responsiblePractitionerId");
            String encounterClass = extractText(payload, "encounterClass");

            log.info("Creating initial tasks for new encounter {} (class: {})", encounterId, encounterClass);

            // Get matching templates and create tasks
            createTasksFromTemplates(encounterId, patientId, responsiblePractitionerId, encounterClass);

        } catch (Exception e) {
            log.error("Failed to parse encounter created event payload", e);
            throw new RuntimeException("Failed to process encounter created event", e);
        }
    }

    private void createTasksFromTemplates(UUID encounterId, UUID patientId,
                                           UUID assigneeId, String encounterClass) {
        List<TaskTemplate> templates = templateService.getTemplatesForEncounterClass(encounterClass);

        if (templates.isEmpty()) {
            log.warn("No task templates found for encounter class: {}", encounterClass);
            return;
        }

        // Track created tasks by template name for dependency resolution
        Map<String, Task> createdTasksByTemplateName = new HashMap<>();
        LocalDateTime encounterStartTime = LocalDateTime.now();

        int createdCount = 0;
        for (TaskTemplate template : templates) {
            try {
                // For discharge planning tasks, don't assign immediately
                UUID taskAssignee = "Plan discharge".equals(template.getTitle()) ? null : assigneeId;

                // Calculate due date from template offset
                LocalDateTime dueAt = null;
                if (template.getDueOffsetMinutes() != null) {
                    dueAt = encounterStartTime.plusMinutes(template.getDueOffsetMinutes());
                }

                // Resolve dependency
                UUID dependsOnTaskId = null;
                if (template.hasDependency()) {
                    Task dependencyTask = createdTasksByTemplateName.get(template.getDependsOnTemplate());
                    if (dependencyTask != null) {
                        dependsOnTaskId = dependencyTask.getId();
                        log.debug("Task '{}' depends on task '{}'", template.getTitle(), dependencyTask.getTitle());
                    } else {
                        log.warn("Dependency template '{}' not found for template '{}'",
                                template.getDependsOnTemplate(), template.getName());
                    }
                }

                Task task = taskService.createTaskWithDependency(
                        template.getTitle(),
                        template.getCategory(),
                        template.getDefaultPriority(),
                        SYSTEM_USER_ID,
                        patientId,
                        encounterId,
                        taskAssignee,
                        dueAt,
                        "ENCOUNTER",
                        encounterId,
                        dependsOnTaskId,
                        template.getId()
                );

                createdTasksByTemplateName.put(template.getName(), task);
                createdCount++;

                String status = task.getDependsOnTaskId() != null ? " (blocked)" : "";
                String dueInfo = dueAt != null ? " due at " + dueAt : "";
                log.debug("Created task '{}' from template '{}'{}{}",
                        template.getTitle(), template.getName(), status, dueInfo);

            } catch (Exception e) {
                log.error("Failed to create task from template '{}': {}", template.getName(), e.getMessage());
            }
        }

        log.info("Created {} tasks for encounter {} from {} templates",
                createdCount, encounterId, templates.size());
    }

    private UUID extractUUID(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        if (field != null && !field.isNull()) {
            return UUID.fromString(field.asText());
        }
        return null;
    }

    private String extractText(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        if (field != null && !field.isNull()) {
            return field.asText();
        }
        return null;
    }
}
