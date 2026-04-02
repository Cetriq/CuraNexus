package se.curanexus.audit.api;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.curanexus.audit.api.dto.AuditEventRequest;
import se.curanexus.audit.api.dto.AuditEventResponse;
import se.curanexus.audit.domain.AuditEventType;
import se.curanexus.audit.domain.ResourceType;
import se.curanexus.audit.service.AuditService;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit/events")
public class AuditEventController {

    private final AuditService auditService;

    public AuditEventController(AuditService auditService) {
        this.auditService = auditService;
    }

    @PostMapping
    public ResponseEntity<AuditEventResponse> recordEvent(@Valid @RequestBody AuditEventRequest request) {
        var event = auditService.recordEvent(
                request.eventType(),
                request.userId(),
                request.resourceType(),
                request.resourceId(),
                request.patientId(),
                request.action(),
                request.details(),
                request.ipAddress(),
                request.userAgent(),
                request.careRelationId(),
                request.reason(),
                request.username()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(AuditEventResponse.fromEntity(event));
    }

    @PostMapping("/async")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void recordEventAsync(@Valid @RequestBody AuditEventRequest request) {
        auditService.recordEventAsync(
                request.eventType(),
                request.userId(),
                request.resourceType(),
                request.resourceId(),
                request.patientId(),
                request.action(),
                request.details(),
                request.ipAddress(),
                request.userAgent(),
                request.careRelationId(),
                request.reason(),
                request.username()
        );
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<AuditEventResponse> getEvent(@PathVariable UUID eventId) {
        return auditService.getEvent(eventId)
                .map(AuditEventResponse::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<Page<AuditEventResponse>> searchEvents(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) ResourceType resourceType,
            @RequestParam(required = false) UUID resourceId,
            @RequestParam(required = false) AuditEventType eventType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant toDate,
            Pageable pageable) {
        var page = auditService.searchEvents(userId, resourceType, resourceId, eventType, fromDate, toDate, pageable);
        return ResponseEntity.ok(page.map(AuditEventResponse::fromEntity));
    }
}
