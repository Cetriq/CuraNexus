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
import se.curanexus.forms.domain.SubmissionStatus;
import se.curanexus.forms.service.FormSubmissionService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/forms/submissions")
@Tag(name = "Form Submissions", description = "API för formulärinlämningar")
public class FormSubmissionController {

    private final FormSubmissionService formSubmissionService;

    public FormSubmissionController(FormSubmissionService formSubmissionService) {
        this.formSubmissionService = formSubmissionService;
    }

    @PostMapping
    @Operation(summary = "Starta ny formulärinlämning")
    public ResponseEntity<FormSubmissionDto> startSubmission(
            @Valid @RequestBody StartSubmissionRequest request) {
        FormSubmissionDto created = formSubmissionService.startSubmission(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Hämta formulärinlämning")
    public FormSubmissionDto getSubmission(@PathVariable UUID id) {
        return formSubmissionService.getSubmission(id);
    }

    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Hämta inlämningar för patient")
    public List<FormSubmissionSummaryDto> getPatientSubmissions(
            @PathVariable UUID patientId,
            @RequestParam(required = false) SubmissionStatus status) {
        return formSubmissionService.getPatientSubmissions(patientId, status);
    }

    @GetMapping("/encounter/{encounterId}")
    @Operation(summary = "Hämta inlämningar för vårdkontakt")
    public List<FormSubmissionSummaryDto> getEncounterSubmissions(
            @PathVariable UUID encounterId) {
        return formSubmissionService.getEncounterSubmissions(encounterId);
    }

    @GetMapping
    @Operation(summary = "Sök inlämningar")
    public Page<FormSubmissionSummaryDto> searchSubmissions(
            @RequestParam(required = false) UUID patientId,
            @RequestParam(required = false) UUID encounterId,
            @RequestParam(required = false) String templateCode,
            @RequestParam(required = false) SubmissionStatus status,
            Pageable pageable) {
        return formSubmissionService.searchSubmissions(patientId, encounterId, templateCode, status, pageable);
    }

    @GetMapping("/pending-review")
    @Operation(summary = "Hämta inlämningar som väntar på granskning")
    public List<FormSubmissionSummaryDto> getPendingReviewSubmissions() {
        return formSubmissionService.getPendingReviewSubmissions();
    }

    @PutMapping("/{id}/answers")
    @Operation(summary = "Spara svar")
    public FormSubmissionDto saveAnswers(
            @PathVariable UUID id,
            @Valid @RequestBody SaveAnswersRequest request) {
        return formSubmissionService.saveAnswers(id, request);
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Slutför inlämning")
    public FormSubmissionDto completeSubmission(@PathVariable UUID id) {
        return formSubmissionService.completeSubmission(id);
    }

    @PostMapping("/{id}/review")
    @Operation(summary = "Granska inlämning")
    public FormSubmissionDto reviewSubmission(
            @PathVariable UUID id,
            @Valid @RequestBody ReviewSubmissionRequest request) {
        return formSubmissionService.reviewSubmission(id, request);
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Avbryt inlämning")
    public FormSubmissionDto cancelSubmission(@PathVariable UUID id) {
        return formSubmissionService.cancelSubmission(id);
    }

    @GetMapping("/{id}/score")
    @Operation(summary = "Beräkna poäng för inlämning")
    public ResponseEntity<Double> calculateScore(@PathVariable UUID id) {
        Double score = formSubmissionService.calculateScore(id);
        return ResponseEntity.ok(score);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Ta bort inlämning (endast ej slutförda)")
    public ResponseEntity<Void> deleteSubmission(@PathVariable UUID id) {
        formSubmissionService.deleteSubmission(id);
        return ResponseEntity.noContent().build();
    }
}
