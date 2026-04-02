package se.curanexus.patient.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.curanexus.patient.api.dto.*;
import se.curanexus.patient.service.PatientService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/patients")
@Tag(name = "patients", description = "Patient identity management")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    @Operation(summary = "Search patients")
    public Page<PatientSummaryDto> searchPatients(
            @RequestParam(required = false) String personalIdentityNumber,
            @RequestParam(required = false) String name,
            Pageable pageable) {
        return patientService.searchPatients(personalIdentityNumber, name, pageable);
    }

    @GetMapping("/{patientId}")
    @Operation(summary = "Get patient by ID")
    public PatientDto getPatient(@PathVariable UUID patientId) {
        return patientService.getPatient(patientId);
    }

    @PostMapping
    @Operation(summary = "Register a new patient")
    public ResponseEntity<PatientDto> createPatient(@Valid @RequestBody CreatePatientRequest request) {
        PatientDto created = patientService.createPatient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{patientId}")
    @Operation(summary = "Update patient")
    public PatientDto updatePatient(
            @PathVariable UUID patientId,
            @Valid @RequestBody UpdatePatientRequest request) {
        return patientService.updatePatient(patientId, request);
    }

    // Contact endpoints

    @GetMapping("/{patientId}/contacts")
    @Operation(summary = "Get patient contact information")
    @Tag(name = "contacts")
    public List<ContactInfoDto> getPatientContacts(@PathVariable UUID patientId) {
        return patientService.getPatientContacts(patientId);
    }

    @PostMapping("/{patientId}/contacts")
    @Operation(summary = "Add contact information")
    @Tag(name = "contacts")
    public ResponseEntity<ContactInfoDto> addPatientContact(
            @PathVariable UUID patientId,
            @Valid @RequestBody CreateContactRequest request) {
        ContactInfoDto created = patientService.addPatientContact(patientId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{patientId}/contacts/{contactId}")
    @Operation(summary = "Update contact information")
    @Tag(name = "contacts")
    public ContactInfoDto updatePatientContact(
            @PathVariable UUID patientId,
            @PathVariable UUID contactId,
            @Valid @RequestBody UpdateContactRequest request) {
        return patientService.updatePatientContact(patientId, contactId, request);
    }

    @DeleteMapping("/{patientId}/contacts/{contactId}")
    @Operation(summary = "Remove contact information")
    @Tag(name = "contacts")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePatientContact(
            @PathVariable UUID patientId,
            @PathVariable UUID contactId) {
        patientService.deletePatientContact(patientId, contactId);
    }

    // Related persons endpoints

    @GetMapping("/{patientId}/related-persons")
    @Operation(summary = "Get related persons")
    @Tag(name = "related-persons")
    public List<RelatedPersonDto> getRelatedPersons(@PathVariable UUID patientId) {
        return patientService.getRelatedPersons(patientId);
    }

    @PostMapping("/{patientId}/related-persons")
    @Operation(summary = "Add related person")
    @Tag(name = "related-persons")
    public ResponseEntity<RelatedPersonDto> addRelatedPerson(
            @PathVariable UUID patientId,
            @Valid @RequestBody CreateRelatedPersonRequest request) {
        RelatedPersonDto created = patientService.addRelatedPerson(patientId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{patientId}/related-persons/{relatedPersonId}")
    @Operation(summary = "Update related person")
    @Tag(name = "related-persons")
    public RelatedPersonDto updateRelatedPerson(
            @PathVariable UUID patientId,
            @PathVariable UUID relatedPersonId,
            @Valid @RequestBody UpdateRelatedPersonRequest request) {
        return patientService.updateRelatedPerson(patientId, relatedPersonId, request);
    }

    @DeleteMapping("/{patientId}/related-persons/{relatedPersonId}")
    @Operation(summary = "Remove related person")
    @Tag(name = "related-persons")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRelatedPerson(
            @PathVariable UUID patientId,
            @PathVariable UUID relatedPersonId) {
        patientService.deleteRelatedPerson(patientId, relatedPersonId);
    }

    // Consent endpoints

    @GetMapping("/{patientId}/consents")
    @Operation(summary = "Get patient consents")
    @Tag(name = "consents")
    public List<ConsentDto> getPatientConsents(@PathVariable UUID patientId) {
        return patientService.getPatientConsents(patientId);
    }

    @GetMapping("/{patientId}/consents/{consentId}")
    @Operation(summary = "Get specific consent")
    @Tag(name = "consents")
    public ConsentDto getConsent(
            @PathVariable UUID patientId,
            @PathVariable UUID consentId) {
        return patientService.getConsent(patientId, consentId);
    }

    @PostMapping("/{patientId}/consents")
    @Operation(summary = "Register consent")
    @Tag(name = "consents")
    public ResponseEntity<ConsentDto> registerConsent(
            @PathVariable UUID patientId,
            @Valid @RequestBody CreateConsentRequest request) {
        ConsentDto created = patientService.registerConsent(patientId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{patientId}/consents/{consentId}")
    @Operation(summary = "Revoke consent")
    @Tag(name = "consents")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokeConsent(
            @PathVariable UUID patientId,
            @PathVariable UUID consentId) {
        patientService.revokeConsent(patientId, consentId);
    }
}
