package se.curanexus.integration.fhir;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Component
public class FhirMapper {

    private final FhirContext fhirContext;

    public FhirMapper(FhirContext fhirContext) {
        this.fhirContext = fhirContext;
    }

    public Patient mapToFhirPatient(Map<String, Object> patientData) {
        Patient patient = new Patient();

        // ID
        if (patientData.get("id") != null) {
            patient.setId(patientData.get("id").toString());
        }

        // Identifier (personnummer)
        if (patientData.get("personnummer") != null) {
            patient.addIdentifier()
                    .setSystem("urn:oid:1.2.752.129.2.1.3.1") // Swedish personnummer OID
                    .setValue(patientData.get("personnummer").toString())
                    .setType(new CodeableConcept()
                            .addCoding(new Coding()
                                    .setSystem("http://terminology.hl7.org/CodeSystem/v2-0203")
                                    .setCode("NI")
                                    .setDisplay("National unique individual identifier")));
        }

        // Name
        HumanName name = patient.addName();
        if (patientData.get("firstName") != null) {
            name.addGiven(patientData.get("firstName").toString());
        }
        if (patientData.get("lastName") != null) {
            name.setFamily(patientData.get("lastName").toString());
        }

        // Gender
        if (patientData.get("gender") != null) {
            String gender = patientData.get("gender").toString();
            patient.setGender(switch (gender.toUpperCase()) {
                case "MALE" -> Enumerations.AdministrativeGender.MALE;
                case "FEMALE" -> Enumerations.AdministrativeGender.FEMALE;
                case "OTHER" -> Enumerations.AdministrativeGender.OTHER;
                default -> Enumerations.AdministrativeGender.UNKNOWN;
            });
        }

        // Birth date
        if (patientData.get("dateOfBirth") != null) {
            try {
                LocalDate dob = LocalDate.parse(patientData.get("dateOfBirth").toString());
                patient.setBirthDate(Date.from(dob.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            } catch (Exception ignored) {
                // Skip if date parsing fails
            }
        }

        // Active status
        if (patientData.get("active") != null) {
            patient.setActive((Boolean) patientData.get("active"));
        }

        // Address
        if (patientData.get("streetAddress") != null || patientData.get("city") != null) {
            Address address = patient.addAddress();
            if (patientData.get("streetAddress") != null) {
                address.addLine(patientData.get("streetAddress").toString());
            }
            if (patientData.get("city") != null) {
                address.setCity(patientData.get("city").toString());
            }
            if (patientData.get("postalCode") != null) {
                address.setPostalCode(patientData.get("postalCode").toString());
            }
            address.setCountry("SE");
        }

        // Telecom
        if (patientData.get("phoneNumber") != null) {
            patient.addTelecom()
                    .setSystem(ContactPoint.ContactPointSystem.PHONE)
                    .setValue(patientData.get("phoneNumber").toString())
                    .setUse(ContactPoint.ContactPointUse.HOME);
        }
        if (patientData.get("email") != null) {
            patient.addTelecom()
                    .setSystem(ContactPoint.ContactPointSystem.EMAIL)
                    .setValue(patientData.get("email").toString())
                    .setUse(ContactPoint.ContactPointUse.HOME);
        }

        return patient;
    }

    public Encounter mapToFhirEncounter(Map<String, Object> encounterData) {
        Encounter encounter = new Encounter();

        // ID
        if (encounterData.get("id") != null) {
            encounter.setId(encounterData.get("id").toString());
        }

        // Status
        if (encounterData.get("status") != null) {
            String status = encounterData.get("status").toString();
            encounter.setStatus(switch (status) {
                case "PLANNED" -> Encounter.EncounterStatus.PLANNED;
                case "ARRIVED" -> Encounter.EncounterStatus.ARRIVED;
                case "TRIAGED" -> Encounter.EncounterStatus.TRIAGED;
                case "IN_PROGRESS" -> Encounter.EncounterStatus.INPROGRESS;
                case "FINISHED" -> Encounter.EncounterStatus.FINISHED;
                case "CANCELLED" -> Encounter.EncounterStatus.CANCELLED;
                default -> Encounter.EncounterStatus.UNKNOWN;
            });
        }

        // Class
        if (encounterData.get("encounterClass") != null) {
            String encClass = encounterData.get("encounterClass").toString();
            Coding classCoding = new Coding()
                    .setSystem("http://terminology.hl7.org/CodeSystem/v3-ActCode");
            switch (encClass) {
                case "INPATIENT" -> classCoding.setCode("IMP").setDisplay("inpatient encounter");
                case "OUTPATIENT" -> classCoding.setCode("AMB").setDisplay("ambulatory");
                case "EMERGENCY" -> classCoding.setCode("EMER").setDisplay("emergency");
                case "HOME_VISIT" -> classCoding.setCode("HH").setDisplay("home health");
                case "VIRTUAL" -> classCoding.setCode("VR").setDisplay("virtual");
                default -> classCoding.setCode("AMB").setDisplay("ambulatory");
            }
            encounter.setClass_(classCoding);
        }

        // Subject (patient reference)
        if (encounterData.get("patientId") != null) {
            encounter.setSubject(new Reference("Patient/" + encounterData.get("patientId")));
        }

        // Period
        Period period = new Period();
        if (encounterData.get("plannedStart") != null) {
            try {
                Instant start = Instant.parse(encounterData.get("plannedStart").toString());
                period.setStart(Date.from(start));
            } catch (Exception ignored) {
            }
        }
        if (encounterData.get("plannedEnd") != null) {
            try {
                Instant end = Instant.parse(encounterData.get("plannedEnd").toString());
                period.setEnd(Date.from(end));
            } catch (Exception ignored) {
            }
        }
        if (period.hasStart() || period.hasEnd()) {
            encounter.setPeriod(period);
        }

        // Priority
        if (encounterData.get("priority") != null) {
            String priority = encounterData.get("priority").toString();
            CodeableConcept priorityConcept = new CodeableConcept()
                    .addCoding(new Coding()
                            .setSystem("http://terminology.hl7.org/CodeSystem/v3-ActPriority")
                            .setCode(switch (priority) {
                                case "ELECTIVE" -> "EL";
                                case "URGENT" -> "UR";
                                case "EMERGENCY" -> "EM";
                                default -> "R";
                            }));
            encounter.setPriority(priorityConcept);
        }

        return encounter;
    }

    public String toJson(Resource resource) {
        return fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(resource);
    }

    public Bundle createSearchBundle(java.util.List<? extends Resource> resources, String resourceType) {
        Bundle bundle = new Bundle();
        bundle.setId(UUID.randomUUID().toString());
        bundle.setType(Bundle.BundleType.SEARCHSET);
        bundle.setTotal(resources.size());
        bundle.setTimestamp(new Date());

        for (Resource resource : resources) {
            Bundle.BundleEntryComponent entry = bundle.addEntry();
            entry.setFullUrl("urn:uuid:" + resource.getId());
            entry.setResource(resource);
            entry.getSearch().setMode(Bundle.SearchEntryMode.MATCH);
        }

        return bundle;
    }
}
