package se.curanexus.encounter.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.curanexus.encounter.api.dto.*;
import se.curanexus.encounter.domain.EncounterClass;
import se.curanexus.encounter.domain.EncounterStatus;
import se.curanexus.encounter.service.EncounterService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "encounters", description = "Care encounter management")
public class EncounterController {

    private final EncounterService encounterService;

    public EncounterController(EncounterService encounterService) {
        this.encounterService = encounterService;
    }

    @GetMapping("/encounters")
    @Operation(summary = "Search encounters")
    public Page<EncounterSummaryDto> searchEncounters(
            @RequestParam(required = false) UUID patientId,
            @RequestParam(required = false) EncounterStatus status,
            @RequestParam(name = "class", required = false) EncounterClass encounterClass,
            @RequestParam(required = false) UUID responsibleUnitId,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            Pageable pageable) {
        return encounterService.searchEncounters(
                patientId, status, encounterClass, responsibleUnitId, fromDate, toDate, pageable);
    }

    @PostMapping("/encounters")
    @Operation(summary = "Create a new encounter")
    public ResponseEntity<EncounterDto> createEncounter(@Valid @RequestBody CreateEncounterRequest request) {
        EncounterDto created = encounterService.createEncounter(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/encounters/{encounterId}")
    @Operation(summary = "Get encounter by ID")
    public EncounterDto getEncounter(@PathVariable UUID encounterId) {
        return encounterService.getEncounter(encounterId);
    }

    @PutMapping("/encounters/{encounterId}")
    @Operation(summary = "Update encounter")
    public EncounterDto updateEncounter(
            @PathVariable UUID encounterId,
            @Valid @RequestBody UpdateEncounterRequest request) {
        return encounterService.updateEncounter(encounterId, request);
    }

    @PatchMapping("/encounters/{encounterId}/status")
    @Operation(summary = "Update encounter status")
    public EncounterDto updateEncounterStatus(
            @PathVariable UUID encounterId,
            @Valid @RequestBody UpdateStatusRequest request) {
        return encounterService.updateEncounterStatus(encounterId, request);
    }

    // Patient encounters endpoint

    @GetMapping("/patients/{patientId}/encounters")
    @Operation(summary = "Get patient encounters")
    public Page<EncounterSummaryDto> getPatientEncounters(
            @PathVariable UUID patientId,
            @RequestParam(required = false) EncounterStatus status,
            Pageable pageable) {
        return encounterService.getPatientEncounters(patientId, status, pageable);
    }

    // Participant endpoints

    @GetMapping("/encounters/{encounterId}/participants")
    @Operation(summary = "Get encounter participants")
    @Tag(name = "participants")
    public List<ParticipantDto> getParticipants(@PathVariable UUID encounterId) {
        return encounterService.getParticipants(encounterId);
    }

    @PostMapping("/encounters/{encounterId}/participants")
    @Operation(summary = "Add participant to encounter")
    @Tag(name = "participants")
    public ResponseEntity<ParticipantDto> addParticipant(
            @PathVariable UUID encounterId,
            @Valid @RequestBody AddParticipantRequest request) {
        ParticipantDto created = encounterService.addParticipant(encounterId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/encounters/{encounterId}/participants/{participantId}")
    @Operation(summary = "Update participant")
    @Tag(name = "participants")
    public ParticipantDto updateParticipant(
            @PathVariable UUID encounterId,
            @PathVariable UUID participantId,
            @Valid @RequestBody UpdateParticipantRequest request) {
        return encounterService.updateParticipant(encounterId, participantId, request);
    }

    @DeleteMapping("/encounters/{encounterId}/participants/{participantId}")
    @Operation(summary = "Remove participant")
    @Tag(name = "participants")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeParticipant(
            @PathVariable UUID encounterId,
            @PathVariable UUID participantId) {
        encounterService.removeParticipant(encounterId, participantId);
    }

    // Reason endpoints

    @GetMapping("/encounters/{encounterId}/reasons")
    @Operation(summary = "Get reasons for encounter")
    @Tag(name = "reasons")
    public List<EncounterReasonDto> getReasons(@PathVariable UUID encounterId) {
        return encounterService.getReasons(encounterId);
    }

    @PostMapping("/encounters/{encounterId}/reasons")
    @Operation(summary = "Add reason for encounter")
    @Tag(name = "reasons")
    public ResponseEntity<EncounterReasonDto> addReason(
            @PathVariable UUID encounterId,
            @Valid @RequestBody AddReasonRequest request) {
        EncounterReasonDto created = encounterService.addReason(encounterId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/encounters/{encounterId}/reasons/{reasonId}")
    @Operation(summary = "Remove reason")
    @Tag(name = "reasons")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeReason(
            @PathVariable UUID encounterId,
            @PathVariable UUID reasonId) {
        encounterService.removeReason(encounterId, reasonId);
    }
}
