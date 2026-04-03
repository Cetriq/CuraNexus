package se.curanexus.encounter.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;

/**
 * Client for communicating with the Journal module.
 */
@Component
public class JournalServiceClient {

    private static final Logger log = LoggerFactory.getLogger(JournalServiceClient.class);

    private final RestClient restClient;

    public JournalServiceClient(
            RestClient.Builder restClientBuilder,
            @Value("${curanexus.services.journal.url:http://localhost:8082}") String journalServiceUrl) {
        this.restClient = restClientBuilder
                .baseUrl(journalServiceUrl)
                .build();
    }

    /**
     * Get note statistics for an encounter.
     */
    public NoteStatistics getNoteStatistics(UUID encounterId) {
        try {
            NoteSummaryResponse response = restClient.get()
                    .uri("/api/v1/encounters/{encounterId}/notes/summary", encounterId)
                    .retrieve()
                    .body(NoteSummaryResponse.class);

            if (response != null) {
                return new NoteStatistics(
                        response.total(),
                        response.signed(),
                        response.unsigned(),
                        response.unsignedNoteTitles()
                );
            }
        } catch (Exception e) {
            log.warn("Failed to get note statistics for encounter {}: {}", encounterId, e.getMessage());
        }

        return NoteStatistics.empty();
    }

    public record NoteStatistics(
            int total,
            int signed,
            int unsigned,
            List<String> unsignedNoteTitles
    ) {
        public static NoteStatistics empty() {
            return new NoteStatistics(0, 0, 0, List.of());
        }

        public boolean allSigned() {
            return unsigned == 0;
        }
    }

    private record NoteSummaryResponse(
            int total,
            int signed,
            int unsigned,
            List<String> unsignedNoteTitles
    ) {}
}
