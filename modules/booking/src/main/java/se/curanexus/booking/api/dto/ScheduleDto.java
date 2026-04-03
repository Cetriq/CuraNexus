package se.curanexus.booking.api.dto;

import se.curanexus.booking.domain.Schedule;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * DTO för schema.
 */
public record ScheduleDto(
        UUID id,
        UUID practitionerId,
        String practitionerHsaId,
        UUID unitId,
        String unitHsaId,
        String name,
        String description,
        LocalDate validFrom,
        LocalDate validTo,
        boolean active,
        List<ScheduleRuleDto> rules
) {
    /**
     * Konvertera från domänentitet till DTO.
     */
    public static ScheduleDto from(Schedule schedule) {
        return new ScheduleDto(
                schedule.getId(),
                schedule.getPractitionerId(),
                schedule.getPractitionerHsaId(),
                schedule.getUnitId(),
                schedule.getUnitHsaId(),
                schedule.getName(),
                schedule.getDescription(),
                schedule.getValidFrom(),
                schedule.getValidTo(),
                schedule.isActive(),
                schedule.getRules().stream()
                        .map(ScheduleRuleDto::from)
                        .toList()
        );
    }
}
