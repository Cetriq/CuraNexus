package se.curanexus.coding.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import se.curanexus.coding.domain.*;
import se.curanexus.coding.repository.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CodingServiceTest {

    @Mock
    private CodeSystemRepository codeSystemRepository;

    @Mock
    private DiagnosisCodeRepository diagnosisCodeRepository;

    @Mock
    private ProcedureCodeRepository procedureCodeRepository;

    @Mock
    private MedicationCodeRepository medicationCodeRepository;

    private CodingService codingService;

    @BeforeEach
    void setUp() {
        codingService = new CodingService(
                codeSystemRepository,
                diagnosisCodeRepository,
                procedureCodeRepository,
                medicationCodeRepository
        );
    }

    // === Code System Tests ===

    @Test
    void shouldGetAllActiveCodeSystems() {
        var icd10 = new CodeSystem(CodeSystemType.ICD10_SE, "2024", LocalDate.of(2024, 1, 1));
        var kva = new CodeSystem(CodeSystemType.KVA, "2024", LocalDate.of(2024, 1, 1));

        when(codeSystemRepository.findByActiveTrue()).thenReturn(List.of(icd10, kva));

        var result = codingService.getAllCodeSystems();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(CodeSystem::getType)
                .containsExactlyInAnyOrder(CodeSystemType.ICD10_SE, CodeSystemType.KVA);
    }

    @Test
    void shouldGetCodeSystemByType() {
        var icd10 = new CodeSystem(CodeSystemType.ICD10_SE, "2024", LocalDate.of(2024, 1, 1));

        when(codeSystemRepository.findByTypeAndActiveTrue(CodeSystemType.ICD10_SE))
                .thenReturn(Optional.of(icd10));

        var result = codingService.getCodeSystem(CodeSystemType.ICD10_SE);

        assertThat(result.getType()).isEqualTo(CodeSystemType.ICD10_SE);
        assertThat(result.getVersion()).isEqualTo("2024");
    }

    // === Diagnosis Code Tests ===

    @Test
    void shouldGetDiagnosisCodeByCode() {
        var diabetes = new DiagnosisCode("E11.9", "Type 2 diabetes", "Diabetes mellitus typ 2");

        when(diagnosisCodeRepository.findByCodeAndActiveTrue("E11.9"))
                .thenReturn(Optional.of(diabetes));

        var result = codingService.getDiagnosisCode("E11.9");

        assertThat(result.getCode()).isEqualTo("E11.9");
        assertThat(result.getSwedishName()).isEqualTo("Diabetes mellitus typ 2");
    }

    @Test
    void shouldThrowExceptionForInvalidDiagnosisCode() {
        when(diagnosisCodeRepository.findByCodeAndActiveTrue("INVALID"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> codingService.getDiagnosisCode("INVALID"))
                .isInstanceOf(CodeNotFoundException.class)
                .hasMessageContaining("ICD-10");
    }

    @Test
    void shouldSearchDiagnosisCodes() {
        var diabetes = new DiagnosisCode("E11.9", "Type 2 diabetes", "Diabetes mellitus typ 2");
        Page<DiagnosisCode> page = new PageImpl<>(List.of(diabetes));

        when(diagnosisCodeRepository.search(eq("diabetes"), any(PageRequest.class)))
                .thenReturn(page);

        var result = codingService.searchDiagnosisCodes("diabetes", 0, 20);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCode()).isEqualTo("E11.9");
    }

    @Test
    void shouldValidateDiagnosisCode() {
        when(diagnosisCodeRepository.findByCodeAndActiveTrue("E11.9"))
                .thenReturn(Optional.of(new DiagnosisCode("E11.9", "Type 2 diabetes", "Diabetes mellitus typ 2")));
        when(diagnosisCodeRepository.findByCodeAndActiveTrue("INVALID"))
                .thenReturn(Optional.empty());

        assertThat(codingService.isValidDiagnosisCode("E11.9")).isTrue();
        assertThat(codingService.isValidDiagnosisCode("INVALID")).isFalse();
    }

    // === Procedure Code Tests ===

    @Test
    void shouldGetProcedureCodeByCode() {
        var procedure = new ProcedureCode("NFB09", "Primary total hip replacement", "Primär total höftledsplastik");

        when(procedureCodeRepository.findByCodeAndActiveTrue("NFB09"))
                .thenReturn(Optional.of(procedure));

        var result = codingService.getProcedureCode("NFB09");

        assertThat(result.getCode()).isEqualTo("NFB09");
        assertThat(result.getSwedishName()).isEqualTo("Primär total höftledsplastik");
    }

    @Test
    void shouldThrowExceptionForInvalidProcedureCode() {
        when(procedureCodeRepository.findByCodeAndActiveTrue("INVALID"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> codingService.getProcedureCode("INVALID"))
                .isInstanceOf(CodeNotFoundException.class)
                .hasMessageContaining("KVÅ");
    }

    @Test
    void shouldSearchProcedureCodes() {
        var procedure = new ProcedureCode("NFB09", "Primary total hip replacement", "Primär total höftledsplastik");
        Page<ProcedureCode> page = new PageImpl<>(List.of(procedure));

        when(procedureCodeRepository.search(eq("höft"), any(PageRequest.class)))
                .thenReturn(page);

        var result = codingService.searchProcedureCodes("höft", 0, 20);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCode()).isEqualTo("NFB09");
    }

    // === Medication Code Tests ===

    @Test
    void shouldGetMedicationCodeByCode() {
        var medication = new MedicationCode("N02BE01", "Paracetamol", "Paracetamol", 5);

        when(medicationCodeRepository.findByCodeAndActiveTrue("N02BE01"))
                .thenReturn(Optional.of(medication));

        var result = codingService.getMedicationCode("N02BE01");

        assertThat(result.getCode()).isEqualTo("N02BE01");
        assertThat(result.getSwedishName()).isEqualTo("Paracetamol");
    }

    @Test
    void shouldThrowExceptionForInvalidMedicationCode() {
        when(medicationCodeRepository.findByCodeAndActiveTrue("INVALID"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> codingService.getMedicationCode("INVALID"))
                .isInstanceOf(CodeNotFoundException.class)
                .hasMessageContaining("ATC");
    }

    @Test
    void shouldSearchMedicationCodes() {
        var medication = new MedicationCode("N02BE01", "Paracetamol", "Paracetamol", 5);
        Page<MedicationCode> page = new PageImpl<>(List.of(medication));

        when(medicationCodeRepository.search(eq("paracetamol"), any(PageRequest.class)))
                .thenReturn(page);

        var result = codingService.searchMedicationCodes("paracetamol", 0, 20);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCode()).isEqualTo("N02BE01");
    }

    // === Statistics Tests ===

    @Test
    void shouldGetStatistics() {
        when(diagnosisCodeRepository.countByActiveTrue()).thenReturn(100L);
        when(procedureCodeRepository.countByActiveTrue()).thenReturn(50L);
        when(medicationCodeRepository.countByActiveTrue()).thenReturn(75L);

        var stats = codingService.getStatistics();

        assertThat(stats.diagnosisCodes()).isEqualTo(100L);
        assertThat(stats.procedureCodes()).isEqualTo(50L);
        assertThat(stats.medicationCodes()).isEqualTo(75L);
        assertThat(stats.totalCodes()).isEqualTo(225L);
    }
}
