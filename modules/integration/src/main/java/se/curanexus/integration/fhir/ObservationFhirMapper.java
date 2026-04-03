package se.curanexus.integration.fhir;

import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static se.curanexus.integration.fhir.SwedishFhirExtensions.*;

/**
 * Maps Observation data to FHIR R4 Observation resources.
 * Supports vital signs and other clinical observations.
 */
@Component
public class ObservationFhirMapper {

    // LOINC codes for common vital signs
    private static final String LOINC_SYSTEM = "http://loinc.org";
    private static final String LOINC_HEART_RATE = "8867-4";
    private static final String LOINC_RESPIRATORY_RATE = "9279-1";
    private static final String LOINC_SYSTOLIC_BP = "8480-6";
    private static final String LOINC_DIASTOLIC_BP = "8462-4";
    private static final String LOINC_BLOOD_PRESSURE = "85354-9";
    private static final String LOINC_BODY_TEMPERATURE = "8310-5";
    private static final String LOINC_OXYGEN_SATURATION = "2708-6";
    private static final String LOINC_BODY_WEIGHT = "29463-7";
    private static final String LOINC_BODY_HEIGHT = "8302-2";
    private static final String LOINC_BMI = "39156-5";
    private static final String LOINC_PAIN_LEVEL = "38208-5";

    /**
     * Convert internal observation to FHIR Observation.
     */
    public Observation toFhir(ObservationData observation) {
        Observation fhirObs = new Observation();

        // ID
        fhirObs.setId(observation.id().toString());

        // Status
        fhirObs.setStatus(mapStatus(observation.status()));

        // Category
        fhirObs.addCategory(createCategory(observation.category()));

        // Code
        fhirObs.setCode(createCode(observation.type(), observation.code(), observation.display()));

        // Subject
        fhirObs.setSubject(new Reference("Patient/" + observation.patientId()));

        // Encounter
        if (observation.encounterId() != null) {
            fhirObs.setEncounter(new Reference("Encounter/" + observation.encounterId()));
        }

        // Effective time
        if (observation.effectiveTime() != null) {
            fhirObs.setEffective(new DateTimeType(Date.from(observation.effectiveTime())));
        }

        // Performer (who made the observation)
        if (observation.performerId() != null) {
            fhirObs.addPerformer(new Reference("Practitioner/" + observation.performerId()));
        }

        // Value
        addValue(fhirObs, observation);

        // Interpretation
        if (observation.interpretation() != null) {
            fhirObs.addInterpretation(createInterpretation(observation.interpretation()));
        }

        // Reference range
        if (observation.referenceRangeLow() != null || observation.referenceRangeHigh() != null) {
            addReferenceRange(fhirObs, observation);
        }

        // Notes
        if (observation.note() != null) {
            fhirObs.addNote(new Annotation().setText(observation.note()));
        }

        // Body site
        if (observation.bodySite() != null) {
            fhirObs.setBodySite(new CodeableConcept().setText(observation.bodySite()));
        }

        // Method
        if (observation.method() != null) {
            fhirObs.setMethod(new CodeableConcept().setText(observation.method()));
        }

        // Device
        if (observation.deviceId() != null) {
            fhirObs.setDevice(new Reference("Device/" + observation.deviceId()));
        }

        return fhirObs;
    }

    /**
     * Create a vital sign observation with standard LOINC coding.
     */
    public Observation createVitalSign(VitalSignData vitalSign) {
        Observation fhirObs = new Observation();

        fhirObs.setId(vitalSign.id().toString());
        fhirObs.setStatus(Observation.ObservationStatus.FINAL);

        // Vital signs category
        fhirObs.addCategory(new CodeableConcept()
                .addCoding(new Coding()
                        .setSystem("http://terminology.hl7.org/CodeSystem/observation-category")
                        .setCode("vital-signs")
                        .setDisplay("Vital Signs")));

        // Add LOINC code based on vital sign type
        CodeableConcept code = createVitalSignCode(vitalSign.type());
        fhirObs.setCode(code);

        // Subject and encounter
        fhirObs.setSubject(new Reference("Patient/" + vitalSign.patientId()));
        if (vitalSign.encounterId() != null) {
            fhirObs.setEncounter(new Reference("Encounter/" + vitalSign.encounterId()));
        }

        // Effective time
        fhirObs.setEffective(new DateTimeType(Date.from(vitalSign.effectiveTime())));

        // Performer
        if (vitalSign.performerId() != null) {
            fhirObs.addPerformer(new Reference("Practitioner/" + vitalSign.performerId()));
        }

        // Value
        addVitalSignValue(fhirObs, vitalSign);

        return fhirObs;
    }

