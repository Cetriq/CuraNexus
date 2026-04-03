package se.curanexus.notification.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.curanexus.notification.service.DeadLetterQueueService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API for Dead Letter Queue management.
 * Provides monitoring, inspection, and reprocessing capabilities.
 */
@RestController
@RequestMapping("/api/v1/dlq")
@Tag(name = "Dead Letter Queue", description = "DLQ monitoring and management API")
public class DeadLetterQueueController {

    private final DeadLetterQueueService dlqService;

    public DeadLetterQueueController(DeadLetterQueueService dlqService) {
        this.dlqService = dlqService;
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get DLQ statistics",
            description = "Returns message count, consumer count, and queue status")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        return ResponseEntity.ok(dlqService.getStatistics());
    }

    @GetMapping("/messages")
    @Operation(summary = "Peek at DLQ messages",
            description = "Returns details of messages in the DLQ without removing them. " +
                    "Note: Due to RabbitMQ limitations, messages are temporarily removed and re-queued.")
    public ResponseEntity<List<Map<String, Object>>> peekMessages(
            @Parameter(description = "Maximum number of messages to return (default: 10, max: 100)")
            @RequestParam(defaultValue = "10") int limit) {

        int safeLimit = Math.min(Math.max(limit, 1), 100);
        return ResponseEntity.ok(dlqService.peekMessages(safeLimit));
    }

    @PostMapping("/reprocess")
    @Operation(summary = "Reprocess DLQ messages",
            description = "Moves messages from DLQ back to the main queue for reprocessing")
    public ResponseEntity<Map<String, Object>> reprocess(
            @Parameter(description = "If true, reprocess all messages. If false, reprocess one message.")
            @RequestParam(defaultValue = "false") boolean all) {

        Map<String, Object> result = new HashMap<>();

        if (all) {
            int count = dlqService.reprocessAll();
            result.put("action", "reprocess_all");
            result.put("messagesReprocessed", count);
            result.put("success", true);
        } else {
            boolean success = dlqService.reprocessOne();
            result.put("action", "reprocess_one");
            result.put("messagesReprocessed", success ? 1 : 0);
            result.put("success", success);
        }

        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/purge")
    @Operation(summary = "Purge all DLQ messages",
            description = "Permanently deletes all messages from the DLQ. Use with caution!")
    public ResponseEntity<Map<String, Object>> purge() {
        int count = dlqService.purge();

        Map<String, Object> result = new HashMap<>();
        result.put("action", "purge");
        result.put("messagesPurged", count);
        result.put("success", true);

        return ResponseEntity.ok(result);
    }
}
