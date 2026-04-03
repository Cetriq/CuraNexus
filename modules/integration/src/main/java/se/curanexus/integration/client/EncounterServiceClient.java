package se.curanexus.integration.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Reactive client for the Encounter Service.
 * Uses WebClient for non-blocking HTTP calls compatible with Spring Cloud Gateway.
 */
@Component
public class EncounterServiceClient {

    private static final Logger log = LoggerFactory.getLogger(EncounterServiceClient.class);

    private final WebClient webClient;

    public EncounterServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${curanexus.gateway.services.encounter.url:http://localhost:8081}") String encounterServiceUrl) {
        this.webClient = webClientBuilder
                .baseUrl(encounterServiceUrl)
                .build();
    }

    /**
     * Get encounter by ID.
     */
    public Mono<EncounterResponse> getEncounter(UUID encounterId) {
        log.debug("Fetching encounter: {}", encounterId);
        return webClient.get()
                .uri("/api/v1/encounters/{id}", encounterId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> {
                    if (response.statusCode().value() == 404) {
                        return Mono.empty();
                    }
                    return response.createException().flatMap(Mono::error);
                })
                .bodyToMono(EncounterResponse.class)
                .doOnNext(e -> log.debug("Retrieved encounter: {} (status: {})", e.id(), e.status()))
                .doOnError(e -> log.error("Error fetching encounter {}: {}", encounterId, e.getMessage()));
    }

    /**
     * Search encounters by patient ID.
     */
    public Mono<List<EncounterResponse>> searchByPatient(UUID patientId, int limit) {
        log.debug("Searching encounters for patient: {}", patientId);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/encounters")
                        .queryParam("patientId", patientId)
                        .queryParam("size", limit)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<PageResponse<EncounterResponse>>() {})
                .map(PageResponse::content)
                .doOnNext(encounters -> log.debug("Found {} encounters for patient {}", encounters.size(), patientId))
                .doOnError(e -> log.error("Error searching encounters: {}", e.getMessage()));
    }

    /**
     * Search encounters by status.
     */
    public Mono<List<EncounterResponse>> searchByStatus(String status, int limit) {
        log.debug("Searching encounters by status: {}", status);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/encounters")
                        .queryParam("status", status)
                        .queryParam("size", limit)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<PageResponse<EncounterResponse>>() {})
                .map(PageResponse::content)
                .doOnNext(encounters -> log.debug("Found {} encounters with status {}", encounters.size(), status))
                .doOnError(e -> log.error("Error searching encounters by status: {}", e.getMessage()));
    }

    /**
     * Search encounters by class (INPATIENT, OUTPATIENT, EMERGENCY, etc.).
     */
    public Mono<List<EncounterResponse>> searchByClass(String encounterClass, int limit) {
        log.debug("Searching encounters by class: {}", encounterClass);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/encounters")
                        .queryParam("encounterClass", encounterClass)
                        .queryParam("size", limit)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<PageResponse<EncounterResponse>>() {})
                .map(PageResponse::content)
                .doOnNext(encounters -> log.debug("Found {} encounters with class {}", encounters.size(), encounterClass))
                .doOnError(e -> log.error("Error searching encounters by class: {}", e.getMessage()));
    }

    // DTOs for Encounter Service responses

    public record EncounterResponse(
            UUID id,
            UUID patientId,
            String status,
            String encounterClass,
            String type,
            String priority,
            String serviceType,
            UUID responsibleUnitId,
            UUID responsiblePractitionerId,
            String responsiblePractitionerHsaId,
            String responsibleUnitHsaId,
            Instant plannedStartTime,
            Instant plannedEndTime,
            Instant actualStartTime,
            Instant actualEndTime,
            String triageLevel,
            List<String> reasonCodes
    ) {}

    public record PageResponse<T>(
            List<T> content,
            int totalElements,
            int totalPages,
            int number,
            int size
    ) {}
}
