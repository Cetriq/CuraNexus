package se.curanexus.forms.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.curanexus.forms.api.dto.*;
import se.curanexus.forms.domain.FormStatus;
import se.curanexus.forms.domain.FormType;
import se.curanexus.forms.service.FormTemplateService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/forms/templates")
@Tag(name = "Form Templates", description = "API för formulärmallar")
public class FormTemplateController {

    private final FormTemplateService formTemplateService;

    public FormTemplateController(FormTemplateService formTemplateService) {
        this.formTemplateService = formTemplateService;
    }

    @PostMapping
    @Operation(summary = "Skapa ny formulärmall")
    public ResponseEntity<FormTemplateDto> createTemplate(
            @Valid @RequestBody CreateFormTemplateRequest request) {
        FormTemplateDto created = formTemplateService.createTemplate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Hämta formulärmall med ID")
    public FormTemplateDto getTemplate(@PathVariable UUID id) {
        return formTemplateService.getTemplate(id);
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Hämta aktiv formulärmall med kod")
    public FormTemplateDto getTemplateByCode(@PathVariable String code) {
        return formTemplateService.getActiveTemplateByCode(code);
    }

    @GetMapping("/code/{code}/version/{version}")
    @Operation(summary = "Hämta specifik version av formulärmall")
    public FormTemplateDto getTemplateByCodeAndVersion(
            @PathVariable String code,
            @PathVariable Integer version) {
        return formTemplateService.getTemplateByCodeAndVersion(code, version);
    }

    @GetMapping
    @Operation(summary = "Sök formulärmallar")
    public Page<FormTemplateSummaryDto> searchTemplates(
            @RequestParam(required = false) FormType type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) FormStatus status,
            @RequestParam(required = false) UUID ownerUnitId,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        return formTemplateService.searchTemplates(type, category, status, ownerUnitId, search, pageable);
    }

    @GetMapping("/active")
    @Operation(summary = "Hämta alla aktiva formulärmallar")
    public List<FormTemplateSummaryDto> getActiveTemplates() {
        return formTemplateService.getActiveTemplates();
    }

    @GetMapping("/categories")
    @Operation(summary = "Hämta alla kategorier")
    public List<String> getCategories() {
        return formTemplateService.getAllCategories();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Uppdatera formulärmall")
    public FormTemplateDto updateTemplate(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateFormTemplateRequest request) {
        return formTemplateService.updateTemplate(id, request);
    }

    @PostMapping("/{id}/fields")
    @Operation(summary = "Lägg till fält i formulärmall")
    public FormTemplateDto addField(
            @PathVariable UUID id,
            @Valid @RequestBody CreateFormFieldRequest request) {
        return formTemplateService.addField(id, request);
    }

    @PutMapping("/{id}/fields/{fieldId}")
    @Operation(summary = "Uppdatera fält i formulärmall")
    public FormTemplateDto updateField(
            @PathVariable UUID id,
            @PathVariable UUID fieldId,
            @Valid @RequestBody CreateFormFieldRequest request) {
        return formTemplateService.updateField(id, fieldId, request);
    }

    @DeleteMapping("/{id}/fields/{fieldId}")
    @Operation(summary = "Ta bort fält från formulärmall")
    public FormTemplateDto removeField(
            @PathVariable UUID id,
            @PathVariable UUID fieldId) {
        return formTemplateService.removeField(id, fieldId);
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "Publicera formulärmall")
    public FormTemplateDto publishTemplate(@PathVariable UUID id) {
        return formTemplateService.publishTemplate(id);
    }

    @PostMapping("/{id}/deprecate")
    @Operation(summary = "Markera formulärmall som föråldrad")
    public FormTemplateDto deprecateTemplate(@PathVariable UUID id) {
        return formTemplateService.deprecateTemplate(id);
    }

    @PostMapping("/{id}/archive")
    @Operation(summary = "Arkivera formulärmall")
    public FormTemplateDto archiveTemplate(@PathVariable UUID id) {
        return formTemplateService.archiveTemplate(id);
    }

    @PostMapping("/{id}/new-version")
    @Operation(summary = "Skapa ny version av formulärmall")
    public FormTemplateDto createNewVersion(@PathVariable UUID id) {
        return formTemplateService.createNewVersion(id);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Ta bort formulärmall (endast utkast)")
    public ResponseEntity<Void> deleteTemplate(@PathVariable UUID id) {
        formTemplateService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }
}
