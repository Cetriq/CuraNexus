package se.curanexus.medication.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.curanexus.medication.api.dto.MedicationDto;
import se.curanexus.medication.service.MedicationService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/medications")
@Tag(name = "Medications", description = "Läkemedelsregister")
public class MedicationController {

    private final MedicationService medicationService;

    public MedicationController(MedicationService medicationService) {
        this.medicationService = medicationService;
    }

    @GetMapping("/{medicationId}")
    @Operation(summary = "Hämta läkemedel via ID")
    public ResponseEntity<MedicationDto> getMedication(@PathVariable UUID medicationId) {
        return ResponseEntity.ok(medicationService.getMedication(medicationId));
    }

    @GetMapping("/npl/{nplId}")
    @Operation(summary = "Hämta läkemedel via NPL-ID")
    public ResponseEntity<MedicationDto> getMedicationByNplId(@PathVariable String nplId) {
        return ResponseEntity.ok(medicationService.getMedicationByNplId(nplId));
    }

    @GetMapping("/search")
    @Operation(summary = "Sök läkemedel")
    public ResponseEntity<Page<MedicationDto>> searchMedications(
            @RequestParam String query,
            Pageable pageable) {
        return ResponseEntity.ok(medicationService.searchMedications(query, pageable));
    }

    @GetMapping("/search/quick")
    @Operation(summary = "Snabbsökning för autocomplete")
    public ResponseEntity<List<MedicationDto>> quickSearch(@RequestParam String query) {
        return ResponseEntity.ok(medicationService.quickSearchByName(query));
    }

    @GetMapping("/atc/{atcCode}")
    @Operation(summary = "Hämta läkemedel via ATC-kod")
    public ResponseEntity<List<MedicationDto>> getMedicationsByAtcCode(@PathVariable String atcCode) {
        return ResponseEntity.ok(medicationService.getMedicationsByAtcCode(atcCode));
    }

    @GetMapping("/atc-prefix/{atcPrefix}")
    @Operation(summary = "Hämta läkemedel via ATC-kodprefix")
    public ResponseEntity<List<MedicationDto>> getMedicationsByAtcPrefix(@PathVariable String atcPrefix) {
        return ResponseEntity.ok(medicationService.getMedicationsByAtcPrefix(atcPrefix));
    }

    @GetMapping("/narcotic")
    @Operation(summary = "Hämta narkotikaklassade läkemedel")
    public ResponseEntity<List<MedicationDto>> getNarcoticMedications() {
        return ResponseEntity.ok(medicationService.getNarcoticMedications());
    }
}
