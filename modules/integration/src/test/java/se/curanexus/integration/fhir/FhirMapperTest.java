package se.curanexus.integration.fhir;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FhirMapperTest {

    private FhirMapper fhirMapper;
    private FhirContext fhirContext;

    @BeforeEach
    void setUp() {
        fhirContext = FhirContext.forR4();
        fhirMapper = new FhirMapper(fhirContext);
    }

    @Test
    void shouldMapPatientDataToFhirPatient() {
        Map<String, Object> patientData = Map.of(
                "id", UUID.randomUUID().toString(),
                "personnummer", "199001011234",
                "firstName", "Anna",
                "lastName", "Andersson",
                "gender", "FEMALE",
                "dateOfBirth", "1990-01-01",
                "active", true
        );

        Patient patient = fhirMapper.mapToFhirPatient(patientData);

        assertNotNull(patient);
        assertEquals("Anna", patient.getNameFirstRep().getGivenAsSingleString());
        assertEquals("Andersson", patient.getNameFirstRep().getFamily());
        assertEquals(Enumerations.AdministrativeGender.FEMALE, patient.getGender());
        assertTrue(patient.getActive());
    }

    @Test
    void shouldMapPersonnummerAsIdentifier() {
        Map<String, Object> patientData = Map.of(
                "personnummer", "199001011234"
        );

        Patient patient = fhirMapper.mapToFhirPatient(patientData);

        assertFalse(patient.getIdentifier().isEmpty());
        assertEquals("urn:oid:1.2.752.129.2.1.3.1", patient.getIdentifierFirstRep().getSystem());
        assertEquals("199001011234", patient.getIdentifierFirstRep().getValue());
    }

    @Test
    void shouldMapPatientAddress() {
        Map<String, Object> patientData = Map.of(
                "streetAddress", "Storgatan 1",
                "city", "Stockholm",
                "postalCode", "11122"
        );

        Patient patient = fhirMapper.mapToFhirPatient(patientData);

        assertFalse(patient.getAddress().isEmpty());
        assertEquals("Storgatan 1", patient.getAddressFirstRep().getLine().get(0).getValue());
        assertEquals("Stockholm", patient.getAddressFirstRep().getCity());
        assertEquals("11122", patient.getAddressFirstRep().getPostalCode());
        assertEquals("SE", patient.getAddressFirstRep().getCountry());
    }

    @Test
    void shouldMapPatientTelecom() {
        Map<String, Object> patientData = Map.of(
                "phoneNumber", "+46701234567",
                "email", "anna@example.com"
        );

        Patient patient = fhirMapper.mapToFhirPatient(patientData);

        assertEquals(2, patient.getTelecom().size());
    }

    @Test
    void shouldMapEncounterDataToFhirEncounter() {
        UUID patientId = UUID.randomUUID();
        Map<String, Object> encounterData = Map.of(
                "id", UUID.randomUUID().toString(),
                "patientId", patientId.toString(),
                "status", "IN_PROGRESS",
                "encounterClass", "OUTPATIENT",
                "priority", "URGENT"
        );

        Encounter encounter = fhirMapper.mapToFhirEncounter(encounterData);

        assertNotNull(encounter);
        assertEquals(Encounter.EncounterStatus.INPROGRESS, encounter.getStatus());
        assertEquals("AMB", encounter.getClass_().getCode());
        assertEquals("Patient/" + patientId, encounter.getSubject().getReference());
    }

    @Test
    void shouldMapAllEncounterStatuses() {
        Map<String, Encounter.EncounterStatus> statusMap = Map.of(
                "PLANNED", Encounter.EncounterStatus.PLANNED,
                "ARRIVED", Encounter.EncounterStatus.ARRIVED,
                "TRIAGED", Encounter.EncounterStatus.TRIAGED,
                "IN_PROGRESS", Encounter.EncounterStatus.INPROGRESS,
                "FINISHED", Encounter.EncounterStatus.FINISHED,
                "CANCELLED", Encounter.EncounterStatus.CANCELLED
        );

        for (var entry : statusMap.entrySet()) {
            Map<String, Object> data = Map.of("status", entry.getKey());
            Encounter encounter = fhirMapper.mapToFhirEncounter(data);
            assertEquals(entry.getValue(), encounter.getStatus(), "Failed for status: " + entry.getKey());
        }
    }

    @Test
    void shouldMapAllEncounterClasses() {
        Map<String, String> classMap = Map.of(
                "INPATIENT", "IMP",
                "OUTPATIENT", "AMB",
                "EMERGENCY", "EMER",
                "HOME_VISIT", "HH",
                "VIRTUAL", "VR"
        );

        for (var entry : classMap.entrySet()) {
            Map<String, Object> data = Map.of("encounterClass", entry.getKey());
            Encounter encounter = fhirMapper.mapToFhirEncounter(data);
            assertEquals(entry.getValue(), encounter.getClass_().getCode(), "Failed for class: " + entry.getKey());
        }
    }

    @Test
    void shouldConvertResourceToJson() {
        Patient patient = new Patient();
        patient.setId("test-id");
        patient.addName().setFamily("Test");

        String json = fhirMapper.toJson(patient);

        assertNotNull(json);
        assertTrue(json.contains("test-id"));
        assertTrue(json.contains("Test"));
    }

    @Test
    void shouldCreateSearchBundle() {
        Patient patient1 = new Patient();
        patient1.setId("patient-1");
        Patient patient2 = new Patient();
        patient2.setId("patient-2");

        var bundle = fhirMapper.createSearchBundle(java.util.List.of(patient1, patient2), "Patient");

        assertNotNull(bundle);
        assertEquals(2, bundle.getTotal());
        assertEquals(org.hl7.fhir.r4.model.Bundle.BundleType.SEARCHSET, bundle.getType());
        assertEquals(2, bundle.getEntry().size());
    }
}
