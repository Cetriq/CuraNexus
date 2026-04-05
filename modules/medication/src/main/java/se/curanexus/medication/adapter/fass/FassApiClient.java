package se.curanexus.medication.adapter.fass;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import se.curanexus.medication.adapter.fass.dto.*;

import java.util.List;
import java.util.Optional;

/**
 * HTTP client for Fass API.
 *
 * Fass API provides:
 * - /medicinal-product - Product information
 * - /medicinal-product/{nplId} - Single product by NPL-ID
 * - /medicinal-product/{nplId}/smpc - Product resume (SMPC)
 * - /medicinal-product/{nplId}/pil - Patient information leaflet
 * - /atc - ATC classification
 * - /substances - Active substances
 *
 * @see <a href="https://api.fass.se/documentation">Fass API Documentation</a>
 */
@Component
public class FassApiClient implements FassApiOperations {

    private static final Logger log = LoggerFactory.getLogger(FassApiClient.class);

    private final WebClient webClient;
    private final FassApiProperties properties;

    public FassApiClient(WebClient.Builder webClientBuilder, FassApiProperties properties) {
        this.properties = properties;
        this.webClient = webClientBuilder
                .baseUrl(properties.getBaseUrl())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * Get medicinal product by NPL-ID.
     *
     * @param nplId NPL identifier (e.g., "20010131000022")
     * @return Product information
     */
    public Optional<FassMedicinalProduct> getMedicinalProduct(String nplId) {
        if (!properties.isEnabled()) {
            log.debug("Fass integration disabled, skipping getMedicinalProduct");
            return Optional.empty();
        }

        log.debug("Fetching medicinal product from Fass: {}", nplId);

        try {
            FassMedicinalProduct product = webClient.get()
                    .uri("/medicinal-product/{nplId}", nplId)
                    .headers(this::addAuthHeaders)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, this::handleClientError)
                    .onStatus(HttpStatusCode::is5xxServerError, this::handleServerError)
                    .bodyToMono(FassMedicinalProduct.class)
                    .block();

            return Optional.ofNullable(product);
        } catch (FassApiException e) {
            if (e.isNotFound()) {
                log.debug("Product not found in Fass: {}", nplId);
                return Optional.empty();
            }
            throw e;
        }
    }

