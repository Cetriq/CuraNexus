package se.curanexus.booking.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.curanexus.booking.api.dto.CreateWaitlistEntryRequest;
import se.curanexus.booking.api.dto.WaitlistEntryDto;
import se.curanexus.booking.domain.WaitlistEntry.WaitlistPriority;
import se.curanexus.booking.service.WaitlistService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * REST API för väntelistehantering.
 */
@RestController
@RequestMapping("/api/v1/waitlist")
@Tag(name = "Väntelista", description = "API för att hantera väntelista")
public class WaitlistController {

    private final WaitlistService waitlistService;

    public WaitlistController(WaitlistService waitlistService) {
        this.waitlistService = waitlistService;
    }

    @PostMapping
    @Operation(summary = "Lägg till på väntelista", description = "Lägger till en patient på väntelistan")
    public ResponseEntity<WaitlistEntryDto> addToWaitlist(
            @Valid @RequestBody CreateWaitlistEntryRequest request) {
        WaitlistEntryDto entry = waitlistService.addToWaitlist(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(entry);
    }

    @GetMapping("/{entryId}")
    @Operation(summary = "Hämta väntelistpost", description = "Hämtar en väntelistpost via ID")
    public ResponseEntity<WaitlistEntryDto> getWaitlistEntry(
            @Parameter(description = "Post-ID") @PathVariable UUID entryId) {
        return ResponseEntity.ok(waitlistService.getWaitlistEntry(entryId));
    }

    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Hämta patientens poster", description = "Hämtar patientens aktiva väntelistposter")
    public ResponseEntity<List<WaitlistEntryDto>> getPatientWaitlistEntries(
            @Parameter(description = "Patient-ID") @PathVariable UUID patientId) {
        return ResponseEntity.ok(waitlistService.getPatientWaitlistEntries(patientId));
    }

    @GetMapping("/practitioner/{practitionerId}")
    @Operation(summary = "Hämta vårdgivarens väntelista", description = "Hämtar väntelista för en vårdgivare")
    public ResponseEntity<List<WaitlistEntryDto>> getPractitionerWaitlist(
            @Parameter(description = "Vårdgivar-ID") @PathVariable UUID practitionerId) {
        return ResponseEntity.ok(waitlistService.getPractitionerWaitlist(practitionerId));
    }

    @GetMapping("/unit/{unitId}")
    @Operation(summary = "Hämta enhetens väntelista", description = "Hämtar väntelista för en enhet")
    public ResponseEntity<List<WaitlistEntryDto>> getUnitWaitlist(
            @Parameter(description = "Enhets-ID") @PathVariable UUID unitId) {
        return ResponseEntity.ok(waitlistService.getUnitWaitlist(unitId));
    }

    @PostMapping("/{entryId}/notify")
    @Operation(summary = "Notifiera patient", description = "Skickar notifiering till patient om tillgänglig tid")
    public ResponseEntity<WaitlistEntryDto> notifyPatient(
            @Parameter(description = "Post-ID") @PathVariable UUID entryId) {
        return ResponseEntity.ok(waitlistService.notifyPatient(entryId));
    }

    @PostMapping("/{entryId}/book/{appointmentId}")
    @Operation(summary = "Markera som bokad", description = "Markerar väntelistposten som bokad")
    public ResponseEntity<WaitlistEntryDto> markAsBooked(
            @Parameter(description = "Post-ID") @PathVariable UUID entryId,
            @Parameter(description = "Boknings-ID") @PathVariable UUID appointmentId) {
        return ResponseEntity.ok(waitlistService.markAsBooked(entryId, appointmentId));
    }

    @DeleteMapping("/{entryId}")
    @Operation(summary = "Avbryt väntelistpost", description = "Avbryter en väntelistpost")
    public ResponseEntity<WaitlistEntryDto> cancelWaitlistEntry(
            @Parameter(description = "Post-ID") @PathVariable UUID entryId) {
        return ResponseEntity.ok(waitlistService.cancelWaitlistEntry(entryId));
    }

    @GetMapping("/matching")
    @Operation(summary = "Hitta matchande poster", description = "Hittar poster som matchar en tillgänglig tid")
    public ResponseEntity<List<WaitlistEntryDto>> findMatchingForAvailableSlot(
            @Parameter(description = "Tjänstetyp") @RequestParam String serviceType,
            @Parameter(description = "Datum") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(waitlistService.findMatchingForAvailableSlot(serviceType, date));
    }

    @PutMapping("/{entryId}/priority")
    @Operation(summary = "Uppdatera prioritet", description = "Uppdaterar prioritet för en väntelistpost")
    public ResponseEntity<WaitlistEntryDto> updatePriority(
            @Parameter(description = "Post-ID") @PathVariable UUID entryId,
            @Parameter(description = "Ny prioritet") @RequestParam WaitlistPriority priority) {
        return ResponseEntity.ok(waitlistService.updatePriority(entryId, priority));
    }

    @GetMapping("/count/practitioner/{practitionerId}")
    @Operation(summary = "Räkna väntande", description = "Räknar antal väntande för en vårdgivare")
    public ResponseEntity<Long> countWaitingByPractitioner(
            @Parameter(description = "Vårdgivar-ID") @PathVariable UUID practitionerId) {
        return ResponseEntity.ok(waitlistService.countWaitingByPractitioner(practitionerId));
    }

    @GetMapping("/count/unit/{unitId}")
    @Operation(summary = "Räkna väntande", description = "Räknar antal väntande för en enhet")
    public ResponseEntity<Long> countWaitingByUnit(
            @Parameter(description = "Enhets-ID") @PathVariable UUID unitId) {
        return ResponseEntity.ok(waitlistService.countWaitingByUnit(unitId));
    }
}
