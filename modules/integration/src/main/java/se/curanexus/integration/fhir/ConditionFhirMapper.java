package se.curanexus.integration.fhir;

import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

import static se.curanexus.integration.fhir.SwedishFhirExtensions.*;

/**
 * Maps Condition/Diagnosis data to FHIR R4 Condition resources.
 * Supports ICD-10-SE coding for Swedish healthcare.
 */
@Component
public class ConditionFhirMapper {

    /**
     * Convert diagnosis data to FHIR Condition.
     */
    public Condition toFhir(ConditionData condition) {
        Condition fhirCondition = new Condition();

        // ID
        fhirCondition.setId(condition.id().toString());

        // Clinical status
        fhirCondition.setClinicalStatus(createClinicalStatus(condition.clinicalStatus()));

        // Verification status
        fhirCondition.setVerificationStatus(createVerificationStatus(condition.verificationStatus()));

        // Category
        fhirCondition.addCategory(createCategory(condition.category()));

        // Severity
        if (condition.severity() != null) {
            fhirCondition.setSeverity(createSeverity(condition.severity()));
        }

        // Code (ICD-10-SE or SNOMED CT)
        fhirCondition.setCode(createDiagnosisCode(condition));

        // Body site
        if (condition.bodySite() != null) {
            fhirCondition.addBodySite(new CodeableConcept().setText(condition.bodySite()));
        }

        // Subject
        fhirCondition.setSubject(new Reference("Patient/" + condition.patientId()));

        // Encounter
        if (condition.encounterId() != null) {
            fhirCondition.setEncounter(new Reference("Encounter/" + condition.encounterId()));
        }

        // Onset
        if (condition.onsetDate() != null) {
            fhirCondition.setOnset(new DateTimeType(toDate(condition.onsetDate())));
        } else if (condition.onsetString() != null) {
            fhirCondition.setOnset(new StringType(condition.onsetString()));
        }

        // Abatement (when condition resolved)
        if (condition.abatementDate() != null) {
            fhirCondition.setAbatement(new DateTimeType(toDate(condition.abatementDate())));
        }

        // Recorded date
        if (condition.recordedDate() != null) {
            fhirCondition.setRecordedDate(Date.from(condition.recordedDate()));
        }

        // Recorder (who recorded the diagnosis)
        if (condition.recorderId() != null) {
            fhirCondition.setRecorder(new Reference("Practitioner/" + condition.recorderId()));
        }

        // Asserter (who asserted the diagnosis)
        if (condition.asserterId() != null) {
            fhirCondition.setAsserter(new Reference("Practitioner/" + condition.asserterId()));
        }

        // Evidence (supporting observations, etc.)
        if (condition.evidenceCode() != null) {
            Condition.ConditionEvidenceComponent evidence = fhirCondition.addEvidence();
            evidence.addCode(new CodeableConcept().setText(condition.evidenceCode()));
        }

        // Note
        if (condition.note() != null) {
            fhirCondition.addNote(new Annotation().setText(condition.note()));
        }

        return fhirCondition;
    }

    /**
     * Create a primary diagnosis for an encounter.
     */
    public Condition createPrimaryDiagnosis(UUID id, UUID patientId, UUID encounterId,
                                             String icd10Code, String displayName,
                                             UUID asserterId, Instant recordedDate) {
        ConditionData data = new ConditionData(
                id, patientId, encounterId,
                "active", "confirmed", "encounter-diagnosis",
                null, // severity
                "ICD10", icd10Code, displayName,
                null, null, null, null, // onset
                recordedDate, asserterId, asserterId,
                null, null, null, true
        );
        return toFhir(data);
    }

    /**
     * Create a secondary diagnosis for an encounter.
     */
    public Condition createSecondaryDiagnosis(UUID id, UUID patientId, UUID encounterId,
                                               String icd10Code, String displayName,
                                               UUID asserterId, Instant recordedDate) {
        ConditionData data = new ConditionData(
                id, patientId, encounterId,
                "active", "confirmed", "encounter-diagnosis",
                null,
                "ICD10", icd10Code, displayName,
                null, null, null, null,
                recordedDate, asserterId, asserterId,
                null, null, null, false
        );
        return toFhir(data);
    }

    private CodeableConcept createClinicalStatus(String status) {
        CodeableConcept cc = new CodeableConcept();
        Coding coding = new Coding()
                .setSystem("http://terminology.hl7.org/CodeSystem/condition-clinical");

        return switch (status.toLowerCase()) {
            case "active" -> cc.addCoding(coding.setCode("active").setDisplay("Active"));
            case "recurrence" -> cc.addCoding(coding.setCode("recurrence").setDisplay("Recurrence"));
            case "relapse" -> cc.addCoding(coding.setCode("relapse").setDisplay("Relapse"));
            case "inactive" -> cc.addCoding(coding.setCode("inactive").setDisplay("Inactive"));
            case "remission" -> cc.addCoding(coding.setCode("remission").setDisplay("Remission"));
            case "resolved" -> cc.addCoding(coding.setCode("resolved").setDisplay("Resolved"));
            default -> cc.addCoding(coding.setCode("active").setDisplay("Active"));
        };
    }

