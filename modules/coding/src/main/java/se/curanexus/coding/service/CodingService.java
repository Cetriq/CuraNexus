package se.curanexus.coding.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.curanexus.coding.domain.*;
import se.curanexus.coding.repository.*;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CodingService {

    private final CodeSystemRepository codeSystemRepository;
    private final DiagnosisCodeRepository diagnosisCodeRepository;
    private final ProcedureCodeRepository procedureCodeRepository;
    private final MedicationCodeRepository medicationCodeRepository;

    public CodingService(
            CodeSystemRepository codeSystemRepository,
            DiagnosisCodeRepository diagnosisCodeRepository,
            ProcedureCodeRepository procedureCodeRepository,
            MedicationCodeRepository medicationCodeRepository) {
        this.codeSystemRepository = codeSystemRepository;
        this.diagnosisCodeRepository = diagnosisCodeRepository;
        this.procedureCodeRepository = procedureCodeRepository;
        this.medicationCodeRepository = medicationCodeRepository;
    }

    // === Code Systems ===

    public List<CodeSystem> getAllCodeSystems() {
        return codeSystemRepository.findByActiveTrue();
    }

    public CodeSystem getCodeSystem(CodeSystemType type) {
        return codeSystemRepository.findByTypeAndActiveTrue(type)
                .orElseThrow(() -> new CodeNotFoundException("CodeSystem", type.name()));
    }

    // === Diagnosis Codes (ICD-10) ===

    public DiagnosisCode getDiagnosisCode(String code) {
        return diagnosisCodeRepository.findByCodeAndActiveTrue(code)
                .orElseThrow(() -> new CodeNotFoundException("ICD-10", code));
    }

    public Page<DiagnosisCode> searchDiagnosisCodes(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return diagnosisCodeRepository.search(query, pageable);
    }

    public Page<DiagnosisCode> searchDiagnosisCodesLeafOnly(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return diagnosisCodeRepository.searchLeafCodes(query, pageable);
    }

    public List<DiagnosisCode> getDiagnosisCodesByChapter(String chapter) {
        return diagnosisCodeRepository.findByChapter(chapter);
    }

    public List<DiagnosisCode> getDiagnosisCodeChildren(String parentCode) {
        return diagnosisCodeRepository.findByParentCode(parentCode);
    }

    public List<String> getAllDiagnosisChapters() {
        return diagnosisCodeRepository.findAllChapters();
    }

    public boolean isValidDiagnosisCode(String code) {
        return diagnosisCodeRepository.findByCodeAndActiveTrue(code).isPresent();
    }

    // === Procedure Codes (KVÅ) ===

    public ProcedureCode getProcedureCode(String code) {
        return procedureCodeRepository.findByCodeAndActiveTrue(code)
                .orElseThrow(() -> new CodeNotFoundException("KVÅ", code));
    }

    public Page<ProcedureCode> searchProcedureCodes(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return procedureCodeRepository.search(query, pageable);
    }

    public Page<ProcedureCode> searchProcedureCodesLeafOnly(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return procedureCodeRepository.searchLeafCodes(query, pageable);
    }

    public List<ProcedureCode> getProcedureCodesByCategory(String category) {
        return procedureCodeRepository.findByCategory(category);
    }

    public List<ProcedureCode> getProcedureCodeChildren(String parentCode) {
        return procedureCodeRepository.findByParentCode(parentCode);
    }

    public List<String> getAllProcedureCategories() {
        return procedureCodeRepository.findAllCategories();
    }

    public boolean isValidProcedureCode(String code) {
        return procedureCodeRepository.findByCodeAndActiveTrue(code).isPresent();
    }

    // === Medication Codes (ATC) ===

    public MedicationCode getMedicationCode(String code) {
        return medicationCodeRepository.findByCodeAndActiveTrue(code)
                .orElseThrow(() -> new CodeNotFoundException("ATC", code));
    }

    public Page<MedicationCode> searchMedicationCodes(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return medicationCodeRepository.search(query, pageable);
    }

    public Page<MedicationCode> searchMedicationSubstances(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return medicationCodeRepository.searchChemicalSubstances(query, pageable);
    }

    public List<MedicationCode> getMedicationCodesByAnatomicalGroup(String group) {
        return medicationCodeRepository.findByAnatomicalGroup(group);
    }

    public List<MedicationCode> getMedicationCodeChildren(String parentCode) {
        return medicationCodeRepository.findByParentCode(parentCode);
    }

    public List<String> getAllAnatomicalGroups() {
        return medicationCodeRepository.findAllAnatomicalGroups();
    }

    public boolean isValidMedicationCode(String code) {
        return medicationCodeRepository.findByCodeAndActiveTrue(code).isPresent();
    }

    // === Statistics ===

    public CodingStatistics getStatistics() {
        return new CodingStatistics(
                diagnosisCodeRepository.countByActiveTrue(),
                procedureCodeRepository.countByActiveTrue(),
                medicationCodeRepository.countByActiveTrue()
        );
    }

    public record CodingStatistics(
            long diagnosisCodes,
            long procedureCodes,
            long medicationCodes
    ) {
        public long totalCodes() {
            return diagnosisCodes + procedureCodes + medicationCodes;
        }
    }
}
