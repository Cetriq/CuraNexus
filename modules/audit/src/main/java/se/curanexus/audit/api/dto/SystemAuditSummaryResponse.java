package se.curanexus.audit.api.dto;

import se.curanexus.audit.service.AuditService.SystemAuditSummary;

import java.time.LocalDate;
import java.util.Map;

public record SystemAuditSummaryResponse(
        LocalDate fromDate,
        LocalDate toDate,
        long totalEvents,
        long totalUsers,
        long totalPatientsAccessed,
        Map<String, Long> eventsByType,
        SecuritySummary securityEventsSummary
) {
    public record SecuritySummary(
            long totalLogins,
            long failedLogins,
            long permissionDenied,
            long emergencyAccess
    ) {
        public static SecuritySummary fromSummary(SystemAuditSummary.SecuritySummary summary) {
            return new SecuritySummary(
                    summary.totalLogins(),
                    summary.failedLogins(),
                    summary.permissionDenied(),
                    summary.emergencyAccess()
            );
        }
    }

    public static SystemAuditSummaryResponse fromSummary(SystemAuditSummary summary) {
        return new SystemAuditSummaryResponse(
                summary.fromDate(),
                summary.toDate(),
                summary.totalEvents(),
                summary.totalUsers(),
                summary.totalPatientsAccessed(),
                summary.eventsByType(),
                SecuritySummary.fromSummary(summary.securityEventsSummary())
        );
    }
}
