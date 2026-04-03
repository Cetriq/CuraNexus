package se.curanexus.encounter.api.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for encounter readiness check.
 * Indicates whether an encounter can be marked as FINISHED.
 */
public record EncounterReadinessDto(
        UUID encounterId,
        ReadinessStatus status,
        List<String> blockers,
        EncounterReadinessSummary summary,
        EncounterProgressDetails progress
) {
    public enum ReadinessStatus {
        READY,
        NOT_READY,
        ERROR
    }

    public record EncounterReadinessSummary(
            int totalTasks,
            int completedTasks,
            int pendingTasks,
            int totalNotes,
            int signedNotes,
            int unsignedNotes
    ) {}

    public record EncounterProgressDetails(
            double overallCompletionPercentage,
            double taskCompletionPercentage,
            double noteCompletionPercentage,
            int blockedTasks,
            int overdueTasks,
            int escalatedTasks,
            LocalDateTime nextTaskDueAt,
            List<String> overdueTaskTitles,
            List<String> blockedTaskTitles
    ) {
        public static EncounterProgressDetails empty() {
            return new EncounterProgressDetails(100.0, 100.0, 100.0, 0, 0, 0, null, List.of(), List.of());
        }
    }

    public static EncounterReadinessDto ready(UUID encounterId, EncounterReadinessSummary summary) {
        return new EncounterReadinessDto(encounterId, ReadinessStatus.READY, List.of(), summary,
                EncounterProgressDetails.empty());
    }

    public static EncounterReadinessDto ready(UUID encounterId, EncounterReadinessSummary summary, EncounterProgressDetails progress) {
        return new EncounterReadinessDto(encounterId, ReadinessStatus.READY, List.of(), summary, progress);
    }

    public static EncounterReadinessDto notReady(UUID encounterId, List<String> blockers, EncounterReadinessSummary summary) {
        return new EncounterReadinessDto(encounterId, ReadinessStatus.NOT_READY, blockers, summary,
                EncounterProgressDetails.empty());
    }

    public static EncounterReadinessDto notReady(UUID encounterId, List<String> blockers, EncounterReadinessSummary summary,
                                                  EncounterProgressDetails progress) {
        return new EncounterReadinessDto(encounterId, ReadinessStatus.NOT_READY, blockers, summary, progress);
    }

    public static EncounterReadinessDto error(UUID encounterId, String errorMessage) {
        return new EncounterReadinessDto(
                encounterId,
                ReadinessStatus.ERROR,
                List.of(errorMessage),
                new EncounterReadinessSummary(0, 0, 0, 0, 0, 0),
                EncounterProgressDetails.empty()
        );
    }
}
