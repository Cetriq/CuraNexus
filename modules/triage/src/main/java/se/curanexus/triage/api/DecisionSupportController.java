package se.curanexus.triage.api;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.curanexus.triage.api.dto.DecisionSupportRequest;
import se.curanexus.triage.api.dto.DecisionSupportResponse;
import se.curanexus.triage.domain.Severity;
import se.curanexus.triage.service.DecisionSupportService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/triage/decision-support")
public class DecisionSupportController {

    private final DecisionSupportService decisionSupportService;

    public DecisionSupportController(DecisionSupportService decisionSupportService) {
        this.decisionSupportService = decisionSupportService;
    }

    @PostMapping
    public ResponseEntity<DecisionSupportResponse> getDecisionSupport(
            @Valid @RequestBody DecisionSupportRequest request) {

        // Convert DTO to service request
        var serviceRequest = new DecisionSupportService.DecisionSupportRequest(
                request.patientAge(),
                request.patientSex(),
                request.symptoms() != null ? request.symptoms().stream()
                        .map(s -> new DecisionSupportService.SymptomInput(
                                s.symptomCode(),
                                s.description(),
                                s.severity()
                        ))
                        .toList() : List.of(),
                request.vitalSigns() != null ? new DecisionSupportService.VitalSignsInput(
                        request.vitalSigns().bloodPressureSystolic(),
                        request.vitalSigns().bloodPressureDiastolic(),
                        request.vitalSigns().heartRate(),
                        request.vitalSigns().respiratoryRate(),
                        request.vitalSigns().temperature(),
                        request.vitalSigns().oxygenSaturation(),
                        request.vitalSigns().painLevel(),
                        request.vitalSigns().consciousnessLevel(),
                        request.vitalSigns().glucoseLevel()
                ) : null,
                request.medicalHistory(),
                request.currentMedications()
        );

        var result = decisionSupportService.getRecommendation(serviceRequest);
        return ResponseEntity.ok(DecisionSupportResponse.fromResult(result));
    }
}
