package se.curanexus.medication.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.curanexus.medication.api.dto.CreateDrugAllergyRequest;
import se.curanexus.medication.api.dto.DrugAllergyDto;
import se.curanexus.medication.service.DrugAllergyService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/drug-allergies")
@Tag(name = "Drug Allergies", description = "Läkemedelsallergier")
public class DrugAllergyController {

    private final DrugAllergyService allergyService;

    public DrugAllergyController(DrugAllergyService allergyService) {
        this.allergyService = allergyService;
    }

    @PostMapping
    @Operation(summary = "Registrera läkemedelsallergi")
    public ResponseEntity<DrugAllergyDto> createAllergy(
            @Valid @RequestBody CreateDrugAllergyRequest request,
            @RequestHeader("X-User-Id") UUID recordedById) {
        DrugAllergyDto allergy = allergyService.createAllergy(request, recordedById);
        return ResponseEntity.status(HttpStatus.CREATED).body(allergy);
    }

    @GetMapping("/{allergyId}")
    @Operation(summary = "Hämta allergi via ID")
    public ResponseEntity<DrugAllergyDto> getAllergy(@PathVariable UUID allergyId) {
        return ResponseEntity.ok(allergyService.getAllergy(allergyId));
    }

    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Hämta patients aktiva allergier")
    public ResponseEntity<List<DrugAllergyDto>> getPatientAllergies(@PathVariable UUID patientId) {
        return ResponseEntity.ok(allergyService.getPatientAllergies(patientId));
    }

    @GetMapping("/patient/{patientId}/all")
    @Operation(summary = "Hämta alla allergier för patient (inkl. inaktiva)")
    public ResponseEntity<List<DrugAllergyDto>> getAllPatientAllergies(@PathVariable UUID patientId) {
        return ResponseEntity.ok(allergyService.getAllPatientAllergies(patientId));
    }

    @GetMapping("/patient/{patientId}/check")
    @Operation(summary = "Kontrollera allergi mot läkemedel")
    public ResponseEntity<List<DrugAllergyDto>> checkAllergyForMedication(
            @PathVariable UUID patientId,
            @RequestParam String atcCode) {
        return ResponseEntity.ok(allergyService.checkAllergyForAtcCode(patientId, atcCode));
    }

    @PostMapping("/{allergyId}/verify")
    @Operation(summary = "Verifiera allergi")
    public ResponseEntity<DrugAllergyDto> verifyAllergy(
            @PathVariable UUID allergyId,
            @RequestHeader("X-User-Id") UUID verifiedById) {
        return ResponseEntity.ok(allergyService.verifyAllergy(allergyId, verifiedById));
    }

    @PostMapping("/{allergyId}/deactivate")
    @Operation(summary = "Inaktivera allergi")
    public ResponseEntity<DrugAllergyDto> deactivateAllergy(
            @PathVariable UUID allergyId,
            @RequestParam String reason) {
        return ResponseEntity.ok(allergyService.deactivateAllergy(allergyId, reason));
    }

    @GetMapping("/patient/{patientId}/count")
    @Operation(summary = "Räkna aktiva allergier för patient")
    public ResponseEntity<Long> countActiveAllergies(@PathVariable UUID patientId) {
        return ResponseEntity.ok(allergyService.countActiveAllergies(patientId));
    }
}
