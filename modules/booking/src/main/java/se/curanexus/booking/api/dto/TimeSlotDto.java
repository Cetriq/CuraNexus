package se.curanexus.booking.api.dto;

import se.curanexus.booking.domain.TimeSlot;
import se.curanexus.booking.domain.TimeSlot.SlotStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO för tidslucka.
 */
public record TimeSlotDto(
        UUID id,
        UUID scheduleId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        SlotStatus status,
        int currentBookings,
        int maxBookings,
        boolean overbookable,
        String serviceType
) {
    /**
     * Konvertera från domänentitet till DTO.
     */
    public static TimeSlotDto from(TimeSlot slot) {
        return new TimeSlotDto(
                slot.getId(),
                slot.getSchedule() != null ? slot.getSchedule().getId() : null,
                slot.getStartTime(),
                slot.getEndTime(),
                slot.getStatus(),
                slot.getCurrentBookings(),
                slot.getMaxOverbook(),
                slot.isOverbookable(),
                slot.getServiceType()
        );
    }
}
