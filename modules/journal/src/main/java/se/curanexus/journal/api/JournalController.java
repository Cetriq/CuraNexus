package se.curanexus.journal.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.curanexus.journal.api.dto.*;
import se.curanexus.journal.domain.*;
import se.curanexus.journal.service.JournalService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/journal")
public class JournalController {

    private final JournalService journalService;

    public JournalController(JournalService journalService) {
        this.journalService = journalService;
    }

    // === Clinical Notes ===

    @PostMapping("/notes")
    public ResponseEntity<NoteResponse> createNote(@Valid @RequestBody CreateNoteRequest request) {
        ClinicalNote note = journalService.createNote(
                request.encounterId(),
                request.patientId(),
                request.type(),
                request.authorId(),
                request.authorName(),
                request.title(),
                request.content()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(NoteResponse.from(note));
    }

    @GetMapping("/notes/{noteId}")
    public ResponseEntity<NoteResponse> getNote(@PathVariable UUID noteId) {
        ClinicalNote note = journalService.getNote(noteId);
        return ResponseEntity.ok(NoteResponse.from(note));
    }

    @GetMapping("/encounters/{encounterId}/notes")
    public ResponseEntity<List<NoteResponse>> getNotesByEncounter(@PathVariable UUID encounterId) {
        List<NoteResponse> notes = journalService.getNotesByEncounter(encounterId).stream()
                .map(NoteResponse::from)
                .toList();
        return ResponseEntity.ok(notes);
    }

    @GetMapping("/patients/{patientId}/notes")
    public ResponseEntity<List<NoteResponse>> getNotesByPatient(@PathVariable UUID patientId) {
        List<NoteResponse> notes = journalService.getNotesByPatient(patientId).stream()
                .map(NoteResponse::from)
                .toList();
        return ResponseEntity.ok(notes);
    }

    @PutMapping("/notes/{noteId}")
    public ResponseEntity<NoteResponse> updateNote(@PathVariable UUID noteId,
                                                    @Valid @RequestBody UpdateNoteRequest request) {
        ClinicalNote note = journalService.updateNote(noteId, request.title(), request.content());
        return ResponseEntity.ok(NoteResponse.from(note));
    }

    @PostMapping("/notes/{noteId}/sign")
    public ResponseEntity<NoteResponse> signNote(@PathVariable UUID noteId,
                                                  @Valid @RequestBody SignNoteRequest request) {
        ClinicalNote note = journalService.signNote(noteId, request.signedById(), request.signedByName());
        return ResponseEntity.ok(NoteResponse.from(note));
    }

    @PostMapping("/notes/{noteId}/amend")
    public ResponseEntity<NoteResponse> amendNote(@PathVariable UUID noteId,
                                                   @Valid @RequestBody AmendNoteRequest request) {
        ClinicalNote note = journalService.amendNote(noteId, request.newContent());
        return ResponseEntity.ok(NoteResponse.from(note));
    }

    @PostMapping("/notes/{noteId}/cancel")
    public ResponseEntity<NoteResponse> cancelNote(@PathVariable UUID noteId) {
        ClinicalNote note = journalService.cancelNote(noteId);
        return ResponseEntity.ok(NoteResponse.from(note));
    }

    // === Diagnoses ===

    @PostMapping("/diagnoses")
    public ResponseEntity<DiagnosisResponse> createDiagnosis(@Valid @RequestBody CreateDiagnosisRequest request) {
        Diagnosis diagnosis = journalService.createDiagnosis(
                request.encounterId(),
                request.patientId(),
                request.code(),
                request.displayText(),
                request.type(),
                request.recordedById()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(DiagnosisResponse.from(diagnosis));
    }

    @GetMapping("/diagnoses/{diagnosisId}")
    public ResponseEntity<DiagnosisResponse> getDiagnosis(@PathVariable UUID diagnosisId) {
        Diagnosis diagnosis = journalService.getDiagnosis(diagnosisId);
        return ResponseEntity.ok(DiagnosisResponse.from(diagnosis));
    }

    @GetMapping("/encounters/{encounterId}/diagnoses")
    public ResponseEntity<List<DiagnosisResponse>> getDiagnosesByEncounter(@PathVariable UUID encounterId) {
        List<DiagnosisResponse> diagnoses = journalService.getDiagnosesByEncounter(encounterId).stream()
                .map(DiagnosisResponse::from)
                .toList();
        return ResponseEntity.ok(diagnoses);
    }

    @GetMapping("/patients/{patientId}/diagnoses")
    public ResponseEntity<List<DiagnosisResponse>> getDiagnosesByPatient(
            @PathVariable UUID patientId,
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly) {
        List<Diagnosis> diagnoses = activeOnly
                ? journalService.getActiveDiagnosesByPatient(patientId)
                : journalService.getDiagnosesByPatient(patientId);
        return ResponseEntity.ok(diagnoses.stream().map(DiagnosisResponse::from).toList());
    }

    @PutMapping("/diagnoses/{diagnosisId}")
    public ResponseEntity<DiagnosisResponse> updateDiagnosis(@PathVariable UUID diagnosisId,
                                                              @Valid @RequestBody UpdateDiagnosisRequest request) {
        Diagnosis diagnosis = journalService.updateDiagnosis(
                diagnosisId,
                request.code(),
                request.displayText(),
                request.type(),
                request.rank()
        );
        return ResponseEntity.ok(DiagnosisResponse.from(diagnosis));
    }

    @PostMapping("/diagnoses/{diagnosisId}/resolve")
    public ResponseEntity<DiagnosisResponse> resolveDiagnosis(@PathVariable UUID diagnosisId,
                                                               @RequestParam LocalDate resolvedDate) {
        Diagnosis diagnosis = journalService.resolveDiagnosis(diagnosisId, resolvedDate);
        return ResponseEntity.ok(DiagnosisResponse.from(diagnosis));
    }

    @DeleteMapping("/diagnoses/{diagnosisId}")
    public ResponseEntity<Void> deleteDiagnosis(@PathVariable UUID diagnosisId) {
        journalService.deleteDiagnosis(diagnosisId);
        return ResponseEntity.noContent().build();
    }

    // === Procedures ===

    @PostMapping("/procedures")
    public ResponseEntity<ProcedureResponse> createProcedure(@Valid @RequestBody CreateProcedureRequest request) {
        Procedure procedure = journalService.createProcedure(
                request.encounterId(),
                request.patientId(),
                request.code(),
                request.displayText(),
                request.bodySite(),
                request.laterality()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ProcedureResponse.from(procedure));
    }

    @GetMapping("/procedures/{procedureId}")
    public ResponseEntity<ProcedureResponse> getProcedure(@PathVariable UUID procedureId) {
        Procedure procedure = journalService.getProcedure(procedureId);
        return ResponseEntity.ok(ProcedureResponse.from(procedure));
    }

    @GetMapping("/encounters/{encounterId}/procedures")
    public ResponseEntity<List<ProcedureResponse>> getProceduresByEncounter(@PathVariable UUID encounterId) {
        List<ProcedureResponse> procedures = journalService.getProceduresByEncounter(encounterId).stream()
                .map(ProcedureResponse::from)
                .toList();
        return ResponseEntity.ok(procedures);
    }

    @GetMapping("/patients/{patientId}/procedures")
    public ResponseEntity<List<ProcedureResponse>> getProceduresByPatient(@PathVariable UUID patientId) {
        List<ProcedureResponse> procedures = journalService.getProceduresByPatient(patientId).stream()
                .map(ProcedureResponse::from)
                .toList();
        return ResponseEntity.ok(procedures);
    }

    @PostMapping("/procedures/{procedureId}/start")
    public ResponseEntity<ProcedureResponse> startProcedure(@PathVariable UUID procedureId,
                                                             @Valid @RequestBody StartProcedureRequest request) {
        Procedure procedure = journalService.startProcedure(procedureId, request.performedById(), request.performedByName());
        return ResponseEntity.ok(ProcedureResponse.from(procedure));
    }

    @PostMapping("/procedures/{procedureId}/complete")
    public ResponseEntity<ProcedureResponse> completeProcedure(@PathVariable UUID procedureId,
                                                                @Valid @RequestBody CompleteProcedureRequest request) {
        Procedure procedure = journalService.completeProcedure(procedureId, request.outcome());
        return ResponseEntity.ok(ProcedureResponse.from(procedure));
    }

    @PostMapping("/procedures/{procedureId}/cancel")
    public ResponseEntity<ProcedureResponse> cancelProcedure(@PathVariable UUID procedureId) {
        Procedure procedure = journalService.cancelProcedure(procedureId);
        return ResponseEntity.ok(ProcedureResponse.from(procedure));
    }

    // === Observations ===

    @PostMapping("/observations")
    public ResponseEntity<ObservationResponse> createObservation(@Valid @RequestBody CreateObservationRequest request) {
        Observation observation;
        if (request.valueNumeric() != null) {
            observation = journalService.createNumericObservation(
                    request.patientId(),
                    request.encounterId(),
                    request.code(),
                    request.category(),
                    request.valueNumeric(),
                    request.unit(),
                    request.observedAt(),
                    request.recordedById(),
                    request.recordedByName()
            );
        } else {
            observation = journalService.createStringObservation(
                    request.patientId(),
                    request.encounterId(),
                    request.code(),
                    request.category(),
                    request.valueString(),
                    request.observedAt(),
                    request.recordedById(),
                    request.recordedByName()
            );
        }

        if (request.referenceRangeLow() != null || request.referenceRangeHigh() != null) {
            journalService.setObservationReferenceRange(
                    observation.getId(),
                    request.referenceRangeLow(),
                    request.referenceRangeHigh()
            );
        }
        if (request.interpretation() != null) {
            journalService.updateObservationInterpretation(observation.getId(), request.interpretation());
        }

        observation = journalService.getObservation(observation.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ObservationResponse.from(observation));
    }

    @GetMapping("/observations/{observationId}")
    public ResponseEntity<ObservationResponse> getObservation(@PathVariable UUID observationId) {
        Observation observation = journalService.getObservation(observationId);
        return ResponseEntity.ok(ObservationResponse.from(observation));
    }

    @GetMapping("/encounters/{encounterId}/observations")
    public ResponseEntity<List<ObservationResponse>> getObservationsByEncounter(@PathVariable UUID encounterId) {
        List<ObservationResponse> observations = journalService.getObservationsByEncounter(encounterId).stream()
                .map(ObservationResponse::from)
                .toList();
        return ResponseEntity.ok(observations);
    }

    @GetMapping("/patients/{patientId}/observations")
    public ResponseEntity<List<ObservationResponse>> getObservationsByPatient(
            @PathVariable UUID patientId,
            @RequestParam(required = false) ObservationCategory category) {
        List<Observation> observations;
        if (category == ObservationCategory.VITAL_SIGNS) {
            observations = journalService.getVitalSignsByPatient(patientId);
        } else if (category == ObservationCategory.LABORATORY) {
            observations = journalService.getLabResultsByPatient(patientId);
        } else {
            observations = journalService.getObservationsByPatient(patientId);
        }
        return ResponseEntity.ok(observations.stream().map(ObservationResponse::from).toList());
    }

    @GetMapping("/patients/{patientId}/observations/critical")
    public ResponseEntity<List<ObservationResponse>> getCriticalObservations(@PathVariable UUID patientId) {
        List<ObservationResponse> observations = journalService.getCriticalObservations(patientId).stream()
                .map(ObservationResponse::from)
                .toList();
        return ResponseEntity.ok(observations);
    }

    @GetMapping("/patients/{patientId}/observations/history")
    public ResponseEntity<List<ObservationResponse>> getObservationHistory(
            @PathVariable UUID patientId,
            @RequestParam String code) {
        List<ObservationResponse> observations = journalService.getObservationHistory(patientId, code).stream()
                .map(ObservationResponse::from)
                .toList();
        return ResponseEntity.ok(observations);
    }
}