    private CodeableConcept createVerificationStatus(String status) {
        CodeableConcept cc = new CodeableConcept();
        Coding coding = new Coding()
                .setSystem("http://terminology.hl7.org/CodeSystem/condition-ver-status");

        return switch (status.toLowerCase()) {
            case "unconfirmed" -> cc.addCoding(coding.setCode("unconfirmed").setDisplay("Unconfirmed"));
            case "provisional" -> cc.addCoding(coding.setCode("provisional").setDisplay("Provisional"));
            case "differential" -> cc.addCoding(coding.setCode("differential").setDisplay("Differential"));
            case "confirmed" -> cc.addCoding(coding.setCode("confirmed").setDisplay("Confirmed"));
            case "refuted" -> cc.addCoding(coding.setCode("refuted").setDisplay("Refuted"));
            case "entered-in-error" -> cc.addCoding(coding.setCode("entered-in-error").setDisplay("Entered in Error"));
            default -> cc.addCoding(coding.setCode("unconfirmed").setDisplay("Unconfirmed"));
        };
    }

    private CodeableConcept createCategory(String category) {
        CodeableConcept cc = new CodeableConcept();
        Coding coding = new Coding()
                .setSystem("http://terminology.hl7.org/CodeSystem/condition-category");

        return switch (category.toLowerCase()) {
            case "problem-list-item", "problem" -> cc.addCoding(coding.setCode("problem-list-item").setDisplay("Problem List Item"));
            case "encounter-diagnosis", "diagnosis" -> cc.addCoding(coding.setCode("encounter-diagnosis").setDisplay("Encounter Diagnosis"));
            default -> cc.addCoding(coding.setCode("encounter-diagnosis").setDisplay("Encounter Diagnosis"));
        };
    }

    private CodeableConcept createSeverity(String severity) {
        CodeableConcept cc = new CodeableConcept();
        Coding coding = new Coding()
                .setSystem("http://snomed.info/sct");

        return switch (severity.toLowerCase()) {
            case "mild" -> cc.addCoding(coding.setCode("255604002").setDisplay("Mild"));
            case "moderate" -> cc.addCoding(coding.setCode("6736007").setDisplay("Moderate"));
            case "severe" -> cc.addCoding(coding.setCode("24484000").setDisplay("Severe"));
            default -> cc.setText(severity);
        };
    }

    private CodeableConcept createDiagnosisCode(ConditionData condition) {
        CodeableConcept cc = new CodeableConcept();

        if ("ICD10".equalsIgnoreCase(condition.codeSystem()) || condition.code().matches("[A-Z]\\d{2}.*")) {
            // ICD-10-SE coding
            cc.addCoding(new Coding()
                    .setSystem(ICD10_SE_SYSTEM)
                    .setCode(condition.code())
                    .setDisplay(condition.displayName()));
        } else if ("SNOMED".equalsIgnoreCase(condition.codeSystem())) {
            // SNOMED CT coding
            cc.addCoding(new Coding()
                    .setSystem(SNOMED_CT_SWEDEN)
                    .setCode(condition.code())
                    .setDisplay(condition.displayName()));
        } else {
            // Unknown system
            cc.setText(condition.displayName());
            if (condition.code() != null) {
                cc.addCoding(new Coding()
                        .setCode(condition.code())
                        .setDisplay(condition.displayName()));
            }
        }

        // Add primary diagnosis extension if applicable
        if (condition.isPrimary()) {
            // Mark as primary diagnosis using FHIR R4 extension
            cc.addCoding(new Coding()
                    .setSystem("http://terminology.hl7.org/CodeSystem/diagnosis-role")
                    .setCode("AD")
                    .setDisplay("Admission diagnosis"));
        }

        return cc;
    }

    private Date toDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Condition/Diagnosis data record.
     */
    public record ConditionData(
            UUID id,
            UUID patientId,
            UUID encounterId,
            String clinicalStatus,
            String verificationStatus,
            String category,
            String severity,
            String codeSystem,
            String code,
            String displayName,
            LocalDate onsetDate,
            String onsetString,
            LocalDate abatementDate,
            String bodySite,
            Instant recordedDate,
            UUID recorderId,
            UUID asserterId,
            String evidenceCode,
            String note,
            String laterality,
            boolean isPrimary
    ) {}
}
