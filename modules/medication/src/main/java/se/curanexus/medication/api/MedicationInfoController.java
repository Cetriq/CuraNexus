package se.curanexus.medication.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.curanexus.medication.api.dto.MedicationInfoDto;
import se.curanexus.medication.api.dto.ProductDocumentDto;
import se.curanexus.medication.service.MedicationInfoService;

import java.util.Map;

/**
 * REST controller for medication information from Fass.
 *
 * Provides access to:
 * - Product information (basic data, clinical info)
 * - SMPC sections (for healthcare professionals)
 * - Patient information leaflets
 *
 * See REQ-MED-021: Fass as information source in UI.
 */
@RestController
@RequestMapping("/api/v1/medications/info")
@Tag(name = "Medication Info", description = "Läkemedelsinformation från Fass")
public class MedicationInfoController {

    private final MedicationInfoService infoService;

    public MedicationInfoController(MedicationInfoService infoService) {
        this.infoService = infoService;
    }

    @GetMapping("/npl/{nplId}")
    @Operation(summary = "Hämta samlad läkemedelsinformation",
               description = "Kombinerar lokal data med information från Fass (REQ-MED-033)")
    public ResponseEntity<MedicationInfoDto> getMedicationInfo(
            @Parameter(description = "NPL-ID för läkemedlet")
            @PathVariable String nplId) {

        return infoService.getMedicationInfo(nplId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/npl/{nplId}/indications")
    @Operation(summary = "Hämta indikationer (terapeutiska användningsområden)")
    public ResponseEntity<Map<String, String>> getIndications(@PathVariable String nplId) {
        return infoService.getIndications(nplId)
                .map(text -> ResponseEntity.ok(Map.of("indications", text)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/npl/{nplId}/dosage")
    @Operation(summary = "Hämta dosering och administrering")
    public ResponseEntity<Map<String, String>> getDosage(@PathVariable String nplId) {
        return infoService.getDosageGuidelines(nplId)
                .map(text -> ResponseEntity.ok(Map.of("dosage", text)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/npl/{nplId}/contraindications")
    @Operation(summary = "Hämta kontraindikationer")
    public ResponseEntity<Map<String, String>> getContraindications(@PathVariable String nplId) {
        return infoService.getContraindications(nplId)
                .map(text -> ResponseEntity.ok(Map.of("contraindications", text)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/npl/{nplId}/warnings")
    @Operation(summary = "Hämta varningar och försiktighet")
    public ResponseEntity<Map<String, String>> getWarnings(@PathVariable String nplId) {
        return infoService.getWarnings(nplId)
                .map(text -> ResponseEntity.ok(Map.of("warnings", text)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/npl/{nplId}/interactions")
    @Operation(summary = "Hämta interaktioner")
    public ResponseEntity<Map<String, String>> getInteractions(@PathVariable String nplId) {
        return infoService.getInteractions(nplId)
                .map(text -> ResponseEntity.ok(Map.of("interactions", text)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/npl/{nplId}/pregnancy")
    @Operation(summary = "Hämta graviditet/amning-information")
    public ResponseEntity<Map<String, String>> getPregnancyInfo(@PathVariable String nplId) {
        return infoService.getPregnancyInfo(nplId)
                .map(text -> ResponseEntity.ok(Map.of("pregnancyInfo", text)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/npl/{nplId}/adverse-reactions")
    @Operation(summary = "Hämta biverkningar")
    public ResponseEntity<Map<String, String>> getAdverseReactions(@PathVariable String nplId) {
        return infoService.getAdverseReactions(nplId)
                .map(text -> ResponseEntity.ok(Map.of("adverseReactions", text)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/npl/{nplId}/smpc")
    @Operation(summary = "Hämta full produktresumé (SMPC)",
               description = "Summary of Product Characteristics - för vårdpersonal")
    public ResponseEntity<ProductDocumentDto> getSmpc(@PathVariable String nplId) {
        return infoService.getSmpc(nplId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/npl/{nplId}/pil")
    @Operation(summary = "Hämta bipacksedel (PIL)",
               description = "Patient Information Leaflet - för patienter")
    public ResponseEntity<ProductDocumentDto> getPatientInformation(@PathVariable String nplId) {
        return infoService.getPatientInformation(nplId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/npl/{nplId}/fass-link")
    @Operation(summary = "Hämta direktlänk till Fass",
               description = "Länk för att öppna produkten direkt på fass.se")
    public ResponseEntity<Map<String, String>> getFassLink(@PathVariable String nplId) {
        return ResponseEntity.ok(Map.of(
                "fassLink", infoService.getFassLink(nplId),
                "patientLink", infoService.getFassPatientLink(nplId)
        ));
    }

    @GetMapping("/status")
    @Operation(summary = "Kontrollera status för Fass-integration")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(Map.of(
                "fassEnabled", infoService.isFassAvailable(),
                "source", "FASS API",
                "documentation", "https://api.fass.se/documentation"
        ));
    }
}
