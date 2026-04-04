package se.curanexus.audit.api.dto;

import java.time.Instant;

public record AuditStatisticsDto(
    Instant from, Instant to, long totalEvents, long emergencyAccessEvents, long failedAttempts
) {}
