package se.curanexus.audit.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.curanexus.audit.api.dto.*;
import se.curanexus.audit.domain.ResourceType;
import se.curanexus.audit.service.AuditService;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit")
@Tag(name = "Audit", description = "API för åtkomstloggning och spårbarhet (PDL)")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @PostMapping("/events")
    @Operation(summary = "Logga en audit-händelse")
    public ResponseEntity<AuditEventDto> logEvent(@Valid @RequestBody CreateAuditEventRequest request) {
        AuditEventDto created = auditService.logEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/events/{eventId}/changes")
    @Operation(summary = "Logga dataändringar för en händelse")
    public ResponseEntity<Void> logDataChanges(@PathVariable UUID eventId,
            @Valid @RequestBody List<DataChangeRequest> changes) {
        auditService.logDataChanges(eventId, changes);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/events/{id}")
    @Operation(summary = "Hämta audit-händelse med ID")
    public AuditEventDto getEvent(@PathVariable UUID id) {
        return auditService.getEvent(id);
    }

    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Hämta åtkomstlogg för patient")
    public Page<AuditEventSummaryDto> getPatientAuditLog(@PathVariable UUID patientId, Pageable pageable) {
        return auditService.getPatientAuditLog(patientId, pageable);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Hämta åtkomstlogg för användare")
    public Page<AuditEventSummaryDto> getUserAuditLog(@PathVariable String userId, Pageable pageable) {
        return auditService.getUserAuditLog(userId, pageable);
    }

    @GetMapping("/care-unit/{careUnitId}")
    @Operation(summary = "Hämta åtkomstlogg för vårdenhet")
    public Page<AuditEventSummaryDto> getCareUnitAuditLog(@PathVariable UUID careUnitId, Pageable pageable) {
        return auditService.getCareUnitAuditLog(careUnitId, pageable);
    }

    @GetMapping("/resource/{resourceType}/{resourceId}")
    @Operation(summary = "Hämta historik för en specifik resurs")
    public List<AuditEventSummaryDto> getResourceHistory(@PathVariable ResourceType resourceType,
            @PathVariable UUID resourceId) {
        return auditService.getResourceHistory(resourceType, resourceId);
    }

    @GetMapping("/encounter/{encounterId}")
    @Operation(summary = "Hämta åtkomstlogg för vårdkontakt")
    public List<AuditEventSummaryDto> getEncounterAuditLog(@PathVariable UUID encounterId) {
        return auditService.getEncounterAuditLog(encounterId);
    }

    @GetMapping("/emergency-access")
    @Operation(summary = "Hämta nödöppningar")
    public Page<AuditEventSummaryDto> getEmergencyAccessEvents(
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            Pageable pageable) {
        return auditService.getEmergencyAccessEvents(from, to, pageable);
    }

    @GetMapping("/failed-attempts")
    @Operation(summary = "Hämta misslyckade åtkomstförsök")
    public Page<AuditEventSummaryDto> getFailedAccessAttempts(
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            Pageable pageable) {
        return auditService.getFailedAccessAttempts(from, to, pageable);
    }

    @PostMapping("/search")
    @Operation(summary = "Sök audit-händelser")
    public Page<AuditEventSummaryDto> search(@RequestBody AuditSearchRequest request, Pageable pageable) {
        return auditService.search(request, pageable);
    }

    @GetMapping("/events/{eventId}/changes")
    @Operation(summary = "Hämta dataändringar för händelse")
    public List<DataChangeLogDto> getDataChanges(@PathVariable UUID eventId) {
        return auditService.getDataChanges(eventId);
    }

    @GetMapping("/resource/{resourceType}/{resourceId}/changes")
    @Operation(summary = "Hämta ändringshistorik för resurs")
    public List<DataChangeLogDto> getResourceChangeHistory(@PathVariable ResourceType resourceType,
            @PathVariable UUID resourceId) {
        return auditService.getResourceChangeHistory(resourceType, resourceId);
    }

    @GetMapping("/statistics")
    @Operation(summary = "Hämta statistik")
    public AuditStatisticsDto getStatistics(
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to) {
        return auditService.getStatistics(from, to);
    }
}
