package se.curanexus.audit.api;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.curanexus.audit.api.dto.ChangeLogRequest;
import se.curanexus.audit.api.dto.ChangeLogResponse;
import se.curanexus.audit.domain.ChangeType;
import se.curanexus.audit.domain.ResourceType;
import se.curanexus.audit.service.AuditService;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit/change-logs")
public class ChangeLogController {

    private final AuditService auditService;

    public ChangeLogController(AuditService auditService) {
        this.auditService = auditService;
    }

    @PostMapping
    public ResponseEntity<ChangeLogResponse> recordChange(@Valid @RequestBody ChangeLogRequest request) {
        var log = auditService.recordChange(
                request.userId(),
                request.username(),
                request.resourceType(),
                request.resourceId(),
                request.patientId(),
                request.changeType(),
                request.fieldName(),
                request.oldValue(),
                request.newValue()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ChangeLogResponse.fromEntity(log));
    }

    @PostMapping("/async")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void recordChangeAsync(@Valid @RequestBody ChangeLogRequest request) {
        auditService.recordChangeAsync(
                request.userId(),
                request.username(),
                request.resourceType(),
                request.resourceId(),
                request.patientId(),
                request.changeType(),
                request.fieldName(),
                request.oldValue(),
                request.newValue()
        );
    }

    @GetMapping
    public ResponseEntity<Page<ChangeLogResponse>> searchChangeLogs(
            @RequestParam(required = false) ResourceType resourceType,
            @RequestParam(required = false) UUID resourceId,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) ChangeType changeType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant toDate,
            Pageable pageable) {
        var page = auditService.searchChangeLogs(resourceType, resourceId, userId, changeType, fromDate, toDate, pageable);
        return ResponseEntity.ok(page.map(ChangeLogResponse::fromEntity));
    }

    @GetMapping("/resource/{resourceType}/{resourceId}/history")
    public ResponseEntity<List<ChangeLogResponse>> getResourceHistory(
            @PathVariable ResourceType resourceType,
            @PathVariable UUID resourceId) {
        var logs = auditService.getResourceHistory(resourceType, resourceId);
        return ResponseEntity.ok(logs.stream().map(ChangeLogResponse::fromEntity).toList());
    }
}
