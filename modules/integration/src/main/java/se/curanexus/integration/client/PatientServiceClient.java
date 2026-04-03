package se.curanexus.integration.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Reactive client for the Patient Service.
 * Uses WebClient for non-blocking HTTP calls compatible with Spring Cloud Gateway.
 */
@Component
public class PatientServiceClient {

    private static final Logger log = LoggerFactory.getLogger(PatientServiceClient.class);

    private final WebClient webClient;

    public PatientServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${curanexus.gateway.services.patient.url:http://localhost:8080}") String patientServiceUrl) {
        this.webClient = webClientBuilder
                .baseUrl(patientServiceUrl)
                .build();
    }

    /**
     * Get patient by ID.
     */
    public Mono<PatientResponse> getPatient(UUID patientId) {
        log.debug("Fetching patient: {}", patientId);
        return webClient.get()
                .uri("/api/v1/patients/{id}", patientId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> {
                    if (response.statusCode().value() == 404) {
                        return Mono.empty();
                    }
                    return response.createException().flatMap(Mono::error);
                })
                .bodyToMono(PatientResponse.class)
                .doOnNext(p -> log.debug("Retrieved patient: {}", p.id()))
                .doOnError(e -> log.error("Error fetching patient {}: {}", patientId, e.getMessage()));
    }

    /**
     * Search patients by personnummer.
     */
    public Mono<List<PatientResponse>> searchByPersonnummer(String personnummer) {
        log.debug("Searching patient by personnummer: {}", maskPersonnummer(personnummer));
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/patients")
                        .queryParam("personalIdentityNumber", personnummer)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<PageResponse<PatientResponse>>() {})
                .map(PageResponse::content)
                .doOnNext(patients -> log.debug("Found {} patients", patients.size()))
                .doOnError(e -> log.error("Error searching patients: {}", e.getMessage()));
    }

    /**
     * Search patients by name.
     */
    public Mono<List<PatientResponse>> searchByName(String name, int limit) {
        log.debug("Searching patients by name: {}", name);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/patients")
                        .queryParam("name", name)
                        .queryParam("size", limit)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<PageResponse<PatientResponse>>() {})
                .map(PageResponse::content)
                .doOnNext(patients -> log.debug("Found {} patients matching name '{}'", patients.size(), name))
                .doOnError(e -> log.error("Error searching patients by name: {}", e.getMessage()));
    }

    private String maskPersonnummer(String personnummer) {
        if (personnummer == null || personnummer.length() < 8) {
            return "***";
        }
        return personnummer.substring(0, 8) + "****";
    }

    // DTOs for Patient Service responses

    public record PatientResponse(
            UUID id,
            String personalIdentityNumber,
            String givenName,
            String familyName,
            String middleName,
            LocalDate dateOfBirth,
            String gender,
            boolean protectedIdentity,
            boolean deceased,
            LocalDate deceasedDate
    ) {}

    public record PageResponse<T>(
            List<T> content,
            int totalElements,
            int totalPages,
            int number,
            int size
    ) {}
}
