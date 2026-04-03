package se.curanexus.booking.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.curanexus.booking.api.dto.ScheduleDto;
import se.curanexus.booking.api.dto.TimeSlotDto;
import se.curanexus.booking.domain.Schedule;
import se.curanexus.booking.domain.ScheduleRule;
import se.curanexus.booking.domain.TimeSlot;
import se.curanexus.booking.service.ScheduleService;
import se.curanexus.booking.service.TimeSlotService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * REST API för schemahantering.
 */
@RestController
@RequestMapping("/api/v1/schedules")
@Tag(name = "Scheman", description = "API för att hantera scheman och tidsluckor")
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final TimeSlotService timeSlotService;

    public ScheduleController(ScheduleService scheduleService, TimeSlotService timeSlotService) {
        this.scheduleService = scheduleService;
        this.timeSlotService = timeSlotService;
    }

    @PostMapping
    @Operation(summary = "Skapa schema", description = "Skapar ett nytt schema för en vårdgivare")
    public ResponseEntity<ScheduleDto> createSchedule(@Valid @RequestBody Schedule schedule) {
        ScheduleDto created = scheduleService.createSchedule(schedule);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{scheduleId}")
    @Operation(summary = "Hämta schema", description = "Hämtar ett schema via ID")
    public ResponseEntity<ScheduleDto> getSchedule(
            @Parameter(description = "Schema-ID") @PathVariable UUID scheduleId) {
        return ResponseEntity.ok(scheduleService.getSchedule(scheduleId));
    }

    @GetMapping("/practitioner/{practitionerId}")
    @Operation(summary = "Hämta vårdgivarens scheman", description = "Hämtar alla scheman för en vårdgivare")
    public ResponseEntity<List<ScheduleDto>> getPractitionerSchedules(
            @Parameter(description = "Vårdgivar-ID") @PathVariable UUID practitionerId) {
        return ResponseEntity.ok(scheduleService.getSchedulesForPractitioner(practitionerId));
    }

    @GetMapping("/unit/{unitId}")
    @Operation(summary = "Hämta enhetens aktiva scheman", description = "Hämtar alla aktiva scheman för en enhet")
    public ResponseEntity<List<ScheduleDto>> getUnitSchedules(
            @Parameter(description = "Enhets-ID") @PathVariable UUID unitId) {
        return ResponseEntity.ok(scheduleService.getActiveSchedulesForUnit(unitId));
    }

    @PutMapping("/{scheduleId}")
    @Operation(summary = "Uppdatera schema", description = "Uppdaterar ett befintligt schema")
    public ResponseEntity<ScheduleDto> updateSchedule(
            @Parameter(description = "Schema-ID") @PathVariable UUID scheduleId,
            @Valid @RequestBody Schedule schedule) {
        return ResponseEntity.ok(scheduleService.updateSchedule(scheduleId, schedule));
    }

    @PostMapping("/{scheduleId}/rules")
    @Operation(summary = "Lägg till schemaregel", description = "Lägger till en regel i schemat")
    public ResponseEntity<ScheduleDto> addRule(
            @Parameter(description = "Schema-ID") @PathVariable UUID scheduleId,
            @Valid @RequestBody ScheduleRule rule) {
        return ResponseEntity.ok(scheduleService.addRuleToSchedule(scheduleId, rule));
    }

    @DeleteMapping("/{scheduleId}/rules/{ruleId}")
    @Operation(summary = "Ta bort schemaregel", description = "Tar bort en regel från schemat")
    public ResponseEntity<ScheduleDto> removeRule(
            @Parameter(description = "Schema-ID") @PathVariable UUID scheduleId,
            @Parameter(description = "Regel-ID") @PathVariable UUID ruleId) {
        return ResponseEntity.ok(scheduleService.removeRuleFromSchedule(scheduleId, ruleId));
    }

    @PostMapping("/{scheduleId}/generate-slots")
    @Operation(summary = "Generera tidsluckor", description = "Genererar tidsluckor från schemat för en period")
    public ResponseEntity<List<TimeSlotDto>> generateTimeSlots(
            @Parameter(description = "Schema-ID") @PathVariable UUID scheduleId,
            @Parameter(description = "Från datum") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "Till datum") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

        List<TimeSlot> slots = scheduleService.generateTimeSlots(scheduleId, fromDate, toDate);
        List<TimeSlotDto> dtos = slots.stream().map(TimeSlotDto::from).toList();
        return ResponseEntity.ok(dtos);
    }

    @DeleteMapping("/{scheduleId}")
    @Operation(summary = "Inaktivera schema", description = "Inaktiverar ett schema")
    public ResponseEntity<Void> deactivateSchedule(
            @Parameter(description = "Schema-ID") @PathVariable UUID scheduleId) {
        scheduleService.deactivateSchedule(scheduleId);
        return ResponseEntity.noContent().build();
    }

    // Time slot endpoints

    @GetMapping("/{scheduleId}/slots")
    @Operation(summary = "Hämta tillgängliga tider", description = "Hämtar tillgängliga tidsluckor för ett schema")
    public ResponseEntity<List<TimeSlotDto>> getAvailableSlots(
            @Parameter(description = "Schema-ID") @PathVariable UUID scheduleId,
            @Parameter(description = "Från datum") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "Till datum") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(timeSlotService.getAvailableSlots(scheduleId, fromDate, toDate));
    }

    @GetMapping("/slots/practitioner/{practitionerId}")
    @Operation(summary = "Hämta vårdgivarens tider", description = "Hämtar tillgängliga tider för en vårdgivare")
    public ResponseEntity<List<TimeSlotDto>> getAvailableSlotsForPractitioner(
            @Parameter(description = "Vårdgivar-ID") @PathVariable UUID practitionerId,
            @Parameter(description = "Från datum") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "Till datum") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(timeSlotService.getAvailableSlotsForPractitioner(practitionerId, fromDate, toDate));
    }

    @GetMapping("/slots/unit/{unitId}")
    @Operation(summary = "Hämta enhetens tider", description = "Hämtar tillgängliga tider för en enhet")
    public ResponseEntity<List<TimeSlotDto>> getAvailableSlotsForUnit(
            @Parameter(description = "Enhets-ID") @PathVariable UUID unitId,
            @Parameter(description = "Från datum") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "Till datum") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(timeSlotService.getAvailableSlotsForUnit(unitId, fromDate, toDate));
    }

    @PostMapping("/slots/{slotId}/block")
    @Operation(summary = "Blockera tid", description = "Blockerar en tidslucka")
    public ResponseEntity<TimeSlotDto> blockSlot(
            @Parameter(description = "Tidslucke-ID") @PathVariable UUID slotId,
            @Parameter(description = "Anledning") @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(timeSlotService.blockSlot(slotId, reason));
    }

    @PostMapping("/slots/{slotId}/unblock")
    @Operation(summary = "Avblockera tid", description = "Avblockerar en tidslucka")
    public ResponseEntity<TimeSlotDto> unblockSlot(
            @Parameter(description = "Tidslucke-ID") @PathVariable UUID slotId) {
        return ResponseEntity.ok(timeSlotService.unblockSlot(slotId));
    }
}