    /**
     * Search medicinal products by name or ATC code.
     *
     * @param query Search query
     * @param limit Maximum results to return
     * @return List of matching products
     */
    public List<FassMedicinalProduct> searchProducts(String query, int limit) {
        if (!properties.isEnabled()) {
            log.debug("Fass integration disabled, skipping searchProducts");
            return List.of();
        }

        log.debug("Searching Fass products: query={}, limit={}", query, limit);

        FassSearchResult result = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/medicinal-product")
                        .queryParam("name", query)
                        .queryParam("limit", limit)
                        .build())
                .headers(this::addAuthHeaders)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handleClientError)
                .onStatus(HttpStatusCode::is5xxServerError, this::handleServerError)
                .bodyToMono(FassSearchResult.class)
                .block();

        return result != null && result.products() != null ? result.products() : List.of();
    }

    /**
     * Get products by ATC code.
     *
     * @param atcCode ATC code (e.g., "N02BE01" for Paracetamol)
     * @return List of products with this ATC code
     */
    public List<FassMedicinalProduct> getProductsByAtcCode(String atcCode) {
        if (!properties.isEnabled()) {
            return List.of();
        }

        log.debug("Fetching products by ATC code: {}", atcCode);

        FassSearchResult result = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/medicinal-product")
                        .queryParam("atcCode", atcCode)
                        .build())
                .headers(this::addAuthHeaders)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handleClientError)
                .onStatus(HttpStatusCode::is5xxServerError, this::handleServerError)
                .bodyToMono(FassSearchResult.class)
                .block();

        return result != null && result.products() != null ? result.products() : List.of();
    }

    /**
     * Get products by ATC group (all levels under the given code).
     *
     * @param atcGroup ATC group prefix (e.g., "N02" for analgesics)
     * @return List of products in this ATC group
     */
    public List<FassMedicinalProduct> getProductsByAtcGroup(String atcGroup) {
        if (!properties.isEnabled()) {
            return List.of();
        }

        log.debug("Fetching products by ATC group: {}", atcGroup);

        FassSearchResult result = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/medicinal-product")
                        .queryParam("atcGroup", atcGroup)
                        .build())
                .headers(this::addAuthHeaders)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handleClientError)
                .onStatus(HttpStatusCode::is5xxServerError, this::handleServerError)
                .bodyToMono(FassSearchResult.class)
                .block();

        return result != null && result.products() != null ? result.products() : List.of();
    }

    /**
     * Get SMPC (Summary of Product Characteristics) for a product.
     *
     * @param nplId NPL identifier
     * @return Product document with SMPC sections
     */
    public Optional<FassProductDocument> getSmpc(String nplId) {
        if (!properties.isEnabled()) {
            return Optional.empty();
        }

        log.debug("Fetching SMPC from Fass: {}", nplId);

        try {
            FassProductDocument doc = webClient.get()
                    .uri("/medicinal-product/{nplId}/smpc", nplId)
                    .headers(this::addAuthHeaders)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, this::handleClientError)
                    .onStatus(HttpStatusCode::is5xxServerError, this::handleServerError)
                    .bodyToMono(FassProductDocument.class)
                    .block();

            return Optional.ofNullable(doc);
        } catch (FassApiException e) {
            if (e.isNotFound()) {
                log.debug("SMPC not found for product: {}", nplId);
                return Optional.empty();
            }
            throw e;
        }
    }

    /**
     * Get PIL (Patient Information Leaflet) for a product.
     *
     * @param nplId NPL identifier
     * @return Product document with PIL sections
     */
    public Optional<FassProductDocument> getPil(String nplId) {
        if (!properties.isEnabled()) {
            return Optional.empty();
        }

        log.debug("Fetching PIL from Fass: {}", nplId);

        try {
            FassProductDocument doc = webClient.get()
                    .uri("/medicinal-product/{nplId}/pil", nplId)
                    .headers(this::addAuthHeaders)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, this::handleClientError)
                    .onStatus(HttpStatusCode::is5xxServerError, this::handleServerError)
                    .bodyToMono(FassProductDocument.class)
                    .block();

            return Optional.ofNullable(doc);
        } catch (FassApiException e) {
            if (e.isNotFound()) {
                log.debug("PIL not found for product: {}", nplId);
                return Optional.empty();
            }
            throw e;
        }
    }

    /**
     * Get ATC classification tree.
     *
     * @return List of top-level ATC codes with children
     */
    public List<FassAtcCode> getAtcCodes() {
        if (!properties.isEnabled()) {
            return List.of();
        }

        log.debug("Fetching ATC codes from Fass");

        List<FassAtcCode> result = webClient.get()
                .uri("/atc")
                .headers(this::addAuthHeaders)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handleClientError)
                .onStatus(HttpStatusCode::is5xxServerError, this::handleServerError)
                .bodyToFlux(FassAtcCode.class)
                .collectList()
                .block();

        return result != null ? result : List.of();
    }

    /**
     * Add authentication headers to the request.
     * Fass API uses JWT token authentication.
     */
    private void addAuthHeaders(HttpHeaders headers) {
        if (properties.getApiKey() != null && !properties.getApiKey().isBlank()) {
            headers.setBearerAuth(properties.getApiKey());
        }
    }

    /**
     * Handle 4xx client errors from Fass API.
     */
    private Mono<Throwable> handleClientError(ClientResponse response) {
        return response.bodyToMono(String.class)
                .defaultIfEmpty("Unknown error")
                .flatMap(body -> {
                    int status = response.statusCode().value();
                    log.warn("Fass API client error: {} - {}", status, body);

                    String errorCode = extractErrorCode(body);
                    return Mono.error(new FassApiException(
                            "Fass API error: " + body,
                            status,
                            errorCode
                    ));
                });
    }

    /**
     * Handle 5xx server errors from Fass API.
     */
    private Mono<Throwable> handleServerError(ClientResponse response) {
        return response.bodyToMono(String.class)
                .defaultIfEmpty("Server error")
                .flatMap(body -> {
                    int status = response.statusCode().value();
                    log.error("Fass API server error: {} - {}", status, body);
                    return Mono.error(new FassApiException(
                            "Fass API server error: " + body,
                            status
                    ));
                });
    }

    /**
     * Extract error code from Fass error response.
     */
    private String extractErrorCode(String responseBody) {
        // Fass returns error codes like SMPC_DOCUMENT_NOT_FOUND_BECAUSE_NO_SWEDISH_SMPC_EXISTS
        if (responseBody != null && responseBody.contains("SMPC_DOCUMENT_NOT_FOUND")) {
            return "SMPC_DOCUMENT_NOT_FOUND_BECAUSE_NO_SWEDISH_SMPC_EXISTS";
        }
        return null;
    }
}
