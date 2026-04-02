package se.curanexus.integration.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Instant;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @RequestMapping(value = "/{service}", method = {
            RequestMethod.GET,
            RequestMethod.POST,
            RequestMethod.PUT,
            RequestMethod.DELETE,
            RequestMethod.PATCH
    })
    public Mono<ResponseEntity<FallbackResponse>> fallback(@PathVariable String service) {
        String message = getServiceMessage(service);
        FallbackResponse response = new FallbackResponse(
                "SERVICE_UNAVAILABLE",
                service,
                message,
                Instant.now(),
                30
        );
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    private String getServiceMessage(String service) {
        return switch (service) {
            case "patient" -> "Patient service is currently unavailable. Please try again later.";
            case "encounter" -> "Encounter service is currently unavailable. Please try again later.";
            case "journal" -> "Journal service is currently unavailable. Please try again later.";
            case "task" -> "Task service is currently unavailable. Please try again later.";
            case "authorization" -> "Authorization service is currently unavailable. Please try again later.";
            default -> "Service '" + service + "' is currently unavailable. Please try again later.";
        };
    }

    public record FallbackResponse(
            String error,
            String service,
            String message,
            Instant timestamp,
            int retryAfter
    ) {}
}
