package se.curanexus.integration.fhir;

import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.Encounter;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static se.curanexus.integration.fhir.SwedishFhirExtensions.*;

/**
 * Maps Encounter domain objects to FHIR R4 Encounter resources.
 * Implements Swedish healthcare requirements including:
 * - Responsible practitioner HSA-ID
 * - Responsible unit HSA-ID
 * - RETTS triage levels
 */
@Component
public class EncounterFhirMapper {

    /**
     * Convert internal encounter representation to FHIR Encounter.
     */
    public Encounter toFhir(EncounterData encounter) {
        Encounter fhirEncounter = new Encounter();

        // Meta information
        fhirEncounter.getMeta()
                .addProfile(SWEDISH_ENCOUNTER_PROFILE)
                .setVersionId("1");

        // ID
        fhirEncounter.setId(encounter.id().toString());

        // Status
        fhirEncounter.setStatus(mapStatus(encounter.status()));

        // Class (encounter type: inpatient, outpatient, emergency, etc.)
        fhirEncounter.setClass_(mapEncounterClass(encounter.encounterClass()));

        // Type
        if (encounter.encounterType() != null) {
            fhirEncounter.addType(createEncounterType(encounter.encounterType()));
        }

        // Service type
        if (encounter.serviceType() != null) {
            fhirEncounter.setServiceType(new CodeableConcept()
                    .setText(encounter.serviceType()));
        }

        // Priority
        if (encounter.priority() != null) {
            fhirEncounter.setPriority(mapPriority(encounter.priority()));
        }

        // Subject (patient reference)
        fhirEncounter.setSubject(new Reference("Patient/" + encounter.patientId()));

        // Period
        addPeriod(fhirEncounter, encounter);

        // Participants
        if (encounter.participants() != null) {
            for (ParticipantData participant : encounter.participants()) {
                addParticipant(fhirEncounter, participant);
            }
        }

        // Reason for encounter
        if (encounter.reasonCodes() != null) {
            for (String reasonCode : encounter.reasonCodes()) {
                fhirEncounter.addReasonCode(new CodeableConcept().setText(reasonCode));
            }
        }

        // Responsible practitioner HSA-ID extension
        if (encounter.responsiblePractitionerHsaId() != null) {
            fhirEncounter.addExtension()
                    .setUrl(RESPONSIBLE_HSA_ID_URL)
                    .setValue(new Identifier()
                            .setSystem(HSA_ID_SYSTEM)
                            .setValue(encounter.responsiblePractitionerHsaId()));
        }

        // Responsible unit HSA-ID extension
        if (encounter.responsibleUnitHsaId() != null) {
            fhirEncounter.addExtension()
                    .setUrl(RESPONSIBLE_UNIT_HSA_ID_URL)
                    .setValue(new Identifier()
                            .setSystem(HSA_ID_SYSTEM)
                            .setValue(encounter.responsibleUnitHsaId()));
        }

        // RETTS triage level
        if (encounter.triageLevel() != null) {
            fhirEncounter.addExtension()
                    .setUrl(TRIAGE_LEVEL_URL)
                    .setValue(new CodeableConcept()
                            .addCoding(new Coding()
                                    .setSystem(RETTS_SYSTEM)
                                    .setCode(encounter.triageLevel())
                                    .setDisplay(getTriageDisplay(encounter.triageLevel()))));
        }

        // Service provider (organization)
        if (encounter.serviceProviderOrgId() != null) {
            fhirEncounter.setServiceProvider(new Reference("Organization/" + encounter.serviceProviderOrgId()));
        }

        return fhirEncounter;
    }

    private Encounter.EncounterStatus mapStatus(String status) {
        return switch (status.toUpperCase()) {
            case "PLANNED" -> Encounter.EncounterStatus.PLANNED;
            case "ARRIVED" -> Encounter.EncounterStatus.ARRIVED;
            case "TRIAGED" -> Encounter.EncounterStatus.TRIAGED;
            case "IN_PROGRESS" -> Encounter.EncounterStatus.INPROGRESS;
            case "ON_HOLD" -> Encounter.EncounterStatus.ONLEAVE;
            case "FINISHED" -> Encounter.EncounterStatus.FINISHED;
            case "CANCELLED" -> Encounter.EncounterStatus.CANCELLED;
            default -> Encounter.EncounterStatus.UNKNOWN;
        };
    }

    private Coding mapEncounterClass(String encounterClass) {
        Coding coding = new Coding()
                .setSystem("http://terminology.hl7.org/CodeSystem/v3-ActCode");

        return switch (encounterClass.toUpperCase()) {
            case "INPATIENT" -> coding.setCode("IMP").setDisplay("inpatient encounter");
            case "OUTPATIENT" -> coding.setCode("AMB").setDisplay("ambulatory");
            case "EMERGENCY" -> coding.setCode("EMER").setDisplay("emergency");
            case "HOME_VISIT" -> coding.setCode("HH").setDisplay("home health");
            case "VIRTUAL" -> coding.setCode("VR").setDisplay("virtual");
            case "DAY_CARE" -> coding.setCode("SS").setDisplay("short stay");
            default -> coding.setCode("AMB").setDisplay("ambulatory");
        };
    }

