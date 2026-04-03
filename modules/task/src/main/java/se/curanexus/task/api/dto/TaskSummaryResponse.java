package se.curanexus.task.api.dto;

import java.time.LocalDateTime;
import java.util.List;

public record TaskSummaryResponse(
        int total,
        int completed,
        int pending,
        List<String> pendingTaskTitles,
        // Extended progress information
        TaskProgressDetails progress
) {
    public record TaskProgressDetails(
            int blocked,
            int inProgress,
            int overdue,
            int escalated,
            double completionPercentage,
            LocalDateTime nextDueAt,
            List<String> overdueTaskTitles,
            List<String> blockedTaskTitles
    ) {
        public static TaskProgressDetails empty() {
            return new TaskProgressDetails(0, 0, 0, 0, 100.0, null, List.of(), List.of());
        }
    }

    // Constructor for backward compatibility
    public TaskSummaryResponse(int total, int completed, int pending, List<String> pendingTaskTitles) {
        this(total, completed, pending, pendingTaskTitles, TaskProgressDetails.empty());
    }
}
