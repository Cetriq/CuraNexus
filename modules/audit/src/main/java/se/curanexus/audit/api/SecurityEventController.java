package se.curanexus.audit.api;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.curanexus.audit.api.dto.SecurityEventRequest;
import se.curanexus.audit.api.dto.SecurityEventResponse;
import se.curanexus.audit.domain.SecurityEventType;
import se.curanexus.audit.service.AuditService;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit/security-events")
public class SecurityEventController {

    private final AuditService auditService;

    public SecurityEventController(AuditService auditService) {
        this.auditService = auditService;
    }

    @PostMapping
    public ResponseEntity<SecurityEventResponse> recordSecurityEvent(@Valid @RequestBody SecurityEventRequest request) {
        var event = auditService.recordSecurityEvent(
                request.userId(),
                request.username(),
                request.eventType(),
                request.success(),
                request.ipAddress(),
                request.userAgent(),
                request.details()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(SecurityEventResponse.fromEntity(event));
    }

    @PostMapping("/async")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void recordSecurityEventAsync(@Valid @RequestBody SecurityEventRequest request) {
        auditService.recordSecurityEventAsync(
                request.userId(),
                request.username(),
                request.eventType(),
                request.success(),
                request.ipAddress(),
                request.userAgent(),
                request.details()
        );
    }

    @GetMapping
    public ResponseEntity<Page<SecurityEventResponse>> searchSecurityEvents(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) SecurityEventType eventType,
            @RequestParam(required = false) Boolean success,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant toDate,
            Pageable pageable) {
        var page = auditService.searchSecurityEvents(userId, eventType, success, fromDate, toDate, pageable);
        return ResponseEntity.ok(page.map(SecurityEventResponse::fromEntity));
    }
}
