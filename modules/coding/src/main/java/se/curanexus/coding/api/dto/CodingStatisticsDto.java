package se.curanexus.coding.api.dto;

import se.curanexus.coding.service.CodingService;

public record CodingStatisticsDto(
        long diagnosisCodes,
        long procedureCodes,
        long medicationCodes,
        long totalCodes
) {
    public static CodingStatisticsDto from(CodingService.CodingStatistics stats) {
        return new CodingStatisticsDto(
                stats.diagnosisCodes(),
                stats.procedureCodes(),
                stats.medicationCodes(),
                stats.totalCodes()
        );
    }
}
