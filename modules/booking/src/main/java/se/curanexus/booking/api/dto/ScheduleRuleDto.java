package se.curanexus.booking.api.dto;

import se.curanexus.booking.domain.ScheduleRule;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

/**
 * DTO för schemaregel.
 */
public record ScheduleRuleDto(
        UUID id,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        LocalTime breakStartTime,
        LocalTime breakEndTime,
        Integer slotDurationMinutes
) {
    /**
     * Konvertera från domänentitet till DTO.
     */
    public static ScheduleRuleDto from(ScheduleRule rule) {
        return new ScheduleRuleDto(
                rule.getId(),
                rule.getDayOfWeek(),
                rule.getStartTime(),
                rule.getEndTime(),
                rule.getBreakStartTime(),
                rule.getBreakEndTime(),
                rule.getSlotDurationMinutes()
        );
    }
}
