package se.curanexus.triage.api.dto;

import se.curanexus.triage.domain.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TriageAssessmentResponse(
        UUID id,
        UUID patientId,
        UUID encounterId,
        UUID triageNurseId,
        String triageNurseName,
        String chiefComplaint,
        TriagePriority priority,
        CareLevel careLevel,
        Disposition disposition,
        AssessmentStatus status,
        ArrivalMode arrivalMode,
        Instant arrivalTime,
        Instant triageStartTime,
        Instant triageEndTime,
        int waitTimeMinutes,
        List<SymptomResponse> symptoms,
        VitalSignsResponse vitalSigns,
        String notes,
        List<EscalationRecordResponse> escalationHistory,
        Instant createdAt,
        Instant updatedAt
) {
    public static TriageAssessmentResponse fromEntity(TriageAssessment assessment) {
        return new TriageAssessmentResponse(
                assessment.getId(),
                assessment.getPatientId(),
                assessment.getEncounterId(),
                assessment.getTriageNurseId(),
                assessment.getTriageNurseName(),
                assessment.getChiefComplaint(),
                assessment.getPriority(),
                assessment.getCareLevel(),
                assessment.getDisposition(),
                assessment.getStatus(),
                assessment.getArrivalMode(),
                assessment.getArrivalTime(),
                assessment.getTriageStartTime(),
                assessment.getTriageEndTime(),
                assessment.getWaitTimeMinutes(),
                assessment.getSymptoms().stream().map(SymptomResponse::fromEntity).toList(),
                assessment.getVitalSigns() != null ? VitalSignsResponse.fromEntity(assessment.getVitalSigns()) : null,
                assessment.getNotes(),
                assessment.getEscalationHistory().stream().map(EscalationRecordResponse::fromEntity).toList(),
                assessment.getCreatedAt(),
                assessment.getUpdatedAt()
        );
    }
}
