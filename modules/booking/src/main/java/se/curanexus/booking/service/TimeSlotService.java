package se.curanexus.booking.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.curanexus.booking.api.dto.TimeSlotDto;
import se.curanexus.booking.domain.TimeSlot;
import se.curanexus.booking.repository.TimeSlotRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service för att hantera tidsluckor.
 */
@Service
@Transactional
public class TimeSlotService {

    private static final Logger log = LoggerFactory.getLogger(TimeSlotService.class);

    private final TimeSlotRepository timeSlotRepository;

    public TimeSlotService(TimeSlotRepository timeSlotRepository) {
        this.timeSlotRepository = timeSlotRepository;
    }

    /**
     * Hämta tidslucka via ID.
     */
    @Transactional(readOnly = true)
    public TimeSlotDto getTimeSlot(UUID slotId) {
        return timeSlotRepository.findById(slotId)
                .map(TimeSlotDto::from)
                .orElseThrow(() -> new TimeSlotNotFoundException(slotId));
    }

    /**
     * Hämta tillgängliga tidsluckor för ett schema under en period.
     */
    @Transactional(readOnly = true)
    public List<TimeSlotDto> getAvailableSlots(UUID scheduleId, LocalDate fromDate, LocalDate toDate) {
        LocalDateTime from = fromDate.atStartOfDay();
        LocalDateTime to = toDate.plusDays(1).atStartOfDay();

        return timeSlotRepository.findAvailableByScheduleAndDateRange(scheduleId, from, to)
                .stream()
                .map(TimeSlotDto::from)
                .toList();
    }

    /**
     * Hämta tillgängliga tidsluckor för en vårdgivare under en period.
     */
    @Transactional(readOnly = true)
    public List<TimeSlotDto> getAvailableSlotsForPractitioner(UUID practitionerId, LocalDate fromDate, LocalDate toDate) {
        LocalDateTime from = fromDate.atStartOfDay();
        LocalDateTime to = toDate.plusDays(1).atStartOfDay();

        return timeSlotRepository.findAvailableByPractitionerAndDateRange(practitionerId, from, to)
                .stream()
                .map(TimeSlotDto::from)
                .toList();
    }

    /**
     * Hämta tillgängliga tidsluckor för en enhet under en period.
     */
    @Transactional(readOnly = true)
    public List<TimeSlotDto> getAvailableSlotsForUnit(UUID unitId, LocalDate fromDate, LocalDate toDate) {
        LocalDateTime from = fromDate.atStartOfDay();
        LocalDateTime to = toDate.plusDays(1).atStartOfDay();

        return timeSlotRepository.findAvailableByUnitAndDateRange(unitId, from, to)
                .stream()
                .map(TimeSlotDto::from)
                .toList();
    }

    /**
     * Blockera en tidslucka.
     */
    public TimeSlotDto blockSlot(UUID slotId, String reason) {
        TimeSlot slot = timeSlotRepository.findById(slotId)
                .orElseThrow(() -> new TimeSlotNotFoundException(slotId));

        slot.block(reason);
        TimeSlot saved = timeSlotRepository.save(slot);

        log.info("Blocked time slot {} - reason: {}", slotId, reason);
        return TimeSlotDto.from(saved);
    }

    /**
     * Avblockera en tidslucka.
     */
    public TimeSlotDto unblockSlot(UUID slotId) {
        TimeSlot slot = timeSlotRepository.findById(slotId)
                .orElseThrow(() -> new TimeSlotNotFoundException(slotId));

        slot.unblock();
        TimeSlot saved = timeSlotRepository.save(slot);

        log.info("Unblocked time slot {}", slotId);
        return TimeSlotDto.from(saved);
    }

    /**
     * Ta bort tidsluckor för ett schema under en period.
     * Kan endast ta bort tidsluckor som inte är bokade.
     */
    public int deleteUnbookedSlots(UUID scheduleId, LocalDate fromDate, LocalDate toDate) {
        LocalDateTime from = fromDate.atStartOfDay();
        LocalDateTime to = toDate.plusDays(1).atStartOfDay();

        List<TimeSlot> slots = timeSlotRepository.findByScheduleIdAndStartTimeBetween(scheduleId, from, to);

        int deleted = 0;
        for (TimeSlot slot : slots) {
            if (slot.isAvailable()) {
                timeSlotRepository.delete(slot);
                deleted++;
            }
        }

        log.info("Deleted {} unbooked time slots for schedule {} from {} to {}",
                deleted, scheduleId, fromDate, toDate);
        return deleted;
    }

    /**
     * Aktivera overbooking för en tidslucka.
     */
    public TimeSlotDto enableOverbooking(UUID slotId, int maxBookings) {
        TimeSlot slot = timeSlotRepository.findById(slotId)
                .orElseThrow(() -> new TimeSlotNotFoundException(slotId));

        slot.setOverbookable(true);
        slot.setMaxOverbook(maxBookings);
        TimeSlot saved = timeSlotRepository.save(slot);

        log.info("Enabled overbooking for slot {} with max {}", slotId, maxBookings);
        return TimeSlotDto.from(saved);
    }
}
