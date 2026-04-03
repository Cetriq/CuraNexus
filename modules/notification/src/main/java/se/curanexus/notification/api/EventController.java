package se.curanexus.notification.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.curanexus.notification.api.dto.StoredEventDto;
import se.curanexus.notification.service.EventStoreService;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
@Tag(name = "Events", description = "Event store API for querying domain events")
public class EventController {

    private final EventStoreService eventStoreService;

    public EventController(EventStoreService eventStoreService) {
        this.eventStoreService = eventStoreService;
    }

    @GetMapping("/aggregate/{aggregateId}")
    @Operation(summary = "Get events by aggregate ID",
            description = "Retrieve all events for a specific aggregate (e.g., a specific encounter)")
    public ResponseEntity<Page<StoredEventDto>> getEventsByAggregateId(
            @PathVariable UUID aggregateId,
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(
                eventStoreService.getEventsByAggregateId(aggregateId, pageable)
                        .map(StoredEventDto::from)
        );
    }

    @GetMapping("/type/{aggregateType}")
    @Operation(summary = "Get events by aggregate type",
            description = "Retrieve all events for a type of aggregate (e.g., ENCOUNTER, TASK, NOTE)")
    public ResponseEntity<Page<StoredEventDto>> getEventsByAggregateType(
            @PathVariable String aggregateType,
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(
                eventStoreService.getEventsByAggregateType(aggregateType.toUpperCase(), pageable)
                        .map(StoredEventDto::from)
        );
    }

    @GetMapping("/event-type/{eventType}")
    @Operation(summary = "Get events by event type",
            description = "Retrieve all events of a specific type (e.g., CREATED, STATUS_CHANGED)")
    public ResponseEntity<Page<StoredEventDto>> getEventsByEventType(
            @PathVariable String eventType,
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(
                eventStoreService.getEventsByEventType(eventType.toUpperCase(), pageable)
                        .map(StoredEventDto::from)
        );
    }

    @GetMapping("/type/{aggregateType}/range")
    @Operation(summary = "Get events by aggregate type and date range",
            description = "Retrieve events for an aggregate type within a specific time range")
    public ResponseEntity<Page<StoredEventDto>> getEventsByAggregateTypeAndDateRange(
            @PathVariable String aggregateType,
            @Parameter(description = "Start of date range (ISO 8601)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fromDate,
            @Parameter(description = "End of date range (ISO 8601)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant toDate,
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(
                eventStoreService.getEventsByAggregateTypeAndDateRange(
                                aggregateType.toUpperCase(), fromDate, toDate, pageable)
                        .map(StoredEventDto::from)
        );
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get event statistics",
            description = "Get counts of events by aggregate type")
    public ResponseEntity<Map<String, Long>> getEventStatistics() {
        return ResponseEntity.ok(eventStoreService.getEventStatistics());
    }
}
