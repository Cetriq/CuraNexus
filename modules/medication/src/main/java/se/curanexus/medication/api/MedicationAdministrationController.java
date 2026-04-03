package se.curanexus.medication.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.curanexus.medication.api.dto.MedicationAdministrationDto;
import se.curanexus.medication.api.dto.RecordAdministrationRequest;
import se.curanexus.medication.service.MedicationAdministrationService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/medication-administrations")
@Tag(name = "Medication Administrations", description = "Läkemedelsadministrering")
public class MedicationAdministrationController {

    private final MedicationAdministrationService administrationService;

    public MedicationAdministrationController(MedicationAdministrationService administrationService) {
        this.administrationService = administrationService;
    }

    @PostMapping
    @Operation(summary = "Registrera läkemedelsadministrering")
    public ResponseEntity<MedicationAdministrationDto> recordAdministration(
            @Valid @RequestBody RecordAdministrationRequest request,
            @RequestHeader("X-User-Id") UUID performerId) {
        MedicationAdministrationDto administration = administrationService.recordAdministration(request, performerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(administration);
    }

    @GetMapping("/{administrationId}")
    @Operation(summary = "Hämta administrering via ID")
    public ResponseEntity<MedicationAdministrationDto> getAdministration(@PathVariable UUID administrationId) {
        return ResponseEntity.ok(administrationService.getAdministration(administrationId));
    }

    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Hämta administreringar för patient")
    public ResponseEntity<List<MedicationAdministrationDto>> getPatientAdministrations(
            @PathVariable UUID patientId) {
        return ResponseEntity.ok(administrationService.getPatientAdministrations(patientId));
    }

    @GetMapping("/patient/{patientId}/range")
    @Operation(summary = "Hämta administreringar för patient inom tidsintervall")
    public ResponseEntity<List<MedicationAdministrationDto>> getPatientAdministrationsInRange(
            @PathVariable UUID patientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(administrationService.getPatientAdministrations(patientId, from, to));
    }

    @GetMapping("/prescription/{prescriptionId}")
    @Operation(summary = "Hämta administreringar för ordination")
    public ResponseEntity<List<MedicationAdministrationDto>> getPrescriptionAdministrations(
            @PathVariable UUID prescriptionId) {
        return ResponseEntity.ok(administrationService.getPrescriptionAdministrations(prescriptionId));
    }

    @GetMapping("/patient/{patientId}/pending")
    @Operation(summary = "Hämta planerade administreringar för patient")
    public ResponseEntity<List<MedicationAdministrationDto>> getPendingAdministrations(
            @PathVariable UUID patientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime until) {
        return ResponseEntity.ok(administrationService.getPendingAdministrations(patientId, until));
    }

    @PostMapping("/{administrationId}/not-given")
    @Operation(summary = "Markera administrering som ej given")
    public ResponseEntity<MedicationAdministrationDto> markNotGiven(
            @PathVariable UUID administrationId,
            @RequestParam String reason) {
        return ResponseEntity.ok(administrationService.markNotGiven(administrationId, reason));
    }

    @GetMapping("/overdue")
    @Operation(summary = "Hämta försenade administreringar")
    public ResponseEntity<List<MedicationAdministrationDto>> getOverdueAdministrations() {
        return ResponseEntity.ok(administrationService.getOverdueAdministrations());
    }

    @GetMapping("/prescription/{prescriptionId}/count")
    @Operation(summary = "Räkna utförda administreringar för ordination")
    public ResponseEntity<Long> countCompletedAdministrations(@PathVariable UUID prescriptionId) {
        return ResponseEntity.ok(administrationService.countCompletedAdministrations(prescriptionId));
    }
}
