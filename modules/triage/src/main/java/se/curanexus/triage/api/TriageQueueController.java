package se.curanexus.triage.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.curanexus.triage.api.dto.TriageQueueResponse;
import se.curanexus.triage.service.TriageService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/triage/queue")
public class TriageQueueController {

    private final TriageService triageService;

    public TriageQueueController(TriageService triageService) {
        this.triageService = triageService;
    }

    @GetMapping
    public ResponseEntity<TriageQueueResponse> getTriageQueue(
            @RequestParam(required = false) UUID locationId) {
        var queueInfo = triageService.getTriageQueue(locationId);
        return ResponseEntity.ok(TriageQueueResponse.fromQueueInfo(queueInfo));
    }
}
