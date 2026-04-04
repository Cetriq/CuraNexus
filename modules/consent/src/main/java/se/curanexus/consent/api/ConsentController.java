package se.curanexus.consent.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.curanexus.consent.api.dto.*;
import se.curanexus.consent.domain.ConsentStatus;
import se.curanexus.consent.domain.ConsentType;
import se.curanexus.consent.service.ConsentService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/consents")
@Tag(name = "Consents", description = "Patient consent management")
public class ConsentController {

    private final ConsentService consentService;

    public ConsentController(ConsentService consentService) {
        this.consentService = consentService;
    }

    @PostMapping
    @Operation(summary = "Create a new consent record")
    public ResponseEntity<ConsentDto> createConsent(@Valid @RequestBody CreateConsentRequest request) {
        ConsentDto consent = consentService.createConsent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(consent);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get consent by ID")
    public ResponseEntity<ConsentDto> getConsent(@PathVariable UUID id) {
        ConsentDto consent = consentService.getConsent(id);
        return ResponseEntity.ok(consent);
    }

    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Get all consents for a patient")
    public ResponseEntity<List<ConsentSummaryDto>> getPatientConsents(@PathVariable UUID patientId) {
        List<ConsentSummaryDto> consents = consentService.getPatientConsents(patientId);
        return ResponseEntity.ok(consents);
    }

    @GetMapping("/patient/{patientId}/status/{status}")
    @Operation(summary = "Get patient consents by status")
    public ResponseEntity<List<ConsentSummaryDto>> getPatientConsentsByStatus(
            @PathVariable UUID patientId,
            @PathVariable ConsentStatus status) {
        List<ConsentSummaryDto> consents = consentService.getPatientConsentsByStatus(patientId, status);
        return ResponseEntity.ok(consents);
    }

    @GetMapping("/patient/{patientId}/type/{type}")
    @Operation(summary = "Get patient consents by type")
    public ResponseEntity<List<ConsentSummaryDto>> getPatientConsentsByType(
            @PathVariable UUID patientId,
            @PathVariable ConsentType type) {
        List<ConsentSummaryDto> consents = consentService.getPatientConsentsByType(patientId, type);
        return ResponseEntity.ok(consents);
    }

    @GetMapping("/patient/{patientId}/active")
    @Operation(summary = "Get active consents for a patient")
    public ResponseEntity<List<ConsentSummaryDto>> getActiveConsents(@PathVariable UUID patientId) {
        List<ConsentSummaryDto> consents = consentService.getActiveConsentsForPatient(patientId);
        return ResponseEntity.ok(consents);
    }

    @GetMapping("/patient/{patientId}/type/{type}/active")
    @Operation(summary = "Check if patient has active consent of specific type")
    public ResponseEntity<Boolean> hasActiveConsent(
            @PathVariable UUID patientId,
            @PathVariable ConsentType type) {
        boolean hasConsent = consentService.hasActiveConsent(patientId, type);
        return ResponseEntity.ok(hasConsent);
    }

    @GetMapping("/unit/{unitId}")
    @Operation(summary = "Get consents managed by a unit")
    public ResponseEntity<List<ConsentSummaryDto>> getConsentsForUnit(@PathVariable UUID unitId) {
        List<ConsentSummaryDto> consents = consentService.getConsentsForUnit(unitId);
        return ResponseEntity.ok(consents);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update consent")
    public ResponseEntity<ConsentDto> updateConsent(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateConsentRequest request) {
        ConsentDto consent = consentService.updateConsent(id, request);
        return ResponseEntity.ok(consent);
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate a pending consent")
    public ResponseEntity<ConsentDto> activateConsent(
            @PathVariable UUID id,
            @Valid @RequestBody ActivateConsentRequest request) {
        ConsentDto consent = consentService.activateConsent(id, request);
        return ResponseEntity.ok(consent);
    }

    @PostMapping("/{id}/withdraw")
    @Operation(summary = "Withdraw an active consent")
    public ResponseEntity<ConsentDto> withdrawConsent(
            @PathVariable UUID id,
            @Valid @RequestBody WithdrawConsentRequest request) {
        ConsentDto consent = consentService.withdrawConsent(id, request);
        return ResponseEntity.ok(consent);
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject a pending consent")
    public ResponseEntity<ConsentDto> rejectConsent(@PathVariable UUID id) {
        ConsentDto consent = consentService.rejectConsent(id);
        return ResponseEntity.ok(consent);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a consent (only non-active)")
    public ResponseEntity<Void> deleteConsent(@PathVariable UUID id) {
        consentService.deleteConsent(id);
        return ResponseEntity.noContent().build();
    }
}
