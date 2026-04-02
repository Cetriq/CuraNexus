package se.curanexus.integration.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.curanexus.integration.config.ServiceProperties;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ServiceRegistry {

    private final ServiceProperties serviceProperties;
    private final WebClient webClient;
    private final Map<String, ServiceStatus> statusCache = new ConcurrentHashMap<>();

    public ServiceRegistry(ServiceProperties serviceProperties, WebClient.Builder webClientBuilder) {
        this.serviceProperties = serviceProperties;
        this.webClient = webClientBuilder.build();
    }

    public Flux<ServiceInfo> getAllServices() {
        return Flux.fromIterable(serviceProperties.getServices().entrySet())
                .map(entry -> {
                    ServiceProperties.ServiceConfig config = entry.getValue();
                    ServiceStatus status = statusCache.getOrDefault(entry.getKey(), ServiceStatus.unknown());
                    return new ServiceInfo(
                            entry.getKey(),
                            getServiceName(entry.getKey()),
                            config.getUrl(),
                            config.getHealthPath(),
                            status.status(),
                            getPathPrefixes(entry.getKey())
                    );
                });
    }

    public Mono<ServiceHealth> checkServiceHealth(String serviceId) {
        ServiceProperties.ServiceConfig config = serviceProperties.getServices().get(serviceId);
        if (config == null) {
            return Mono.empty();
        }

        long startTime = System.currentTimeMillis();
        return webClient.get()
                .uri(config.getUrl() + config.getHealthPath())
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    long responseTime = System.currentTimeMillis() - startTime;
                    String status = response.get("status") != null ? response.get("status").toString() : "UP";
                    ServiceStatus serviceStatus = new ServiceStatus(status, responseTime, Instant.now());
                    statusCache.put(serviceId, serviceStatus);
                    return new ServiceHealth(status, responseTime, Instant.now(), response);
                })
                .timeout(Duration.ofSeconds(config.getTimeoutSeconds()))
                .onErrorResume(e -> {
                    long responseTime = System.currentTimeMillis() - startTime;
                    ServiceStatus serviceStatus = new ServiceStatus("DOWN", responseTime, Instant.now());
                    statusCache.put(serviceId, serviceStatus);
                    return Mono.just(new ServiceHealth("DOWN", responseTime, Instant.now(),
                            Map.of("error", e.getMessage())));
                });
    }

    public Mono<AggregatedHealth> checkAllServicesHealth() {
        return Flux.fromIterable(serviceProperties.getServices().keySet())
                .flatMap(serviceId -> checkServiceHealth(serviceId)
                        .map(health -> Map.entry(serviceId, health)))
                .collectMap(Map.Entry::getKey, Map.Entry::getValue)
                .map(healthMap -> {
                    boolean allUp = healthMap.values().stream()
                            .allMatch(h -> "UP".equals(h.status()));
                    boolean allDown = healthMap.values().stream()
                            .allMatch(h -> "DOWN".equals(h.status()));
                    String overallStatus = allUp ? "UP" : (allDown ? "DOWN" : "DEGRADED");
                    return new AggregatedHealth(overallStatus, Instant.now(), healthMap);
                });
    }

    private String getServiceName(String serviceId) {
        return switch (serviceId) {
            case "patient" -> "Patient Service (A1)";
            case "encounter" -> "Care Encounter Service (A2)";
            case "journal" -> "Journal Service (A3)";
            case "task" -> "Task Service (B1)";
            case "authorization" -> "Authorization Service (C2)";
            default -> serviceId;
        };
    }

    private List<String> getPathPrefixes(String serviceId) {
        return switch (serviceId) {
            case "patient" -> List.of("/api/v1/patients");
            case "encounter" -> List.of("/api/v1/encounters");
            case "journal" -> List.of("/api/v1/notes", "/api/v1/diagnoses", "/api/v1/procedures", "/api/v1/observations");
            case "task" -> List.of("/api/v1/tasks", "/api/v1/reminders", "/api/v1/delegations", "/api/v1/watches");
            case "authorization" -> List.of("/api/v1/users", "/api/v1/roles", "/api/v1/permissions", "/api/v1/care-relations", "/api/v1/access");
            default -> List.of();
        };
    }

    public record ServiceInfo(
            String id,
            String name,
            String url,
            String healthPath,
            String status,
            List<String> pathPrefixes
    ) {}

    public record ServiceHealth(
            String status,
            long responseTime,
            Instant lastChecked,
            Map<String, Object> details
    ) {}

    public record ServiceStatus(
            String status,
            long responseTime,
            Instant lastChecked
    ) {
        public static ServiceStatus unknown() {
            return new ServiceStatus("UNKNOWN", 0, null);
        }
    }

    public record AggregatedHealth(
            String status,
            Instant timestamp,
            Map<String, ServiceHealth> services
    ) {}
}
