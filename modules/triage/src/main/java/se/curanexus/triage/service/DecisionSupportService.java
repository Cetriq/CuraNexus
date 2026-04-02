package se.curanexus.triage.service;

import org.springframework.stereotype.Service;
import se.curanexus.triage.domain.*;
import se.curanexus.triage.repository.TriageProtocolRepository;

import java.util.*;

/**
 * Clinical decision support service for triage assessments.
 * Provides priority recommendations, warnings, and protocol suggestions.
 */
@Service
public class DecisionSupportService {

    private final TriageProtocolRepository protocolRepository;

    public DecisionSupportService(TriageProtocolRepository protocolRepository) {
        this.protocolRepository = protocolRepository;
    }

    public DecisionSupportResult getRecommendation(DecisionSupportRequest request) {
        List<ClinicalWarning> warnings = new ArrayList<>();
        List<String> redFlags = new ArrayList<>();
        List<String> recommendedActions = new ArrayList<>();

        TriagePriority recommendedPriority = TriagePriority.NON_URGENT;
        CareLevel recommendedCareLevel = CareLevel.PRIMARY_CARE;
        double confidence = 0.7;

        // Analyze vital signs
        if (request.vitalSigns() != null) {
            VitalSignAnalysis vsAnalysis = analyzeVitalSigns(request.vitalSigns());
            warnings.addAll(vsAnalysis.warnings());
            redFlags.addAll(vsAnalysis.redFlags());

            if (vsAnalysis.suggestedPriority().isHigherThan(recommendedPriority)) {
                recommendedPriority = vsAnalysis.suggestedPriority();
            }
        }

        // Analyze symptoms
        if (request.symptoms() != null && !request.symptoms().isEmpty()) {
            SymptomAnalysis symptomAnalysis = analyzeSymptoms(request.symptoms());
            warnings.addAll(symptomAnalysis.warnings());
            redFlags.addAll(symptomAnalysis.redFlags());

            if (symptomAnalysis.suggestedPriority().isHigherThan(recommendedPriority)) {
                recommendedPriority = symptomAnalysis.suggestedPriority();
            }
        }

        // Determine care level based on priority
        recommendedCareLevel = determineCareLevel(recommendedPriority);

        // Generate recommended actions
        recommendedActions.addAll(generateActions(recommendedPriority, warnings));

        // Find relevant protocols
        List<ProtocolSuggestion> suggestedProtocols = suggestProtocols(request.symptoms());

        // Calculate confidence
        if (!warnings.isEmpty()) {
            confidence = Math.max(0.5, confidence - (warnings.size() * 0.05));
        }
        if (!redFlags.isEmpty()) {
            confidence = Math.min(0.95, confidence + 0.1);
        }

        return new DecisionSupportResult(
                recommendedPriority,
                recommendedCareLevel,
                confidence,
                warnings,
                suggestedProtocols,
                List.of(), // differential diagnoses - would require ML model
                recommendedActions,
                redFlags
        );
    }

