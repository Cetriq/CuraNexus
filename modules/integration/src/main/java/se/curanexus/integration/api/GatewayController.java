package se.curanexus.integration.api;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.curanexus.integration.service.ServiceRegistry;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/gateway")
public class GatewayController {

    private final ServiceRegistry serviceRegistry;
    private final RouteLocator routeLocator;

    public GatewayController(ServiceRegistry serviceRegistry, RouteLocator routeLocator) {
        this.serviceRegistry = serviceRegistry;
        this.routeLocator = routeLocator;
    }

    @GetMapping("/health")
    public Mono<ResponseEntity<ServiceRegistry.AggregatedHealth>> getAggregatedHealth() {
        return serviceRegistry.checkAllServicesHealth()
                .map(health -> {
                    HttpStatus status = switch (health.status()) {
                        case "UP" -> HttpStatus.OK;
                        case "DOWN" -> HttpStatus.SERVICE_UNAVAILABLE;
                        default -> HttpStatus.OK; // DEGRADED still returns 200
                    };
                    return ResponseEntity.status(status).body(health);
                });
    }

    @GetMapping("/services")
    public Flux<ServiceRegistry.ServiceInfo> listServices() {
        return serviceRegistry.getAllServices();
    }

    @GetMapping("/services/{serviceId}/health")
    public Mono<ResponseEntity<ServiceRegistry.ServiceHealth>> getServiceHealth(@PathVariable String serviceId) {
        return serviceRegistry.checkServiceHealth(serviceId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/routes")
    public Flux<RouteInfo> listRoutes() {
        return routeLocator.getRoutes()
                .map(route -> new RouteInfo(
                        route.getId(),
                        route.getUri().toString(),
                        route.getPredicate().toString(),
                        route.getFilters().stream().map(Object::toString).toList(),
                        route.getOrder()
                ));
    }

    @GetMapping("/metrics")
    public Mono<GatewayMetrics> getMetrics() {
        // Simplified metrics - in production, integrate with Micrometer
        return Mono.just(new GatewayMetrics(
                0L,
                0,
                0.0,
                0.0,
                Map.of(
                        "patientCircuitBreaker", "CLOSED",
                        "encounterCircuitBreaker", "CLOSED",
                        "journalCircuitBreaker", "CLOSED",
                        "taskCircuitBreaker", "CLOSED",
                        "authorizationCircuitBreaker", "CLOSED"
                )
        ));
    }

    public record RouteInfo(
            String id,
            String uri,
            String predicates,
            List<String> filters,
            int order
    ) {}

    public record GatewayMetrics(
            long totalRequests,
            int activeConnections,
            double requestsPerSecond,
            double averageResponseTime,
            Map<String, String> circuitBreakerStatus
    ) {}
}
