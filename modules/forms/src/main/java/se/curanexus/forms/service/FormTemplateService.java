package se.curanexus.forms.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.curanexus.forms.api.dto.*;
import se.curanexus.forms.domain.*;
import se.curanexus.forms.repository.FormFieldRepository;
import se.curanexus.forms.repository.FormTemplateRepository;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class FormTemplateService {

    private final FormTemplateRepository templateRepository;
    private final FormFieldRepository fieldRepository;

    public FormTemplateService(FormTemplateRepository templateRepository,
                                FormFieldRepository fieldRepository) {
        this.templateRepository = templateRepository;
        this.fieldRepository = fieldRepository;
    }

    @Transactional(readOnly = true)
    public FormTemplateDto getTemplate(UUID id) {
        FormTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new FormTemplateNotFoundException(id));
        return FormTemplateDto.from(template);
    }

    @Transactional(readOnly = true)
    public FormTemplateDto getActiveTemplateByCode(String code) {
        FormTemplate template = templateRepository.findActiveByCode(code)
                .orElseThrow(() -> new FormTemplateNotFoundException(code));
        return FormTemplateDto.from(template);
    }

    @Transactional(readOnly = true)
    public FormTemplateDto getTemplateByCodeAndVersion(String code, Integer version) {
        FormTemplate template = templateRepository.findByCodeAndVersion(code, version)
                .orElseThrow(() -> new FormTemplateNotFoundException(code, version));
        return FormTemplateDto.from(template);
    }

    @Transactional(readOnly = true)
    public Page<FormTemplateSummaryDto> searchTemplates(FormType type, String category,
                                                         FormStatus status, UUID ownerUnitId,
                                                         String search, Pageable pageable) {
        FormStatus searchStatus = status != null ? status : FormStatus.ACTIVE;
        String searchPattern = search != null ? "%" + search.toLowerCase() + "%" : null;

        return templateRepository.searchTemplates(searchStatus, type, category, pageable)
                .map(FormTemplateSummaryDto::from);
    }

    @Transactional(readOnly = true)
    public List<FormTemplateSummaryDto> getActiveTemplates() {
        return templateRepository.findByStatus(FormStatus.ACTIVE).stream()
                .map(FormTemplateSummaryDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FormTemplateSummaryDto> getTemplatesByType(FormType type) {
        return templateRepository.findByTypeAndStatus(type, FormStatus.ACTIVE).stream()
                .map(FormTemplateSummaryDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<String> getAllCategories() {
        return templateRepository.findAllCategories();
    }

    public FormTemplateDto createTemplate(CreateFormTemplateRequest request) {
        if (templateRepository.existsByCode(request.code())) {
            throw new IllegalArgumentException("Template code already exists: " + request.code());
        }

        FormTemplate template = new FormTemplate(request.code(), request.name(), request.type());
        template.setDescription(request.description());
        template.setCategory(request.category());
        template.setEstimatedDurationMinutes(request.estimatedDurationMinutes());
        template.setInstructions(request.instructions());
        template.setScoringFormula(request.scoringFormula());
        template.setOwnerUnitId(request.ownerUnitId());
        template.setCreatedBy(request.createdBy());

        if (request.fields() != null) {
            int sortOrder = 1;
            for (CreateFormFieldRequest fieldRequest : request.fields()) {
                FormField field = createFieldFromRequest(fieldRequest, sortOrder++);
                template.addField(field);
            }
        }

        FormTemplate saved = templateRepository.save(template);
        return FormTemplateDto.from(saved);
    }

    public FormTemplateDto updateTemplate(UUID id, UpdateFormTemplateRequest request) {
        FormTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new FormTemplateNotFoundException(id));

        if (template.getStatus() != FormStatus.DRAFT) {
            throw new IllegalStateException("Can only update templates in DRAFT status");
        }

        if (request.name() != null) {
            template.setName(request.name());
        }
        if (request.description() != null) {
            template.setDescription(request.description());
        }
        if (request.category() != null) {
            template.setCategory(request.category());
        }
        if (request.estimatedDurationMinutes() != null) {
            template.setEstimatedDurationMinutes(request.estimatedDurationMinutes());
        }
        if (request.instructions() != null) {
            template.setInstructions(request.instructions());
        }
        if (request.scoringFormula() != null) {
            template.setScoringFormula(request.scoringFormula());
        }

        FormTemplate saved = templateRepository.save(template);
        return FormTemplateDto.from(saved);
    }

    public FormTemplateDto addField(UUID templateId, CreateFormFieldRequest request) {
        FormTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new FormTemplateNotFoundException(templateId));

        if (template.getStatus() != FormStatus.DRAFT) {
            throw new IllegalStateException("Can only add fields to templates in DRAFT status");
        }

        int nextSortOrder = template.getFields().size() + 1;
        FormField field = createFieldFromRequest(request, nextSortOrder);
        template.addField(field);

        FormTemplate saved = templateRepository.save(template);
        return FormTemplateDto.from(saved);
    }

    public FormTemplateDto updateField(UUID templateId, UUID fieldId, CreateFormFieldRequest request) {
        FormTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new FormTemplateNotFoundException(templateId));

        if (template.getStatus() != FormStatus.DRAFT) {
            throw new IllegalStateException("Can only update fields in templates with DRAFT status");
        }

        FormField field = template.getFields().stream()
                .filter(f -> f.getId().equals(fieldId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Field not found: " + fieldId));

        updateFieldFromRequest(field, request);

        FormTemplate saved = templateRepository.save(template);
        return FormTemplateDto.from(saved);
    }

    public FormTemplateDto removeField(UUID templateId, UUID fieldId) {
        FormTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new FormTemplateNotFoundException(templateId));

        if (template.getStatus() != FormStatus.DRAFT) {
            throw new IllegalStateException("Can only remove fields from templates with DRAFT status");
        }

        FormField field = template.getFields().stream()
                .filter(f -> f.getId().equals(fieldId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Field not found: " + fieldId));

        template.removeField(field);

        FormTemplate saved = templateRepository.save(template);
        return FormTemplateDto.from(saved);
    }

    public FormTemplateDto publishTemplate(UUID id) {
        FormTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new FormTemplateNotFoundException(id));

        if (template.getStatus() != FormStatus.DRAFT) {
            throw new IllegalStateException("Can only publish templates in draft status");
        }

        if (template.getFields().isEmpty()) {
            throw new IllegalStateException("Cannot publish template without fields");
        }

        // Deprecate any existing active version
        templateRepository.findActiveByCode(template.getCode()).ifPresent(existing -> {
            if (!existing.getId().equals(template.getId())) {
                existing.deprecate();
                templateRepository.save(existing);
            }
        });

        template.publish();
        FormTemplate saved = templateRepository.save(template);
        return FormTemplateDto.from(saved);
    }

    public FormTemplateDto deprecateTemplate(UUID id) {
        FormTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new FormTemplateNotFoundException(id));

        template.deprecate();
        FormTemplate saved = templateRepository.save(template);
        return FormTemplateDto.from(saved);
    }

    public FormTemplateDto archiveTemplate(UUID id) {
        FormTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new FormTemplateNotFoundException(id));

        template.archive();
        FormTemplate saved = templateRepository.save(template);
        return FormTemplateDto.from(saved);
    }

    public FormTemplateDto createNewVersion(UUID id) {
        FormTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new FormTemplateNotFoundException(id));

        if (template.getStatus() == FormStatus.DRAFT) {
            throw new IllegalStateException("Cannot create new version from DRAFT template");
        }

        FormTemplate newVersion = template.createNewVersion();
        FormTemplate saved = templateRepository.save(newVersion);
        return FormTemplateDto.from(saved);
    }

    public void deleteTemplate(UUID id) {
        FormTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new FormTemplateNotFoundException(id));

        if (template.getStatus() != FormStatus.DRAFT) {
            throw new IllegalStateException("Can only delete templates in draft status");
        }

        templateRepository.delete(template);
    }

    private FormField createFieldFromRequest(CreateFormFieldRequest request, int sortOrder) {
        FormField field = new FormField(request.fieldKey(), request.fieldType(), request.label());
        field.setDescription(request.description());
        field.setPlaceholder(request.placeholder());
        field.setHelpText(request.helpText());
        field.setSortOrder(request.sortOrder() != null ? request.sortOrder() : sortOrder);
        field.setRequired(request.required() != null ? request.required() : false);
        field.setDefaultValue(request.defaultValue());
        field.setOptions(request.options());
        field.setValidationRules(request.validationRules());
        field.setConditionalRules(request.conditionalRules());
        field.setMinValue(request.minValue());
        field.setMaxValue(request.maxValue());
        field.setStepValue(request.stepValue());
        field.setScaleLabels(request.scaleLabels());
        field.setCodeSystem(request.codeSystem());
        field.setCode(request.code());
        field.setUnit(request.unit());
        return field;
    }

    private void updateFieldFromRequest(FormField field, CreateFormFieldRequest request) {
        field.setFieldKey(request.fieldKey());
        field.setFieldType(request.fieldType());
        field.setLabel(request.label());
        field.setDescription(request.description());
        field.setPlaceholder(request.placeholder());
        field.setHelpText(request.helpText());
        if (request.sortOrder() != null) {
            field.setSortOrder(request.sortOrder());
        }
        field.setRequired(request.required() != null ? request.required() : false);
        field.setDefaultValue(request.defaultValue());
        field.setOptions(request.options());
        field.setValidationRules(request.validationRules());
        field.setConditionalRules(request.conditionalRules());
        field.setMinValue(request.minValue());
        field.setMaxValue(request.maxValue());
        field.setStepValue(request.stepValue());
        field.setScaleLabels(request.scaleLabels());
        field.setCodeSystem(request.codeSystem());
        field.setCode(request.code());
        field.setUnit(request.unit());
    }
}