    private VitalSignAnalysis analyzeVitalSigns(VitalSignsInput vs) {
        List<ClinicalWarning> warnings = new ArrayList<>();
        List<String> redFlags = new ArrayList<>();
        TriagePriority priority = TriagePriority.NON_URGENT;

        // Blood pressure analysis
        if (vs.bloodPressureSystolic() != null) {
            if (vs.bloodPressureSystolic() >= 180) {
                warnings.add(new ClinicalWarning("VITAL_SIGN", "CRITICAL", "Hypertensive crisis", "Immediate BP management"));
                redFlags.add("Hypertensive crisis (SBP >= 180)");
                priority = TriagePriority.IMMEDIATE;
            } else if (vs.bloodPressureSystolic() <= 90) {
                warnings.add(new ClinicalWarning("VITAL_SIGN", "CRITICAL", "Hypotension", "Check for shock, establish IV access"));
                redFlags.add("Hypotension (SBP <= 90)");
                priority = TriagePriority.IMMEDIATE;
            }
        }

        // Heart rate analysis
        if (vs.heartRate() != null) {
            if (vs.heartRate() >= 150) {
                warnings.add(new ClinicalWarning("VITAL_SIGN", "CRITICAL", "Severe tachycardia", "Obtain ECG immediately"));
                redFlags.add("Severe tachycardia (HR >= 150)");
                if (priority.ordinal() > TriagePriority.EMERGENT.ordinal()) {
                    priority = TriagePriority.EMERGENT;
                }
            } else if (vs.heartRate() <= 40) {
                warnings.add(new ClinicalWarning("VITAL_SIGN", "CRITICAL", "Severe bradycardia", "Prepare for pacing"));
                redFlags.add("Severe bradycardia (HR <= 40)");
                if (priority.ordinal() > TriagePriority.EMERGENT.ordinal()) {
                    priority = TriagePriority.EMERGENT;
                }
            }
        }

        // Oxygen saturation analysis
        if (vs.oxygenSaturation() != null) {
            if (vs.oxygenSaturation() <= 88) {
                warnings.add(new ClinicalWarning("VITAL_SIGN", "CRITICAL", "Severe hypoxia", "High-flow oxygen, prepare for intubation"));
                redFlags.add("Severe hypoxia (SpO2 <= 88%)");
                priority = TriagePriority.IMMEDIATE;
            } else if (vs.oxygenSaturation() <= 92) {
                warnings.add(new ClinicalWarning("VITAL_SIGN", "HIGH", "Hypoxia", "Apply supplemental oxygen"));
                if (priority.ordinal() > TriagePriority.EMERGENT.ordinal()) {
                    priority = TriagePriority.EMERGENT;
                }
            }
        }

        // Temperature analysis
        if (vs.temperature() != null) {
            if (vs.temperature() >= 40.0) {
                warnings.add(new ClinicalWarning("VITAL_SIGN", "CRITICAL", "Hyperpyrexia", "Active cooling measures"));
                redFlags.add("Hyperpyrexia (T >= 40°C)");
                if (priority.ordinal() > TriagePriority.EMERGENT.ordinal()) {
                    priority = TriagePriority.EMERGENT;
                }
            } else if (vs.temperature() <= 35.0) {
                warnings.add(new ClinicalWarning("VITAL_SIGN", "CRITICAL", "Hypothermia", "Active rewarming"));
                redFlags.add("Hypothermia (T <= 35°C)");
                if (priority.ordinal() > TriagePriority.EMERGENT.ordinal()) {
                    priority = TriagePriority.EMERGENT;
                }
            }
        }

        // Consciousness analysis
        if (vs.consciousnessLevel() != null && vs.consciousnessLevel() != ConsciousnessLevel.ALERT) {
            warnings.add(new ClinicalWarning("VITAL_SIGN", "HIGH", "Altered consciousness: " + vs.consciousnessLevel(), "Assess airway, check glucose"));
            if (vs.consciousnessLevel() == ConsciousnessLevel.UNRESPONSIVE) {
                redFlags.add("Unresponsive patient");
                priority = TriagePriority.IMMEDIATE;
            } else if (vs.consciousnessLevel() == ConsciousnessLevel.PAIN) {
                redFlags.add("Responds only to pain");
                if (priority.ordinal() > TriagePriority.EMERGENT.ordinal()) {
                    priority = TriagePriority.EMERGENT;
                }
            }
        }

        return new VitalSignAnalysis(warnings, redFlags, priority);
    }

    private SymptomAnalysis analyzeSymptoms(List<SymptomInput> symptoms) {
        List<ClinicalWarning> warnings = new ArrayList<>();
        List<String> redFlags = new ArrayList<>();
        TriagePriority priority = TriagePriority.NON_URGENT;

        for (SymptomInput symptom : symptoms) {
            String code = symptom.symptomCode().toUpperCase();
            Severity severity = symptom.severity();

            // Check for high-priority symptom codes
            if (code.contains("CHEST") && code.contains("PAIN")) {
                warnings.add(new ClinicalWarning("SYMPTOM_COMBINATION", "HIGH", "Chest pain", "Obtain ECG within 10 minutes"));
                if (severity == Severity.SEVERE || severity == Severity.CRITICAL) {
                    redFlags.add("Severe chest pain");
                    priority = TriagePriority.IMMEDIATE;
                } else {
                    if (priority.ordinal() > TriagePriority.EMERGENT.ordinal()) {
                        priority = TriagePriority.EMERGENT;
                    }
                }
            }

            if (code.contains("DYSPNEA") || code.contains("SOB")) {
                warnings.add(new ClinicalWarning("SYMPTOM_COMBINATION", "HIGH", "Breathing difficulty", "Assess oxygen saturation"));
                if (severity == Severity.SEVERE || severity == Severity.CRITICAL) {
                    redFlags.add("Severe respiratory distress");
                    if (priority.ordinal() > TriagePriority.EMERGENT.ordinal()) {
                        priority = TriagePriority.EMERGENT;
                    }
                }
            }

            if (code.contains("STROKE") || code.contains("WEAKNESS") || code.contains("SPEECH")) {
                warnings.add(new ClinicalWarning("TIME_SENSITIVE", "CRITICAL", "Possible stroke", "Activate stroke protocol, document symptom onset time"));
                redFlags.add("Possible stroke - time critical");
                priority = TriagePriority.IMMEDIATE;
            }

            if (code.contains("BLEED") && severity == Severity.SEVERE) {
                warnings.add(new ClinicalWarning("SYMPTOM_COMBINATION", "CRITICAL", "Severe bleeding", "Establish IV access, type and cross"));
                redFlags.add("Severe hemorrhage");
                priority = TriagePriority.IMMEDIATE;
            }

            // Severity-based escalation
            if (severity == Severity.CRITICAL && priority.ordinal() > TriagePriority.EMERGENT.ordinal()) {
                priority = TriagePriority.EMERGENT;
            }
        }

        return new SymptomAnalysis(warnings, redFlags, priority);
    }

