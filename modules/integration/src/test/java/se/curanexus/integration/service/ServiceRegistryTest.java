package se.curanexus.integration.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;
import se.curanexus.integration.config.ServiceProperties;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ServiceRegistryTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    private ServiceProperties serviceProperties;
    private ServiceRegistry serviceRegistry;

    @BeforeEach
    void setUp() {
        serviceProperties = new ServiceProperties();

        ServiceProperties.ServiceConfig patientConfig = new ServiceProperties.ServiceConfig();
        patientConfig.setUrl("http://localhost:8080");
        patientConfig.setHealthPath("/actuator/health");

        ServiceProperties.ServiceConfig encounterConfig = new ServiceProperties.ServiceConfig();
        encounterConfig.setUrl("http://localhost:8081");
        encounterConfig.setHealthPath("/actuator/health");

        serviceProperties.setServices(Map.of(
                "patient", patientConfig,
                "encounter", encounterConfig
        ));

        org.mockito.Mockito.when(webClientBuilder.build()).thenReturn(webClient);
        serviceRegistry = new ServiceRegistry(serviceProperties, webClientBuilder);
    }

    @Test
    void shouldReturnAllServices() {
        StepVerifier.create(serviceRegistry.getAllServices().collectList())
                .assertNext(services -> {
                    assertEquals(2, services.size());
                    assertTrue(services.stream().anyMatch(s -> s.id().equals("patient")));
                    assertTrue(services.stream().anyMatch(s -> s.id().equals("encounter")));
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnCorrectServiceInfo() {
        StepVerifier.create(serviceRegistry.getAllServices()
                        .filter(s -> s.id().equals("patient"))
                        .next())
                .assertNext(service -> {
                    assertEquals("patient", service.id());
                    assertEquals("Patient Service (A1)", service.name());
                    assertEquals("http://localhost:8080", service.url());
                    assertEquals("/actuator/health", service.healthPath());
                    assertTrue(service.pathPrefixes().contains("/api/v1/patients"));
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnPathPrefixesForAllServices() {
        StepVerifier.create(serviceRegistry.getAllServices().collectList())
                .assertNext(services -> {
                    var patient = services.stream().filter(s -> s.id().equals("patient")).findFirst().orElseThrow();
                    var encounter = services.stream().filter(s -> s.id().equals("encounter")).findFirst().orElseThrow();

                    assertFalse(patient.pathPrefixes().isEmpty());
                    assertFalse(encounter.pathPrefixes().isEmpty());
                })
                .verifyComplete();
    }
}
