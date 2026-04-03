package se.curanexus.medication.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.curanexus.medication.api.dto.CreatePrescriptionRequest;
import se.curanexus.medication.api.dto.InteractionCheckResult;
import se.curanexus.medication.api.dto.PrescriptionDto;
import se.curanexus.medication.domain.PrescriptionStatus;
import se.curanexus.medication.service.InteractionCheckService;
import se.curanexus.medication.service.PrescriptionService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/prescriptions")
@Tag(name = "Prescriptions", description = "Ordinationshantering")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final InteractionCheckService interactionCheckService;

    public PrescriptionController(PrescriptionService prescriptionService,
                                   InteractionCheckService interactionCheckService) {
        this.prescriptionService = prescriptionService;
        this.interactionCheckService = interactionCheckService;
    }

    @PostMapping
    @Operation(summary = "Skapa ny ordination")
    public ResponseEntity<PrescriptionDto> createPrescription(
            @Valid @RequestBody CreatePrescriptionRequest request,
            @RequestHeader("X-User-Id") UUID prescriberId) {
        PrescriptionDto prescription = prescriptionService.createPrescription(request, prescriberId);
        return ResponseEntity.status(HttpStatus.CREATED).body(prescription);
    }

    @GetMapping("/{prescriptionId}")
    @Operation(summary = "Hämta ordination via ID")
    public ResponseEntity<PrescriptionDto> getPrescription(@PathVariable UUID prescriptionId) {
        return ResponseEntity.ok(prescriptionService.getPrescription(prescriptionId));
    }

    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Hämta patients alla ordinationer")
    public ResponseEntity<List<PrescriptionDto>> getPatientPrescriptions(@PathVariable UUID patientId) {
        return ResponseEntity.ok(prescriptionService.getPatientPrescriptions(patientId));
    }

    @GetMapping("/patient/{patientId}/active")
    @Operation(summary = "Hämta patients aktiva ordinationer")
    public ResponseEntity<List<PrescriptionDto>> getActivePrescriptions(@PathVariable UUID patientId) {
        return ResponseEntity.ok(prescriptionService.getActivePrescriptions(patientId));
    }

    @GetMapping("/encounter/{encounterId}")
    @Operation(summary = "Hämta ordinationer för vårdkontakt")
    public ResponseEntity<List<PrescriptionDto>> getEncounterPrescriptions(@PathVariable UUID encounterId) {
        return ResponseEntity.ok(prescriptionService.getEncounterPrescriptions(encounterId));
    }

    @PostMapping("/{prescriptionId}/activate")
    @Operation(summary = "Aktivera ordination")
    public ResponseEntity<PrescriptionDto> activatePrescription(@PathVariable UUID prescriptionId) {
        return ResponseEntity.ok(prescriptionService.activatePrescription(prescriptionId));
    }

    @PostMapping("/{prescriptionId}/hold")
    @Operation(summary = "Pausa ordination")
    public ResponseEntity<PrescriptionDto> putOnHold(
            @PathVariable UUID prescriptionId,
            @RequestParam String reason) {
        return ResponseEntity.ok(prescriptionService.putOnHold(prescriptionId, reason));
    }

    @PostMapping("/{prescriptionId}/complete")
    @Operation(summary = "Avsluta ordination")
    public ResponseEntity<PrescriptionDto> completePrescription(@PathVariable UUID prescriptionId) {
        return ResponseEntity.ok(prescriptionService.completePrescription(prescriptionId));
    }

    @PostMapping("/{prescriptionId}/cancel")
    @Operation(summary = "Avbryt ordination")
    public ResponseEntity<PrescriptionDto> cancelPrescription(
            @PathVariable UUID prescriptionId,
            @RequestParam String reason) {
        return ResponseEntity.ok(prescriptionService.cancelPrescription(prescriptionId, reason));
    }

    @GetMapping("/search")
    @Operation(summary = "Sök ordinationer")
    public ResponseEntity<Page<PrescriptionDto>> searchPrescriptions(
            @RequestParam(required = false) UUID patientId,
            @RequestParam(required = false) UUID prescriberId,
            @RequestParam(required = false) PrescriptionStatus status,
            @RequestParam(required = false) String atcCode,
            Pageable pageable) {
        return ResponseEntity.ok(prescriptionService.searchPrescriptions(
                patientId, prescriberId, status, atcCode, pageable));
    }

    @GetMapping("/patient/{patientId}/count")
    @Operation(summary = "Räkna aktiva ordinationer för patient")
    public ResponseEntity<Long> countActivePrescriptions(@PathVariable UUID patientId) {
        return ResponseEntity.ok(prescriptionService.countActivePrescriptions(patientId));
    }

    @GetMapping("/patient/{patientId}/interactions")
    @Operation(summary = "Kontrollera interaktioner för patients läkemedelslista")
    public ResponseEntity<InteractionCheckResult> checkPatientInteractions(@PathVariable UUID patientId) {
        return ResponseEntity.ok(interactionCheckService.checkAllPatientInteractions(patientId));
    }

    @GetMapping("/patient/{patientId}/interactions/check")
    @Operation(summary = "Kontrollera interaktioner för ny ordination")
    public ResponseEntity<InteractionCheckResult> checkNewPrescriptionInteractions(
            @PathVariable UUID patientId,
            @RequestParam String atcCode) {
        return ResponseEntity.ok(interactionCheckService.checkInteractionsForNewPrescription(patientId, atcCode));
    }
}