    /**
     * Create blood pressure observation (composite).
     */
    public Observation createBloodPressure(UUID id, UUID patientId, UUID encounterId,
                                            BigDecimal systolic, BigDecimal diastolic,
                                            Instant effectiveTime, UUID performerId) {
        Observation bp = new Observation();

        bp.setId(id.toString());
        bp.setStatus(Observation.ObservationStatus.FINAL);

        // Vital signs category
        bp.addCategory(new CodeableConcept()
                .addCoding(new Coding()
                        .setSystem("http://terminology.hl7.org/CodeSystem/observation-category")
                        .setCode("vital-signs")
                        .setDisplay("Vital Signs")));

        // Blood pressure panel code
        bp.setCode(new CodeableConcept()
                .addCoding(new Coding()
                        .setSystem(LOINC_SYSTEM)
                        .setCode(LOINC_BLOOD_PRESSURE)
                        .setDisplay("Blood pressure panel")));

        bp.setSubject(new Reference("Patient/" + patientId));
        if (encounterId != null) {
            bp.setEncounter(new Reference("Encounter/" + encounterId));
        }
        bp.setEffective(new DateTimeType(Date.from(effectiveTime)));
        if (performerId != null) {
            bp.addPerformer(new Reference("Practitioner/" + performerId));
        }

        // Systolic component
        Observation.ObservationComponentComponent systolicComp = bp.addComponent();
        systolicComp.setCode(new CodeableConcept()
                .addCoding(new Coding()
                        .setSystem(LOINC_SYSTEM)
                        .setCode(LOINC_SYSTOLIC_BP)
                        .setDisplay("Systolic blood pressure")));
        systolicComp.setValue(new Quantity()
                .setValue(systolic)
                .setUnit("mmHg")
                .setSystem("http://unitsofmeasure.org")
                .setCode("mm[Hg]"));

        // Diastolic component
        Observation.ObservationComponentComponent diastolicComp = bp.addComponent();
        diastolicComp.setCode(new CodeableConcept()
                .addCoding(new Coding()
                        .setSystem(LOINC_SYSTEM)
                        .setCode(LOINC_DIASTOLIC_BP)
                        .setDisplay("Diastolic blood pressure")));
        diastolicComp.setValue(new Quantity()
                .setValue(diastolic)
                .setUnit("mmHg")
                .setSystem("http://unitsofmeasure.org")
                .setCode("mm[Hg]"));

        return bp;
    }

    private Observation.ObservationStatus mapStatus(String status) {
        return switch (status.toUpperCase()) {
            case "REGISTERED" -> Observation.ObservationStatus.REGISTERED;
            case "PRELIMINARY" -> Observation.ObservationStatus.PRELIMINARY;
            case "FINAL" -> Observation.ObservationStatus.FINAL;
            case "AMENDED" -> Observation.ObservationStatus.AMENDED;
            case "CORRECTED" -> Observation.ObservationStatus.CORRECTED;
            case "CANCELLED" -> Observation.ObservationStatus.CANCELLED;
            case "ENTERED_IN_ERROR" -> Observation.ObservationStatus.ENTEREDINERROR;
            default -> Observation.ObservationStatus.UNKNOWN;
        };
    }