    private CodeableConcept createEncounterType(String type) {
        CodeableConcept cc = new CodeableConcept();
        // Map to Swedish encounter types
        return switch (type.toUpperCase()) {
            case "FIRST_VISIT" -> cc.setText("Nybesök")
                    .addCoding(new Coding().setCode("first-visit").setDisplay("First visit"));
            case "FOLLOW_UP" -> cc.setText("Återbesök")
                    .addCoding(new Coding().setCode("follow-up").setDisplay("Follow-up"));
            case "CONSULTATION" -> cc.setText("Konsultation")
                    .addCoding(new Coding().setCode("consultation").setDisplay("Consultation"));
            case "EMERGENCY_VISIT" -> cc.setText("Akutbesök")
                    .addCoding(new Coding().setCode("emergency").setDisplay("Emergency visit"));
            case "TELEPHONE" -> cc.setText("Telefonkontakt")
                    .addCoding(new Coding().setCode("telephone").setDisplay("Telephone"));
            case "VIDEO" -> cc.setText("Videomöte")
                    .addCoding(new Coding().setCode("video").setDisplay("Video visit"));
            default -> cc.setText(type);
        };
    }

    private CodeableConcept mapPriority(String priority) {
        CodeableConcept cc = new CodeableConcept();
        Coding coding = new Coding()
                .setSystem("http://terminology.hl7.org/CodeSystem/v3-ActPriority");

        return switch (priority.toUpperCase()) {
            case "ELECTIVE" -> cc.addCoding(coding.setCode("EL").setDisplay("elective"));
            case "ROUTINE" -> cc.addCoding(coding.setCode("R").setDisplay("routine"));
            case "URGENT" -> cc.addCoding(coding.setCode("UR").setDisplay("urgent"));
            case "EMERGENCY" -> cc.addCoding(coding.setCode("EM").setDisplay("emergency"));
            case "STAT" -> cc.addCoding(coding.setCode("S").setDisplay("stat"));
            default -> cc.addCoding(coding.setCode("R").setDisplay("routine"));
        };
    }

    private void addPeriod(Encounter fhirEncounter, EncounterData encounter) {
        Period period = new Period();
        boolean hasPeriod = false;

        // Use actual times if available, otherwise planned
        if (encounter.actualStartTime() != null) {
            period.setStart(Date.from(encounter.actualStartTime()));
            hasPeriod = true;
        } else if (encounter.plannedStartTime() != null) {
            period.setStart(Date.from(encounter.plannedStartTime()));
            hasPeriod = true;
        }

        if (encounter.actualEndTime() != null) {
            period.setEnd(Date.from(encounter.actualEndTime()));
            hasPeriod = true;
        } else if (encounter.plannedEndTime() != null) {
            period.setEnd(Date.from(encounter.plannedEndTime()));
            hasPeriod = true;
        }

        if (hasPeriod) {
            fhirEncounter.setPeriod(period);
        }
    }

    private void addParticipant(Encounter fhirEncounter, ParticipantData participant) {
        Encounter.EncounterParticipantComponent pc = fhirEncounter.addParticipant();

        // Participant type
        CodeableConcept type = new CodeableConcept();
        Coding typeCoding = new Coding()
                .setSystem("http://terminology.hl7.org/CodeSystem/v3-ParticipationType");

        switch (participant.role().toUpperCase()) {
            case "PRIMARY_PERFORMER" -> typeCoding.setCode("PPRF").setDisplay("primary performer");
            case "SECONDARY_PERFORMER" -> typeCoding.setCode("SPRF").setDisplay("secondary performer");
            case "CONSULTANT" -> typeCoding.setCode("CON").setDisplay("consultant");
            case "ADMITTER" -> typeCoding.setCode("ADM").setDisplay("admitter");
            case "ATTENDER" -> typeCoding.setCode("ATND").setDisplay("attender");
            case "REFERRER" -> typeCoding.setCode("REF").setDisplay("referrer");
            default -> typeCoding.setCode("PART").setDisplay("participant");
        }
        type.addCoding(typeCoding);
        pc.addType(type);

        // Practitioner reference
        pc.setIndividual(new Reference("Practitioner/" + participant.practitionerId()));

        // Period of participation
        if (participant.startTime() != null || participant.endTime() != null) {
            Period period = new Period();
            if (participant.startTime() != null) {
                period.setStart(Date.from(participant.startTime()));
            }
            if (participant.endTime() != null) {
                period.setEnd(Date.from(participant.endTime()));
            }
            pc.setPeriod(period);
        }
    }

    private String getTriageDisplay(String triageLevel) {
        return switch (triageLevel.toUpperCase()) {
            case "RED" -> "Röd - Livshotande";
            case "ORANGE" -> "Orange - Mycket brådskande";
            case "YELLOW" -> "Gul - Brådskande";
            case "GREEN" -> "Grön - Standard";
            case "BLUE" -> "Blå - Ej akut";
            default -> triageLevel;
        };
    }

    /**
     * Data transfer record for encounter data from internal systems.
     */
    public record EncounterData(
            UUID id,
            UUID patientId,
            String status,
            String encounterClass,
            String encounterType,
            String serviceType,
            String priority,
            Instant plannedStartTime,
            Instant plannedEndTime,
            Instant actualStartTime,
            Instant actualEndTime,
            String responsiblePractitionerHsaId,
            String responsibleUnitHsaId,
            String serviceProviderOrgId,
            String triageLevel,
            List<ParticipantData> participants,
            List<String> reasonCodes
    ) {}

    public record ParticipantData(
            UUID practitionerId,
            String role,
            Instant startTime,
            Instant endTime
    ) {}
}
