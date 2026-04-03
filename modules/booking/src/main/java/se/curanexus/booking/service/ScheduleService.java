package se.curanexus.booking.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.curanexus.booking.api.dto.ScheduleDto;
import se.curanexus.booking.domain.Schedule;
import se.curanexus.booking.domain.ScheduleRule;
import se.curanexus.booking.domain.TimeSlot;
import se.curanexus.booking.repository.ScheduleRepository;
import se.curanexus.booking.repository.TimeSlotRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service för att hantera scheman och generera tidsluckor.
 */
@Service
@Transactional
public class ScheduleService {

    private static final Logger log = LoggerFactory.getLogger(ScheduleService.class);
    private static final int DEFAULT_SLOT_DURATION_MINUTES = 30;

    private final ScheduleRepository scheduleRepository;
    private final TimeSlotRepository timeSlotRepository;

    public ScheduleService(ScheduleRepository scheduleRepository, TimeSlotRepository timeSlotRepository) {
        this.scheduleRepository = scheduleRepository;
        this.timeSlotRepository = timeSlotRepository;
    }

    /**
     * Skapa ett nytt schema.
     */
    public ScheduleDto createSchedule(Schedule schedule) {
        log.info("Creating schedule for practitioner {} at unit {}",
                schedule.getPractitionerId(), schedule.getUnitId());

        Schedule saved = scheduleRepository.save(schedule);
        return ScheduleDto.from(saved);
    }

    /**
     * Hämta schema via ID.
     */
    @Transactional(readOnly = true)
    public ScheduleDto getSchedule(UUID scheduleId) {
        return scheduleRepository.findById(scheduleId)
                .map(ScheduleDto::from)
                .orElseThrow(() -> new ScheduleNotFoundException(scheduleId));
    }

    /**
     * Hämta scheman för en vårdgivare.
     */
    @Transactional(readOnly = true)
    public List<ScheduleDto> getSchedulesForPractitioner(UUID practitionerId) {
        return scheduleRepository.findByPractitionerId(practitionerId)
                .stream()
                .map(ScheduleDto::from)
                .toList();
    }

    /**
     * Hämta aktiva scheman för en enhet.
     */
    @Transactional(readOnly = true)
    public List<ScheduleDto> getActiveSchedulesForUnit(UUID unitId) {
        return scheduleRepository.findActiveByUnitId(unitId, LocalDate.now())
                .stream()
                .map(ScheduleDto::from)
                .toList();
    }

    /**
     * Uppdatera schema.
     */
    public ScheduleDto updateSchedule(UUID scheduleId, Schedule updated) {
        Schedule existing = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleNotFoundException(scheduleId));

        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setValidFrom(updated.getValidFrom());
        existing.setValidTo(updated.getValidTo());
        existing.setActive(updated.isActive());

        return ScheduleDto.from(scheduleRepository.save(existing));
    }

    /**
     * Lägg till regel i schema.
     */
    public ScheduleDto addRuleToSchedule(UUID scheduleId, ScheduleRule rule) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleNotFoundException(scheduleId));

        schedule.addRule(rule);
        return ScheduleDto.from(scheduleRepository.save(schedule));
    }

    /**
     * Ta bort regel från schema.
     */
    public ScheduleDto removeRuleFromSchedule(UUID scheduleId, UUID ruleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleNotFoundException(scheduleId));

        schedule.getRules().removeIf(r -> r.getId().equals(ruleId));
        return ScheduleDto.from(scheduleRepository.save(schedule));
    }

    /**
     * Generera tidsluckor för ett schema under en period.
     */
    public List<TimeSlot> generateTimeSlots(UUID scheduleId, LocalDate fromDate, LocalDate toDate) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleNotFoundException(scheduleId));

        if (!schedule.isActive()) {
            throw new IllegalStateException("Cannot generate slots for inactive schedule");
        }

        List<TimeSlot> generatedSlots = new ArrayList<>();
        LocalDate currentDate = fromDate;

        while (!currentDate.isAfter(toDate)) {
            // Check if date is within schedule validity
            if (schedule.getValidFrom() != null && currentDate.isBefore(schedule.getValidFrom())) {
                currentDate = currentDate.plusDays(1);
                continue;
            }
            if (schedule.getValidTo() != null && currentDate.isAfter(schedule.getValidTo())) {
                break;
            }

            DayOfWeek dayOfWeek = currentDate.getDayOfWeek();

            // Find rule for this day
            for (ScheduleRule rule : schedule.getRules()) {
                if (rule.getDayOfWeek() == dayOfWeek) {
                    List<TimeSlot> daySlots = generateSlotsForDay(schedule, rule, currentDate);
                    generatedSlots.addAll(daySlots);
                }
            }

            currentDate = currentDate.plusDays(1);
        }

        log.info("Generated {} time slots for schedule {} from {} to {}",
                generatedSlots.size(), scheduleId, fromDate, toDate);

        return timeSlotRepository.saveAll(generatedSlots);
    }

    private List<TimeSlot> generateSlotsForDay(Schedule schedule, ScheduleRule rule, LocalDate date) {
        List<TimeSlot> slots = new ArrayList<>();
        int slotDuration = rule.getSlotDurationMinutes() != null
                ? rule.getSlotDurationMinutes()
                : DEFAULT_SLOT_DURATION_MINUTES;

        LocalTime currentTime = rule.getStartTime();
        LocalTime endTime = rule.getEndTime();

        while (currentTime.plusMinutes(slotDuration).isBefore(endTime) ||
               currentTime.plusMinutes(slotDuration).equals(endTime)) {

            // Skip break time
            if (rule.getBreakStartTime() != null && rule.getBreakEndTime() != null) {
                if (!currentTime.isBefore(rule.getBreakStartTime()) &&
                    currentTime.isBefore(rule.getBreakEndTime())) {
                    currentTime = rule.getBreakEndTime();
                    continue;
                }
            }

            LocalDateTime slotStart = LocalDateTime.of(date, currentTime);
            LocalDateTime slotEnd = slotStart.plusMinutes(slotDuration);

            // Check if slot already exists
            if (!timeSlotRepository.existsByScheduleIdAndStartTime(schedule.getId(), slotStart)) {
                TimeSlot slot = new TimeSlot(schedule, slotStart, slotEnd);
                slots.add(slot);
            }

            currentTime = currentTime.plusMinutes(slotDuration);
        }

        return slots;
    }

    /**
     * Inaktivera schema.
     */
    public void deactivateSchedule(UUID scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleNotFoundException(scheduleId));

        schedule.setActive(false);
        scheduleRepository.save(schedule);
        log.info("Deactivated schedule {}", scheduleId);
    }
}
