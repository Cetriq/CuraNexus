package se.curanexus.integration.fhir;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FhirControllerIT {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void shouldReturnCapabilityStatement() {
        webTestClient.get()
                .uri("/fhir/metadata")
                .accept(MediaType.parseMediaType("application/fhir+json"))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/fhir+json")
                .expectBody()
                .jsonPath("$.resourceType").isEqualTo("CapabilityStatement")
                .jsonPath("$.status").isEqualTo("active")
                .jsonPath("$.fhirVersion").isEqualTo("4.0.1")
                .jsonPath("$.format").isArray()
                .jsonPath("$.rest").isArray()
                .jsonPath("$.rest[0].mode").isEqualTo("server");
    }

    @Test
    void shouldIncludePatientResourceInCapabilityStatement() {
        webTestClient.get()
                .uri("/fhir/metadata")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.rest[0].resource[?(@.type=='Patient')]").exists()
                .jsonPath("$.rest[0].resource[?(@.type=='Patient')].interaction").isArray();
    }

    @Test
    void shouldIncludeEncounterResourceInCapabilityStatement() {
        webTestClient.get()
                .uri("/fhir/metadata")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.rest[0].resource[?(@.type=='Encounter')]").exists();
    }

    @Test
    void shouldIncludeObservationResourceInCapabilityStatement() {
        webTestClient.get()
                .uri("/fhir/metadata")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.rest[0].resource[?(@.type=='Observation')]").exists();
    }

    @Test
    void shouldIncludeConditionResourceInCapabilityStatement() {
        webTestClient.get()
                .uri("/fhir/metadata")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.rest[0].resource[?(@.type=='Condition')]").exists();
    }

    @Test
    void shouldIncludeProcedureResourceInCapabilityStatement() {
        webTestClient.get()
                .uri("/fhir/metadata")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.rest[0].resource[?(@.type=='Procedure')]").exists();
    }

    @Test
    void shouldIncludeSecurityInformation() {
        webTestClient.get()
                .uri("/fhir/metadata")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.rest[0].security.cors").isEqualTo(true);
    }

    @Test
    void shouldIncludeSoftwareInformation() {
        webTestClient.get()
                .uri("/fhir/metadata")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.software.name").isEqualTo("Cura Nexus Gateway")
                .jsonPath("$.software.version").isEqualTo("1.0.0");
    }
}
