package se.curanexus.coding.api;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.curanexus.coding.api.dto.*;
import se.curanexus.coding.domain.CodeSystemType;
import se.curanexus.coding.service.CodingService;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/coding")
public class CodingController {

    private final CodingService codingService;

    public CodingController(CodingService codingService) {
        this.codingService = codingService;
    }

    // === Code Systems ===

    @GetMapping("/systems")
    public ResponseEntity<List<CodeSystemDto>> getCodeSystems() {
        var systems = codingService.getAllCodeSystems().stream()
                .map(CodeSystemDto::from)
                .toList();
        return ResponseEntity.ok(systems);
    }

    @GetMapping("/statistics")
    public ResponseEntity<CodingStatisticsDto> getStatistics() {
        var stats = codingService.getStatistics();
        return ResponseEntity.ok(CodingStatisticsDto.from(stats));
    }

    // === Diagnosis Codes (ICD-10) ===

    @GetMapping("/diagnoses/{code}")
    public ResponseEntity<DiagnosisCodeDto> getDiagnosisCode(@PathVariable String code) {
        var diagnosis = codingService.getDiagnosisCode(code);
        return ResponseEntity.ok(DiagnosisCodeDto.from(diagnosis));
    }

    @GetMapping("/diagnoses/search")
    public ResponseEntity<Page<DiagnosisCodeDto>> searchDiagnosisCodes(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "false") boolean leafOnly) {

        Page<DiagnosisCodeDto> results;
        if (leafOnly) {
            results = codingService.searchDiagnosisCodesLeafOnly(query, page, size)
                    .map(DiagnosisCodeDto::from);
        } else {
            results = codingService.searchDiagnosisCodes(query, page, size)
                    .map(DiagnosisCodeDto::from);
        }
        return ResponseEntity.ok(results);
    }

    @GetMapping("/diagnoses/chapters")
    public ResponseEntity<List<String>> getDiagnosisChapters() {
        return ResponseEntity.ok(codingService.getAllDiagnosisChapters());
    }

    @GetMapping("/diagnoses/chapters/{chapter}")
    public ResponseEntity<List<DiagnosisCodeDto>> getDiagnosisCodesByChapter(@PathVariable String chapter) {
        var codes = codingService.getDiagnosisCodesByChapter(chapter).stream()
                .map(DiagnosisCodeDto::from)
                .toList();
        return ResponseEntity.ok(codes);
    }

    @GetMapping("/diagnoses/{code}/children")
    public ResponseEntity<List<DiagnosisCodeDto>> getDiagnosisCodeChildren(@PathVariable String code) {
        var children = codingService.getDiagnosisCodeChildren(code).stream()
                .map(DiagnosisCodeDto::from)
                .toList();
        return ResponseEntity.ok(children);
    }

    // === Procedure Codes (KVÅ) ===

    @GetMapping("/procedures/{code}")
    public ResponseEntity<ProcedureCodeDto> getProcedureCode(@PathVariable String code) {
        var procedure = codingService.getProcedureCode(code);
        return ResponseEntity.ok(ProcedureCodeDto.from(procedure));
    }

    @GetMapping("/procedures/search")
    public ResponseEntity<Page<ProcedureCodeDto>> searchProcedureCodes(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "false") boolean leafOnly) {

        Page<ProcedureCodeDto> results;
        if (leafOnly) {
            results = codingService.searchProcedureCodesLeafOnly(query, page, size)
                    .map(ProcedureCodeDto::from);
        } else {
            results = codingService.searchProcedureCodes(query, page, size)
                    .map(ProcedureCodeDto::from);
        }
        return ResponseEntity.ok(results);
    }

    @GetMapping("/procedures/categories")
    public ResponseEntity<List<String>> getProcedureCategories() {
        return ResponseEntity.ok(codingService.getAllProcedureCategories());
    }

    @GetMapping("/procedures/categories/{category}")
    public ResponseEntity<List<ProcedureCodeDto>> getProcedureCodesByCategory(@PathVariable String category) {
        var codes = codingService.getProcedureCodesByCategory(category).stream()
                .map(ProcedureCodeDto::from)
                .toList();
        return ResponseEntity.ok(codes);
    }

    @GetMapping("/procedures/{code}/children")
    public ResponseEntity<List<ProcedureCodeDto>> getProcedureCodeChildren(@PathVariable String code) {
        var children = codingService.getProcedureCodeChildren(code).stream()
                .map(ProcedureCodeDto::from)
                .toList();
        return ResponseEntity.ok(children);
    }

    // === Medication Codes (ATC) ===

    @GetMapping("/medications/{code}")
    public ResponseEntity<MedicationCodeDto> getMedicationCode(@PathVariable String code) {
        var medication = codingService.getMedicationCode(code);
        return ResponseEntity.ok(MedicationCodeDto.from(medication));
    }

    @GetMapping("/medications/search")
    public ResponseEntity<Page<MedicationCodeDto>> searchMedicationCodes(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "false") boolean substancesOnly) {

        Page<MedicationCodeDto> results;
        if (substancesOnly) {
            results = codingService.searchMedicationSubstances(query, page, size)
                    .map(MedicationCodeDto::from);
        } else {
            results = codingService.searchMedicationCodes(query, page, size)
                    .map(MedicationCodeDto::from);
        }
        return ResponseEntity.ok(results);
    }

    @GetMapping("/medications/groups")
    public ResponseEntity<List<String>> getAnatomicalGroups() {
        return ResponseEntity.ok(codingService.getAllAnatomicalGroups());
    }

    @GetMapping("/medications/groups/{group}")
    public ResponseEntity<List<MedicationCodeDto>> getMedicationCodesByGroup(@PathVariable String group) {
        var codes = codingService.getMedicationCodesByAnatomicalGroup(group).stream()
                .map(MedicationCodeDto::from)
                .toList();
        return ResponseEntity.ok(codes);
    }

    @GetMapping("/medications/{code}/children")
    public ResponseEntity<List<MedicationCodeDto>> getMedicationCodeChildren(@PathVariable String code) {
        var children = codingService.getMedicationCodeChildren(code).stream()
                .map(MedicationCodeDto::from)
                .toList();
        return ResponseEntity.ok(children);
    }

    // === Validation ===

    @PostMapping("/validate")
    public ResponseEntity<ValidationResultDto> validateCodes(@Valid @RequestBody ValidateCodesRequest request) {
        List<ValidationResultDto.CodeValidation> validations = new ArrayList<>();
        boolean allValid = true;

        for (var codeToValidate : request.codes()) {
            boolean valid = false;
            String displayName = null;
            String errorMessage = null;

            try {
                CodeSystemType codeSystem = CodeSystemType.valueOf(codeToValidate.codeSystem());

                switch (codeSystem) {
                    case ICD10_SE -> {
                        var diagnosis = codingService.getDiagnosisCode(codeToValidate.code());
                        valid = true;
                        displayName = diagnosis.getSwedishName();
                    }
                    case KVA -> {
                        var procedure = codingService.getProcedureCode(codeToValidate.code());
                        valid = true;
                        displayName = procedure.getSwedishName();
                    }
                    case ATC -> {
                        var medication = codingService.getMedicationCode(codeToValidate.code());
                        valid = true;
                        displayName = medication.getSwedishName();
                    }
                    default -> errorMessage = "Unsupported code system: " + codeToValidate.codeSystem();
                }
            } catch (IllegalArgumentException e) {
                errorMessage = "Unknown code system: " + codeToValidate.codeSystem();
            } catch (Exception e) {
                errorMessage = "Code not found: " + codeToValidate.code();
            }

            if (!valid) {
                allValid = false;
            }

            validations.add(new ValidationResultDto.CodeValidation(
                    codeToValidate.code(),
                    codeToValidate.codeSystem(),
                    valid,
                    displayName,
                    errorMessage
            ));
        }

        return ResponseEntity.ok(new ValidationResultDto(allValid, validations));
    }
}
