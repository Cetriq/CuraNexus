package se.curanexus.triage.api;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.curanexus.triage.api.dto.*;
import se.curanexus.triage.domain.AssessmentStatus;
import se.curanexus.triage.domain.TriagePriority;
import se.curanexus.triage.service.TriageService;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/triage/assessments")
public class TriageAssessmentController {

    private final TriageService triageService;

    public TriageAssessmentController(TriageService triageService) {
        this.triageService = triageService;
    }

    @PostMapping
    public ResponseEntity<TriageAssessmentResponse> createAssessment(
            @Valid @RequestBody CreateAssessmentRequest request) {
        var assessment = triageService.createAssessment(
                request.patientId(),
                request.encounterId(),
                request.triageNurseId(),
                request.chiefComplaint(),
                request.arrivalMode(),
                request.locationId()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(TriageAssessmentResponse.fromEntity(assessment));
    }

    @GetMapping("/{assessmentId}")
    public ResponseEntity<TriageAssessmentResponse> getAssessment(@PathVariable UUID assessmentId) {
        return triageService.getAssessment(assessmentId)
                .map(TriageAssessmentResponse::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{assessmentId}")
    public ResponseEntity<TriageAssessmentResponse> updateAssessment(
            @PathVariable UUID assessmentId,
            @Valid @RequestBody UpdateAssessmentRequest request) {
        var assessment = triageService.updateAssessment(
                assessmentId,
                request.chiefComplaint(),
                request.notes(),
                request.priority(),
                request.careLevel()
        );
        return ResponseEntity.ok(TriageAssessmentResponse.fromEntity(assessment));
    }

    @PostMapping("/{assessmentId}/complete")
    public ResponseEntity<TriageAssessmentResponse> completeAssessment(
            @PathVariable UUID assessmentId,
            @Valid @RequestBody CompleteAssessmentRequest request) {
        var assessment = triageService.completeAssessment(
                assessmentId,
                request.priority(),
                request.careLevel(),
                request.disposition(),
                request.notes(),
                request.recommendedProtocolId()
        );
        return ResponseEntity.ok(TriageAssessmentResponse.fromEntity(assessment));
    }

    @PostMapping("/{assessmentId}/escalate")
    public ResponseEntity<TriageAssessmentResponse> escalatePriority(
            @PathVariable UUID assessmentId,
            @Valid @RequestBody EscalationRequest request) {
        var assessment = triageService.escalatePriority(
                assessmentId,
                request.newPriority(),
                request.reason(),
                request.escalatedBy()
        );
        return ResponseEntity.ok(TriageAssessmentResponse.fromEntity(assessment));
    }

    @GetMapping
    public ResponseEntity<Page<TriageAssessmentResponse>> searchAssessments(
            @RequestParam(required = false) UUID patientId,
            @RequestParam(required = false) UUID encounterId,
            @RequestParam(required = false) TriagePriority priority,
            @RequestParam(required = false) AssessmentStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant toDate,
            Pageable pageable) {
        var page = triageService.searchAssessments(patientId, encounterId, priority, status, fromDate, toDate, pageable);
        return ResponseEntity.ok(page.map(TriageAssessmentResponse::fromEntity));
    }

    @PostMapping("/{assessmentId}/symptoms")
    public ResponseEntity<SymptomResponse> addSymptom(
            @PathVariable UUID assessmentId,
            @Valid @RequestBody SymptomRequest request) {
        var symptom = triageService.addSymptom(
                assessmentId,
                request.symptomCode(),
                request.description(),
                request.onset(),
                request.duration(),
                request.severity(),
                request.bodyLocation(),
                request.isChiefComplaint()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(SymptomResponse.fromEntity(symptom));
    }

    @GetMapping("/{assessmentId}/symptoms")
    public ResponseEntity<List<SymptomResponse>> getSymptoms(@PathVariable UUID assessmentId) {
        var symptoms = triageService.getSymptoms(assessmentId);
        return ResponseEntity.ok(symptoms.stream().map(SymptomResponse::fromEntity).toList());
    }

    @PostMapping("/{assessmentId}/vital-signs")
    public ResponseEntity<VitalSignsResponse> recordVitalSigns(
            @PathVariable UUID assessmentId,
            @Valid @RequestBody VitalSignsRequest request) {
        var vitalSigns = triageService.recordVitalSigns(
                assessmentId,
                request.recordedBy(),
                request.bloodPressureSystolic(),
                request.bloodPressureDiastolic(),
                request.heartRate(),
                request.respiratoryRate(),
                request.temperature(),
                request.oxygenSaturation(),
                request.painLevel(),
                request.consciousnessLevel(),
                request.glucoseLevel()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(VitalSignsResponse.fromEntity(vitalSigns));
    }

    @GetMapping("/{assessmentId}/vital-signs")
    public ResponseEntity<VitalSignsResponse> getVitalSigns(@PathVariable UUID assessmentId) {
        return triageService.getVitalSigns(assessmentId)
                .map(VitalSignsResponse::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
