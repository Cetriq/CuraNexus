package se.curanexus.triage.api.dto;

import se.curanexus.triage.domain.CareLevel;
import se.curanexus.triage.domain.TriagePriority;
import se.curanexus.triage.service.DecisionSupportService;

import java.util.List;
import java.util.UUID;

public record DecisionSupportResponse(
        TriagePriority recommendedPriority,
        CareLevel recommendedCareLevel,
        double confidence,
        List<ClinicalWarningResponse> warnings,
        List<ProtocolSuggestionResponse> suggestedProtocols,
        List<DifferentialDiagnosisResponse> differentialDiagnoses,
        List<String> recommendedActions,
        List<String> redFlags
) {
    public record ClinicalWarningResponse(String type, String severity, String message, String action) {}
    public record ProtocolSuggestionResponse(UUID protocolId, String protocolName, double relevance) {}
    public record DifferentialDiagnosisResponse(String diagnosisCode, String diagnosisName, double probability) {}

    public static DecisionSupportResponse fromResult(DecisionSupportService.DecisionSupportResult result) {
        return new DecisionSupportResponse(
                result.recommendedPriority(),
                result.recommendedCareLevel(),
                result.confidence(),
                result.warnings().stream()
                        .map(w -> new ClinicalWarningResponse(w.type(), w.severity(), w.message(), w.action()))
                        .toList(),
                result.suggestedProtocols().stream()
                        .map(p -> new ProtocolSuggestionResponse(p.protocolId(), p.protocolName(), p.relevance()))
                        .toList(),
                result.differentialDiagnoses().stream()
                        .map(d -> new DifferentialDiagnosisResponse(d.diagnosisCode(), d.diagnosisName(), d.probability()))
                        .toList(),
                result.recommendedActions(),
                result.redFlags()
        );
    }
}