    private CodeableConcept createCategory(String category) {
        CodeableConcept cc = new CodeableConcept();
        Coding coding = new Coding()
                .setSystem("http://terminology.hl7.org/CodeSystem/observation-category");

        return switch (category.toUpperCase()) {
            case "VITAL_SIGNS" -> cc.addCoding(coding.setCode("vital-signs").setDisplay("Vital Signs"));
            case "LABORATORY" -> cc.addCoding(coding.setCode("laboratory").setDisplay("Laboratory"));
            case "IMAGING" -> cc.addCoding(coding.setCode("imaging").setDisplay("Imaging"));
            case "PROCEDURE" -> cc.addCoding(coding.setCode("procedure").setDisplay("Procedure"));
            case "SURVEY" -> cc.addCoding(coding.setCode("survey").setDisplay("Survey"));
            case "EXAM" -> cc.addCoding(coding.setCode("exam").setDisplay("Exam"));
            case "THERAPY" -> cc.addCoding(coding.setCode("therapy").setDisplay("Therapy"));
            case "ACTIVITY" -> cc.addCoding(coding.setCode("activity").setDisplay("Activity"));
            default -> cc.addCoding(coding.setCode("social-history").setDisplay("Social History"));
        };
    }

    private CodeableConcept createCode(String type, String code, String display) {
        CodeableConcept cc = new CodeableConcept();

        if (code != null && code.startsWith("LOINC:")) {
            cc.addCoding(new Coding()
                    .setSystem(LOINC_SYSTEM)
                    .setCode(code.substring(6))
                    .setDisplay(display));
        } else if (code != null && code.startsWith("SNOMED:")) {
            cc.addCoding(new Coding()
                    .setSystem(SNOMED_CT_SWEDEN)
                    .setCode(code.substring(7))
                    .setDisplay(display));
        } else {
            cc.setText(display != null ? display : type);
        }

        return cc;
    }

    private CodeableConcept createVitalSignCode(String vitalSignType) {
        CodeableConcept cc = new CodeableConcept();
        Coding coding = new Coding().setSystem(LOINC_SYSTEM);

        switch (vitalSignType.toUpperCase()) {
            case "HEART_RATE", "PULSE" -> coding.setCode(LOINC_HEART_RATE).setDisplay("Heart rate");
            case "RESPIRATORY_RATE" -> coding.setCode(LOINC_RESPIRATORY_RATE).setDisplay("Respiratory rate");
            case "BODY_TEMPERATURE", "TEMPERATURE" -> coding.setCode(LOINC_BODY_TEMPERATURE).setDisplay("Body temperature");
            case "OXYGEN_SATURATION", "SPO2" -> coding.setCode(LOINC_OXYGEN_SATURATION).setDisplay("Oxygen saturation");
            case "BODY_WEIGHT", "WEIGHT" -> coding.setCode(LOINC_BODY_WEIGHT).setDisplay("Body weight");
            case "BODY_HEIGHT", "HEIGHT" -> coding.setCode(LOINC_BODY_HEIGHT).setDisplay("Body height");
            case "BMI" -> coding.setCode(LOINC_BMI).setDisplay("Body mass index");
            case "PAIN_LEVEL", "PAIN" -> coding.setCode(LOINC_PAIN_LEVEL).setDisplay("Pain level");
            default -> coding.setCode("unknown").setDisplay(vitalSignType);
        }

        return cc.addCoding(coding);
    }

    private void addValue(Observation fhirObs, ObservationData observation) {
        if (observation.valueQuantity() != null) {
            fhirObs.setValue(new Quantity()
                    .setValue(observation.valueQuantity())
                    .setUnit(observation.unit())
                    .setSystem("http://unitsofmeasure.org")
                    .setCode(observation.unitCode()));
        } else if (observation.valueString() != null) {
            fhirObs.setValue(new StringType(observation.valueString()));
        } else if (observation.valueBoolean() != null) {
            fhirObs.setValue(new BooleanType(observation.valueBoolean()));
        } else if (observation.valueCodeableConceptCode() != null) {
            fhirObs.setValue(new CodeableConcept()
                    .addCoding(new Coding()
                            .setCode(observation.valueCodeableConceptCode())
                            .setDisplay(observation.valueCodeableConceptDisplay())));
        }
    }

