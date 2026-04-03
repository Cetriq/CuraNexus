package se.curanexus.encounter.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Client for communicating with the Task module.
 */
@Component
public class TaskServiceClient {

    private static final Logger log = LoggerFactory.getLogger(TaskServiceClient.class);

    private final RestClient restClient;

    public TaskServiceClient(
            RestClient.Builder restClientBuilder,
            @Value("${curanexus.services.task.url:http://localhost:8083}") String taskServiceUrl) {
        this.restClient = restClientBuilder
                .baseUrl(taskServiceUrl)
                .build();
    }

    /**
     * Get task statistics for an encounter.
     */
    public TaskStatistics getTaskStatistics(UUID encounterId) {
        try {
            TaskSummaryResponse response = restClient.get()
                    .uri("/api/v1/encounters/{encounterId}/tasks/summary", encounterId)
                    .retrieve()
                    .body(TaskSummaryResponse.class);

            if (response != null) {
                TaskProgressDetails progress = TaskProgressDetails.empty();
                if (response.progress() != null) {
                    progress = new TaskProgressDetails(
                            response.progress().blocked(),
                            response.progress().inProgress(),
                            response.progress().overdue(),
                            response.progress().escalated(),
                            response.progress().completionPercentage(),
                            response.progress().nextDueAt(),
                            response.progress().overdueTaskTitles() != null ? response.progress().overdueTaskTitles() : List.of(),
                            response.progress().blockedTaskTitles() != null ? response.progress().blockedTaskTitles() : List.of()
                    );
                }

                return new TaskStatistics(
                        response.total(),
                        response.completed(),
                        response.pending(),
                        response.pendingTaskTitles() != null ? response.pendingTaskTitles() : List.of(),
                        progress
                );
            }
        } catch (Exception e) {
            log.warn("Failed to get task statistics for encounter {}: {}", encounterId, e.getMessage());
        }

        return TaskStatistics.empty();
    }

    public record TaskStatistics(
            int total,
            int completed,
            int pending,
            List<String> pendingTaskTitles,
            TaskProgressDetails progress
    ) {
        public static TaskStatistics empty() {
            return new TaskStatistics(0, 0, 0, List.of(), TaskProgressDetails.empty());
        }

        public boolean allCompleted() {
            return pending == 0;
        }
    }

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

    private record TaskSummaryResponse(
            int total,
            int completed,
            int pending,
            List<String> pendingTaskTitles,
            TaskProgressDetailsResponse progress
    ) {}

    private record TaskProgressDetailsResponse(
            int blocked,
            int inProgress,
            int overdue,
            int escalated,
            double completionPercentage,
            LocalDateTime nextDueAt,
            List<String> overdueTaskTitles,
            List<String> blockedTaskTitles
    ) {}
}
