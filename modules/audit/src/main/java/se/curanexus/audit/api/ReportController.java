package se.curanexus.audit.api;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.curanexus.audit.api.dto.PatientAccessReportResponse;
import se.curanexus.audit.api.dto.SystemAuditSummaryResponse;
import se.curanexus.audit.api.dto.UserActivityReportResponse;
import se.curanexus.audit.service.AuditService;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit/reports")
public class ReportController {

    private final AuditService auditService;

    public ReportController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/user-activity/{userId}")
    public ResponseEntity<UserActivityReportResponse> getUserActivityReport(
            @PathVariable UUID userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        var report = auditService.generateUserActivityReport(userId, fromDate, toDate);
        return ResponseEntity.ok(UserActivityReportResponse.fromReport(report));
    }

    @GetMapping("/patient-access/{patientId}")
    public ResponseEntity<PatientAccessReportResponse> getPatientAccessReport(
            @PathVariable UUID patientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        var report = auditService.generatePatientAccessReport(patientId, fromDate, toDate);
        return ResponseEntity.ok(PatientAccessReportResponse.fromReport(report));
    }

    @GetMapping("/system-summary")
    public ResponseEntity<SystemAuditSummaryResponse> getSystemSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        var summary = auditService.generateSystemSummary(fromDate, toDate);
        return ResponseEntity.ok(SystemAuditSummaryResponse.fromSummary(summary));
    }
}