    private void addVitalSignValue(Observation fhirObs, VitalSignData vitalSign) {
        String unit = getVitalSignUnit(vitalSign.type());
        String unitCode = getVitalSignUnitCode(vitalSign.type());

        fhirObs.setValue(new Quantity()
                .setValue(vitalSign.value())
                .setUnit(unit)
                .setSystem("http://unitsofmeasure.org")
                .setCode(unitCode));
    }

    private String getVitalSignUnit(String type) {
        return switch (type.toUpperCase()) {
            case "HEART_RATE", "PULSE", "RESPIRATORY_RATE" -> "beats/min";
            case "BODY_TEMPERATURE", "TEMPERATURE" -> "°C";
            case "OXYGEN_SATURATION", "SPO2" -> "%";
            case "BODY_WEIGHT", "WEIGHT" -> "kg";
            case "BODY_HEIGHT", "HEIGHT" -> "cm";
            case "BMI" -> "kg/m²";
            case "PAIN_LEVEL", "PAIN" -> "{score}";
            default -> "";
        };
    }

    private String getVitalSignUnitCode(String type) {
        return switch (type.toUpperCase()) {
            case "HEART_RATE", "PULSE" -> "/min";
            case "RESPIRATORY_RATE" -> "/min";
            case "BODY_TEMPERATURE", "TEMPERATURE" -> "Cel";
            case "OXYGEN_SATURATION", "SPO2" -> "%";
            case "BODY_WEIGHT", "WEIGHT" -> "kg";
            case "BODY_HEIGHT", "HEIGHT" -> "cm";
            case "BMI" -> "kg/m2";
            case "PAIN_LEVEL", "PAIN" -> "{score}";
            default -> "";
        };
    }

    private CodeableConcept createInterpretation(String interpretation) {
        CodeableConcept cc = new CodeableConcept();
        Coding coding = new Coding()
                .setSystem("http://terminology.hl7.org/CodeSystem/v3-ObservationInterpretation");

        return switch (interpretation.toUpperCase()) {
            case "NORMAL", "N" -> cc.addCoding(coding.setCode("N").setDisplay("Normal"));
            case "HIGH", "H" -> cc.addCoding(coding.setCode("H").setDisplay("High"));
            case "LOW", "L" -> cc.addCoding(coding.setCode("L").setDisplay("Low"));
            case "ABNORMAL", "A" -> cc.addCoding(coding.setCode("A").setDisplay("Abnormal"));
            case "CRITICAL_HIGH", "HH" -> cc.addCoding(coding.setCode("HH").setDisplay("Critical high"));
            case "CRITICAL_LOW", "LL" -> cc.addCoding(coding.setCode("LL").setDisplay("Critical low"));
            default -> cc.setText(interpretation);
        };
    }

    private void addReferenceRange(Observation fhirObs, ObservationData observation) {
        Observation.ObservationReferenceRangeComponent range = fhirObs.addReferenceRange();

        if (observation.referenceRangeLow() != null) {
            range.setLow(new Quantity()
                    .setValue(observation.referenceRangeLow())
                    .setUnit(observation.unit())
                    .setSystem("http://unitsofmeasure.org"));
        }
        if (observation.referenceRangeHigh() != null) {
            range.setHigh(new Quantity()
                    .setValue(observation.referenceRangeHigh())
                    .setUnit(observation.unit())
                    .setSystem("http://unitsofmeasure.org"));
        }
    }

    /**
     * General observation data record.
     */
    public record ObservationData(
            UUID id,
            UUID patientId,
            UUID encounterId,
            UUID performerId,
            String status,
            String category,
            String type,
            String code,
            String display,
            Instant effectiveTime,
            BigDecimal valueQuantity,
            String unit,
            String unitCode,
            String valueString,
            Boolean valueBoolean,
            String valueCodeableConceptCode,
            String valueCodeableConceptDisplay,
            String interpretation,
            BigDecimal referenceRangeLow,
            BigDecimal referenceRangeHigh,
            String note,
            String bodySite,
            String method,
            UUID deviceId
    ) {}

    /**
     * Simplified vital sign data record.
     */
    public record VitalSignData(
            UUID id,
            UUID patientId,
            UUID encounterId,
            UUID performerId,
            String type,
            BigDecimal value,
            Instant effectiveTime
    ) {}
}
