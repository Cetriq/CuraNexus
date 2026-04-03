package se.curanexus.task.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Template for automatic task creation based on triggers.
 * Templates define what tasks should be created when specific events occur.
 */
@Entity
@Table(name = "task_templates")
public class TaskTemplate {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TaskCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_priority", nullable = false, length = 20)
    private TaskPriority defaultPriority;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", nullable = false, length = 50)
    private TriggerType triggerType;

    @Column(name = "trigger_value", length = 100)
    private String triggerValue;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "sort_order")
    private int sortOrder = 0;

    @Column(name = "depends_on_template", length = 100)
    private String dependsOnTemplate;

    @Column(name = "due_offset_minutes")
    private Integer dueOffsetMinutes;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public TaskTemplate() {
    }

    public TaskTemplate(String name, String title, TaskCategory category, TaskPriority defaultPriority,
                        TriggerType triggerType, String triggerValue) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.title = title;
        this.category = category;
        this.defaultPriority = defaultPriority;
        this.triggerType = triggerType;
        this.triggerValue = triggerValue;
        this.active = true;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.updatedAt = Instant.now();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        this.updatedAt = Instant.now();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = Instant.now();
    }

    public TaskCategory getCategory() {
        return category;
    }

    public void setCategory(TaskCategory category) {
        this.category = category;
        this.updatedAt = Instant.now();
    }

    public TaskPriority getDefaultPriority() {
        return defaultPriority;
    }

    public void setDefaultPriority(TaskPriority defaultPriority) {
        this.defaultPriority = defaultPriority;
        this.updatedAt = Instant.now();
    }

    public TriggerType getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(TriggerType triggerType) {
        this.triggerType = triggerType;
        this.updatedAt = Instant.now();
    }

    public String getTriggerValue() {
        return triggerValue;
    }

    public void setTriggerValue(String triggerValue) {
        this.triggerValue = triggerValue;
        this.updatedAt = Instant.now();
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
        this.updatedAt = Instant.now();
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
        this.updatedAt = Instant.now();
    }

    public String getDependsOnTemplate() {
        return dependsOnTemplate;
    }

    public void setDependsOnTemplate(String dependsOnTemplate) {
        this.dependsOnTemplate = dependsOnTemplate;
        this.updatedAt = Instant.now();
    }

    public Integer getDueOffsetMinutes() {
        return dueOffsetMinutes;
    }

    public void setDueOffsetMinutes(Integer dueOffsetMinutes) {
        this.dueOffsetMinutes = dueOffsetMinutes;
        this.updatedAt = Instant.now();
    }

    public boolean hasDependency() {
        return dependsOnTemplate != null && !dependsOnTemplate.isEmpty();
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
