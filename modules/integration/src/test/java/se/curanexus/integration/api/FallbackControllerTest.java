package se.curanexus.integration.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(FallbackController.class)
class FallbackControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void shouldReturnFallbackForPatientService() {
        webTestClient.get()
                .uri("/fallback/patient")
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectBody()
                .jsonPath("$.error").isEqualTo("SERVICE_UNAVAILABLE")
                .jsonPath("$.service").isEqualTo("patient")
                .jsonPath("$.message").isNotEmpty()
                .jsonPath("$.retryAfter").isEqualTo(30);
    }

    @Test
    void shouldReturnFallbackForEncounterService() {
        webTestClient.get()
                .uri("/fallback/encounter")
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectBody()
                .jsonPath("$.service").isEqualTo("encounter");
    }

    @Test
    void shouldReturnFallbackForJournalService() {
        webTestClient.get()
                .uri("/fallback/journal")
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectBody()
                .jsonPath("$.service").isEqualTo("journal");
    }

    @Test
    void shouldReturnFallbackForTaskService() {
        webTestClient.get()
                .uri("/fallback/task")
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectBody()
                .jsonPath("$.service").isEqualTo("task");
    }

    @Test
    void shouldReturnFallbackForAuthorizationService() {
        webTestClient.get()
                .uri("/fallback/authorization")
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectBody()
                .jsonPath("$.service").isEqualTo("authorization");
    }

    @Test
    void shouldReturnFallbackForUnknownService() {
        webTestClient.get()
                .uri("/fallback/unknown")
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectBody()
                .jsonPath("$.service").isEqualTo("unknown")
                .jsonPath("$.message").value(msg ->
                    ((String)msg).contains("unknown"));
    }

    @Test
    void shouldHandlePostRequests() {
        webTestClient.post()
                .uri("/fallback/patient")
                .exchange()
                .expectStatus().isEqualTo(503);
    }

    @Test
    void shouldHandlePutRequests() {
        webTestClient.put()
                .uri("/fallback/patient")
                .exchange()
                .expectStatus().isEqualTo(503);
    }

    @Test
    void shouldHandleDeleteRequests() {
        webTestClient.delete()
                .uri("/fallback/patient")
                .exchange()
                .expectStatus().isEqualTo(503);
    }
}
