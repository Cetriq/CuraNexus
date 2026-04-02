package se.curanexus.audit.api.dto;

import se.curanexus.audit.service.AuditService.PatientAccessReport;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record PatientAccessReportResponse(
        UUID patientId,
        LocalDate fromDate,
        LocalDate toDate,
        long totalAccesses,
        long uniqueUsers,
        List<UserAccessStats> accessByUser,
        Map<String, Long> accessByResourceType
) {
    public record UserAccessStats(
            UUID userId,
            String username,
            int accessCount,
            String careRelationType,
            Instant lastAccess
    ) {
        public static UserAccessStats fromStats(PatientAccessReport.UserAccessStats stats) {
            return new UserAccessStats(
                    stats.userId(),
                    stats.username(),
                    stats.accessCount(),
                    stats.careRelationType(),
                    stats.lastAccess()
            );
        }
    }

    public static PatientAccessReportResponse fromReport(PatientAccessReport report) {
        return new PatientAccessReportResponse(
                report.patientId(),
                report.fromDate(),
                report.toDate(),
                report.totalAccesses(),
                report.uniqueUsers(),
                report.accessByUser().stream()
                        .map(UserAccessStats::fromStats)
                        .toList(),
                report.accessByResourceType()
        );
    }
}
