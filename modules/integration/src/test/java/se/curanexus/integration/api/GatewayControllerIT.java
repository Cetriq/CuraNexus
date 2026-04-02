package se.curanexus.integration.api;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewayControllerIT {

    @Autowired
    private WebTestClient webTestClient;

    private static MockWebServer mockPatientService;
    private static MockWebServer mockEncounterService;

    @BeforeEach
    void setUp() throws IOException {
        mockPatientService = new MockWebServer();
        mockPatientService.start(18080);

        mockEncounterService = new MockWebServer();
        mockEncounterService.start(18081);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockPatientService.shutdown();
        mockEncounterService.shutdown();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("curanexus.gateway.services.patient.url", () -> "http://localhost:18080");
        registry.add("curanexus.gateway.services.encounter.url", () -> "http://localhost:18081");
    }

    @Test
    void shouldListServices() {
        webTestClient.get()
                .uri("/gateway/services")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isArray();
    }

    @Test
    void shouldListRoutes() {
        webTestClient.get()
                .uri("/gateway/routes")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isArray();
    }

    @Test
    void shouldGetMetrics() {
        webTestClient.get()
                .uri("/gateway/metrics")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.circuitBreakerStatus").exists();
    }

    @Test
    void shouldCheckServiceHealth() {
        mockPatientService.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"status\":\"UP\"}")
                .addHeader("Content-Type", "application/json"));

        webTestClient.get()
                .uri("/gateway/services/patient/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").exists();
    }

    @Test
    void shouldReturnDownWhenServiceUnavailable() {
        // Don't enqueue any response - service will fail
        mockPatientService.enqueue(new MockResponse().setResponseCode(500));

        webTestClient.get()
                .uri("/gateway/services/patient/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("DOWN");
    }

    @Test
    void shouldGetAggregatedHealth() {
        mockPatientService.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"status\":\"UP\"}")
                .addHeader("Content-Type", "application/json"));
        mockEncounterService.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"status\":\"UP\"}")
                .addHeader("Content-Type", "application/json"));

        webTestClient.get()
                .uri("/gateway/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").exists()
                .jsonPath("$.services").exists();
    }

    @Test
    void shouldReturnNotFoundForUnknownService() {
        webTestClient.get()
                .uri("/gateway/services/unknown/health")
                .exchange()
                .expectStatus().isNotFound();
    }
}
