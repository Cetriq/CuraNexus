package se.curanexus.task.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.curanexus.task.domain.TaskTemplate;
import se.curanexus.task.domain.TriggerType;
import se.curanexus.task.repository.TaskTemplateRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing task templates.
 */
@Service
public class TaskTemplateService {

    private static final Logger log = LoggerFactory.getLogger(TaskTemplateService.class);

    private final TaskTemplateRepository repository;

    public TaskTemplateService(TaskTemplateRepository repository) {
        this.repository = repository;
    }

    /**
     * Get all active templates that should be applied for a given encounter class.
     * This includes templates with ALWAYS trigger and templates matching the encounter class.
     */
    @Transactional(readOnly = true)
    public List<TaskTemplate> getTemplatesForEncounterClass(String encounterClass) {
        log.debug("Finding templates for encounter class: {}", encounterClass);
        return repository.findMatchingTemplates(TriggerType.ENCOUNTER_CLASS, encounterClass);
    }

    /**
     * Get all active templates.
     */
    @Transactional(readOnly = true)
    public List<TaskTemplate> getAllActiveTemplates() {
        return repository.findByActiveTrueOrderBySortOrderAsc();
    }

    /**
     * Get a template by ID.
     */
    @Transactional(readOnly = true)
    public Optional<TaskTemplate> getTemplate(UUID id) {
        return repository.findById(id);
    }

    /**
     * Get a template by name.
     */
    @Transactional(readOnly = true)
    public Optional<TaskTemplate> getTemplateByName(String name) {
        return repository.findByName(name);
    }

    /**
     * Create a new template.
     */
    @Transactional
    public TaskTemplate createTemplate(TaskTemplate template) {
        log.info("Creating task template: {}", template.getName());
        return repository.save(template);
    }

    /**
     * Update an existing template.
     */
    @Transactional
    public TaskTemplate updateTemplate(TaskTemplate template) {
        log.info("Updating task template: {}", template.getName());
        return repository.save(template);
    }

    /**
     * Activate a template.
     */
    @Transactional
    public void activateTemplate(UUID id) {
        repository.findById(id).ifPresent(template -> {
            template.setActive(true);
            repository.save(template);
            log.info("Activated template: {}", template.getName());
        });
    }

    /**
     * Deactivate a template.
     */
    @Transactional
    public void deactivateTemplate(UUID id) {
        repository.findById(id).ifPresent(template -> {
            template.setActive(false);
            repository.save(template);
            log.info("Deactivated template: {}", template.getName());
        });
    }

    /**
     * Delete a template.
     */
    @Transactional
    public void deleteTemplate(UUID id) {
        repository.findById(id).ifPresent(template -> {
            repository.delete(template);
            log.info("Deleted template: {}", template.getName());
        });
    }
}
