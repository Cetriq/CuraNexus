package se.curanexus.triage.api.dto;

import se.curanexus.triage.domain.TriageAssessment;
import se.curanexus.triage.domain.TriagePriority;
import se.curanexus.triage.service.TriageService;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public record TriageQueueResponse(
        UUID locationId,
        String locationName,
        long totalWaiting,
        Map<String, Long> byPriority,
        int averageWaitMinutes,
        List<QueuedPatientResponse> patients
) {
    public record QueuedPatientResponse(
            UUID assessmentId,
            UUID patientId,
            TriagePriority priority,
            String chiefComplaint,
            Instant arrivalTime,
            int waitTimeMinutes,
            int maxWaitMinutes,
            boolean isOverdue
    ) {
        public static QueuedPatientResponse fromEntity(TriageAssessment assessment) {
            int maxWait = assessment.getPriority() != null ? assessment.getPriority().getMaxWaitMinutes() : 240;
            return new QueuedPatientResponse(
                    assessment.getId(),
                    assessment.getPatientId(),
                    assessment.getPriority(),
                    assessment.getChiefComplaint(),
                    assessment.getArrivalTime(),
                    assessment.getWaitTimeMinutes(),
                    maxWait,
                    assessment.isOverdue()
            );
        }
    }

    public static TriageQueueResponse fromQueueInfo(TriageService.TriageQueueInfo info) {
        Map<String, Long> priorityMap = info.byPriority().entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().name(), Map.Entry::getValue));

        return new TriageQueueResponse(
                info.locationId(),
                null, // Location name would come from external service
                info.totalWaiting(),
                priorityMap,
                info.averageWaitMinutes(),
                info.patients().stream().map(QueuedPatientResponse::fromEntity).toList()
        );
    }
}
