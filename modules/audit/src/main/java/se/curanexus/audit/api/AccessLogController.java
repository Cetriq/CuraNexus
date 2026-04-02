package se.curanexus.audit.api;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.curanexus.audit.api.dto.AccessLogRequest;
import se.curanexus.audit.api.dto.AccessLogResponse;
import se.curanexus.audit.service.AuditService;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit/access-logs")
public class AccessLogController {

    private final AuditService auditService;

    public AccessLogController(AuditService auditService) {
        this.auditService = auditService;
    }

    @PostMapping
    public ResponseEntity<AccessLogResponse> recordAccess(@Valid @RequestBody AccessLogRequest request) {
        var log = auditService.recordAccess(
                request.userId(),
                request.username(),
                request.patientId(),
                request.resourceType(),
                request.resourceId(),
                request.accessType(),
                request.careRelationId(),
                request.careRelationType(),
                request.reason(),
                request.ipAddress()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(AccessLogResponse.fromEntity(log));
    }

    @PostMapping("/async")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void recordAccessAsync(@Valid @RequestBody AccessLogRequest request) {
        auditService.recordAccessAsync(
                request.userId(),
                request.username(),
                request.patientId(),
                request.resourceType(),
                request.resourceId(),
                request.accessType(),
                request.careRelationId(),
                request.careRelationType(),
                request.reason(),
                request.ipAddress()
        );
    }

    @GetMapping
    public ResponseEntity<Page<AccessLogResponse>> searchAccessLogs(
            @RequestParam(required = false) UUID patientId,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant toDate,
            Pageable pageable) {
        var page = auditService.searchAccessLogs(patientId, userId, fromDate, toDate, pageable);
        return ResponseEntity.ok(page.map(AccessLogResponse::fromEntity));
    }

    @GetMapping("/patient/{patientId}/history")
    public ResponseEntity<List<AccessLogResponse>> getPatientAccessHistory(
            @PathVariable UUID patientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant toDate) {
        var logs = auditService.getPatientAccessHistory(patientId, fromDate, toDate);
        return ResponseEntity.ok(logs.stream().map(AccessLogResponse::fromEntity).toList());
    }
}