    private CareLevel determineCareLevel(TriagePriority priority) {
        return switch (priority) {
            case IMMEDIATE -> CareLevel.INTENSIVE_CARE;
            case EMERGENT -> CareLevel.EMERGENCY_CARE;
            case URGENT -> CareLevel.EMERGENCY_CARE;
            case LESS_URGENT -> CareLevel.OUTPATIENT_CARE;
            case NON_URGENT -> CareLevel.PRIMARY_CARE;
        };
    }

    private List<String> generateActions(TriagePriority priority, List<ClinicalWarning> warnings) {
        List<String> actions = new ArrayList<>();

        switch (priority) {
            case IMMEDIATE -> {
                actions.add("Immediate physician assessment required");
                actions.add("Prepare resuscitation equipment");
                actions.add("Establish IV access");
            }
            case EMERGENT -> {
                actions.add("Physician assessment within 15 minutes");
                actions.add("Continuous monitoring");
            }
            case URGENT -> {
                actions.add("Physician assessment within 60 minutes");
                actions.add("Regular vital sign monitoring");
            }
            case LESS_URGENT -> {
                actions.add("Assessment within 120 minutes");
            }
            case NON_URGENT -> {
                actions.add("Standard assessment");
            }
        }

        // Add warning-specific actions
        for (ClinicalWarning warning : warnings) {
            if (warning.action() != null && !warning.action().isBlank()) {
                actions.add(warning.action());
            }
        }

        return actions;
    }

    private List<ProtocolSuggestion> suggestProtocols(List<SymptomInput> symptoms) {
        if (symptoms == null || symptoms.isEmpty()) {
            return List.of();
        }

        List<ProtocolSuggestion> suggestions = new ArrayList<>();
        List<TriageProtocol> activeProtocols = protocolRepository.findByActiveTrue();

        for (SymptomInput symptom : symptoms) {
            String code = symptom.symptomCode().toUpperCase();

            for (TriageProtocol protocol : activeProtocols) {
                double relevance = calculateProtocolRelevance(protocol, code);
                if (relevance > 0.3) {
                    suggestions.add(new ProtocolSuggestion(protocol.getId(), protocol.getName(), relevance));
                }
            }
        }

        // Sort by relevance and deduplicate
        return suggestions.stream()
                .sorted((a, b) -> Double.compare(b.relevance(), a.relevance()))
                .distinct()
                .limit(5)
                .toList();
    }

    private double calculateProtocolRelevance(TriageProtocol protocol, String symptomCode) {
        String protocolCode = protocol.getCode().toUpperCase();
        String category = protocol.getCategory() != null ? protocol.getCategory().toUpperCase() : "";

        if (symptomCode.contains("CHEST") && protocolCode.contains("CHEST")) return 0.9;
        if (symptomCode.contains("RESP") && protocolCode.contains("RESP")) return 0.9;
        if (symptomCode.contains("ABD") && protocolCode.contains("ABD")) return 0.9;
        if (symptomCode.contains("TRAUMA") && protocolCode.contains("TRAUMA")) return 0.9;
        if (symptomCode.contains("NEURO") && protocolCode.contains("NEURO")) return 0.9;

        if (category.contains("CARDIOVASCULAR") && symptomCode.contains("CHEST")) return 0.7;
        if (category.contains("RESPIRATORY") && (symptomCode.contains("BREATH") || symptomCode.contains("DYSPNEA"))) return 0.7;

        return 0.0;
    }

    // Records for input/output
    public record DecisionSupportRequest(
            Integer patientAge,
            String patientSex,
            List<SymptomInput> symptoms,
            VitalSignsInput vitalSigns,
            List<String> medicalHistory,
            List<String> currentMedications
    ) {}

    public record SymptomInput(
            String symptomCode,
            String description,
            Severity severity
    ) {}

    public record VitalSignsInput(
            Integer bloodPressureSystolic,
            Integer bloodPressureDiastolic,
            Integer heartRate,
            Integer respiratoryRate,
            Double temperature,
            Integer oxygenSaturation,
            Integer painLevel,
            ConsciousnessLevel consciousnessLevel,
            Double glucoseLevel
    ) {}

    public record DecisionSupportResult(
            TriagePriority recommendedPriority,
            CareLevel recommendedCareLevel,
            double confidence,
            List<ClinicalWarning> warnings,
            List<ProtocolSuggestion> suggestedProtocols,
            List<DifferentialDiagnosis> differentialDiagnoses,
            List<String> recommendedActions,
            List<String> redFlags
    ) {}

    public record ClinicalWarning(String type, String severity, String message, String action) {}
    public record ProtocolSuggestion(UUID protocolId, String protocolName, double relevance) {}
    public record DifferentialDiagnosis(String diagnosisCode, String diagnosisName, double probability) {}

    private record VitalSignAnalysis(List<ClinicalWarning> warnings, List<String> redFlags, TriagePriority suggestedPriority) {}
    private record SymptomAnalysis(List<ClinicalWarning> warnings, List<String> redFlags, TriagePriority suggestedPriority) {}
}
