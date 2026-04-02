package se.curanexus.audit.api.dto;

import se.curanexus.audit.service.AuditService.UserActivityReport;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

public record UserActivityReportResponse(
        UUID userId,
        String username,
        LocalDate fromDate,
        LocalDate toDate,
        long totalEvents,
        long accessCount,
        long modificationCount,
        long patientsAccessed,
        long loginCount,
        Map<String, Long> eventsByType
) {
    public static UserActivityReportResponse fromReport(UserActivityReport report) {
        return new UserActivityReportResponse(
                report.userId(),
                report.username(),
                report.fromDate(),
                report.toDate(),
                report.totalEvents(),
                report.accessCount(),
                report.modificationCount(),
                report.patientsAccessed(),
                report.loginCount(),
                report.eventsByType()
        );
    }
}
