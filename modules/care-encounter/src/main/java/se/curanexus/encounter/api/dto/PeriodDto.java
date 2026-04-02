package se.curanexus.encounter.api.dto;

import java.time.Instant;

public record PeriodDto(
        Instant start,
        Instant end
) {
}
