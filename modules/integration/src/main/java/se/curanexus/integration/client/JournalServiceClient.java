package se.curanexus.integration.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Reactive client for the Journal Service.
 * Provides access to observations, diagnoses, and clinical notes.
 */
@Component
public class JournalServiceClient {

    private static final Logger log = LoggerFactory.getLogger(JournalServiceClient.class);

    private final WebClient webClient;

    public JournalServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${curanexus.gateway.services.journal.url:http://localhost:8082}") String journalServiceUrl) {
        this.webClient = webClientBuilder
                .baseUrl(journalServiceUrl)
                .build();
    }

    // ========== Observation Methods ==========

    /**
     * Get observation by ID.
     */
    public Mono<ObservationResponse> getObservation(UUID observationId) {
        log.debug("Fetching observation: {}", observationId);
        return webClient.get()
                .uri("/api/v1/observations/{id}", observationId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> {
                    if (response.statusCode().value() == 404) {
                        return Mono.empty();
                    }
                    return response.createException().flatMap(Mono::error);
                })
                .bodyToMono(ObservationResponse.class)
                .doOnNext(o -> log.debug("Retrieved observation: {} (code: {})", o.id(), o.code()))
                .doOnError(e -> log.error("Error fetching observation {}: {}", observationId, e.getMessage()));
    }

    /**
     * Search observations by patient.
     */
    public Mono<List<ObservationResponse>> searchObservationsByPatient(UUID patientId, int limit) {
        log.debug("Searching observations for patient: {}", patientId);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/observations")
                        .queryParam("patientId", patientId)
                        .queryParam("size", limit)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<PageResponse<ObservationResponse>>() {})
                .map(PageResponse::content)
                .doOnNext(obs -> log.debug("Found {} observations for patient {}", obs.size(), patientId))
                .doOnError(e -> log.error("Error searching observations: {}", e.getMessage()));
    }

    /**
     * Search observations by encounter.
     */
    public Mono<List<ObservationResponse>> searchObservationsByEncounter(UUID encounterId, int limit) {
        log.debug("Searching observations for encounter: {}", encounterId);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/observations")
                        .queryParam("encounterId", encounterId)
                        .queryParam("size", limit)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<PageResponse<ObservationResponse>>() {})
                .map(PageResponse::content)
                .doOnNext(obs -> log.debug("Found {} observations for encounter {}", obs.size(), encounterId))
                .doOnError(e -> log.error("Error searching observations by encounter: {}", e.getMessage()));
    }

    /**
     * Search observations by category (e.g., "vital-signs", "laboratory").
     */
    public Mono<List<ObservationResponse>> searchObservationsByCategory(UUID patientId, String category, int limit) {
        log.debug("Searching {} observations for patient: {}", category, patientId);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/observations")
                        .queryParam("patientId", patientId)
                        .queryParam("category", category)
                        .queryParam("size", limit)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<PageResponse<ObservationResponse>>() {})
                .map(PageResponse::content)
                .doOnNext(obs -> log.debug("Found {} {} observations", obs.size(), category))
                .doOnError(e -> log.error("Error searching observations by category: {}", e.getMessage()));
    }

    // ========== Diagnosis Methods ==========

    /**
     * Get diagnosis by ID.
     */
    public Mono<DiagnosisResponse> getDiagnosis(UUID diagnosisId) {
        log.debug("Fetching diagnosis: {}", diagnosisId);
        return webClient.get()
                .uri("/api/v1/diagnoses/{id}", diagnosisId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> {
                    if (response.statusCode().value() == 404) {
                        return Mono.empty();
                    }
                    return response.createException().flatMap(Mono::error);
                })
                .bodyToMono(DiagnosisResponse.class)
                .doOnNext(d -> log.debug("Retrieved diagnosis: {} (code: {})", d.id(), d.code()))
                .doOnError(e -> log.error("Error fetching diagnosis {}: {}", diagnosisId, e.getMessage()));
    }

    /**
     * Search diagnoses by patient.
     */
    public Mono<List<DiagnosisResponse>> searchDiagnosesByPatient(UUID patientId, int limit) {
        log.debug("Searching diagnoses for patient: {}", patientId);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/diagnoses")
                        .queryParam("patientId", patientId)
                        .queryParam("size", limit)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<PageResponse<DiagnosisResponse>>() {})
                .map(PageResponse::content)
                .doOnNext(diag -> log.debug("Found {} diagnoses for patient {}", diag.size(), patientId))
                .doOnError(e -> log.error("Error searching diagnoses: {}", e.getMessage()));
    }

    /**
     * Search diagnoses by encounter.
     */
    public Mono<List<DiagnosisResponse>> searchDiagnosesByEncounter(UUID encounterId, int limit) {
        log.debug("Searching diagnoses for encounter: {}", encounterId);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/diagnoses")
                        .queryParam("encounterId", encounterId)
                        .queryParam("size", limit)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<PageResponse<DiagnosisResponse>>() {})
                .map(PageResponse::content)
                .doOnNext(diag -> log.debug("Found {} diagnoses for encounter {}", diag.size(), encounterId))
                .doOnError(e -> log.error("Error searching diagnoses by encounter: {}", e.getMessage()));
    }

    // ========== DTOs ==========

    public record ObservationResponse(
            UUID id,
            UUID encounterId,
            UUID patientId,
            String code,
            String codeSystem,
            String displayText,
            String category,
            BigDecimal valueNumeric,
            String valueString,
            Boolean valueBoolean,
            String unit,
            BigDecimal referenceRangeLow,
            BigDecimal referenceRangeHigh,
            String interpretation,
            LocalDateTime observedAt,
            Instant recordedAt,
            UUID recordedById,
            String recordedByName,
            String method,
            String bodySite,
            String device,
            String notes,
            boolean withinReferenceRange
    ) {}

    public record DiagnosisResponse(
            UUID id,
            UUID encounterId,
            UUID patientId,
            String code,
            String codeSystem,
            String displayText,
            String type,
            Integer rank,
            LocalDate onsetDate,
            LocalDate resolvedDate,
            Instant recordedAt,
            UUID recordedById
    ) {}

    public record PageResponse<T>(
            List<T> content,
            int totalElements,
            int totalPages,
            int number,
            int size
    ) {}
}
