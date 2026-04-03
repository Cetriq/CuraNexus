package se.curanexus.integration.fhir;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r4.model.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

/**
 * FHIR R4 Resource endpoints.
 * Provides RESTful access to healthcare resources in FHIR format.
 *
 * Endpoints follow FHIR conventions:
 * - GET /fhir/Patient/{id} - Read patient
 * - GET /fhir/Patient?identifier=... - Search patients
 * - GET /fhir/Encounter/{id} - Read encounter
 * - GET /fhir/Observation/{id} - Read observation
 * - GET /fhir/Condition/{id} - Read condition/diagnosis
 */
@RestController
@RequestMapping("/fhir")
public class FhirResourceController {

    private static final MediaType FHIR_JSON = MediaType.parseMediaType("application/fhir+json");

    private final FhirContext fhirContext;
    private final PatientFhirMapper patientMapper;
    private final EncounterFhirMapper encounterMapper;
    private final ObservationFhirMapper observationMapper;
    private final ConditionFhirMapper conditionMapper;

    public FhirResourceController(
            FhirContext fhirContext,
            PatientFhirMapper patientMapper,
            EncounterFhirMapper encounterMapper,
            ObservationFhirMapper observationMapper,
            ConditionFhirMapper conditionMapper) {
        this.fhirContext = fhirContext;
        this.patientMapper = patientMapper;
        this.encounterMapper = encounterMapper;
        this.observationMapper = observationMapper;
        this.conditionMapper = conditionMapper;
    }

    // ========== Patient Resources ==========

    @GetMapping(value = "/Patient/{id}", produces = "application/fhir+json")
    public ResponseEntity<String> getPatient(@PathVariable String id) {
        // TODO: Fetch from patient service via RestClient
        // For now, return example data
        PatientFhirMapper.PatientData mockPatient = createMockPatient(id);
        Patient fhirPatient = patientMapper.toFhir(mockPatient);
        return ResponseEntity.ok()
                .contentType(FHIR_JSON)
                .body(toJson(fhirPatient));
    }

    @GetMapping(value = "/Patient", produces = "application/fhir+json")
    public ResponseEntity<String> searchPatients(
            @RequestParam(required = false) String identifier,
            @RequestParam(required = false) String family,
            @RequestParam(required = false) String given,
            @RequestParam(required = false, defaultValue = "10") int _count) {

        // TODO: Implement actual search via patient service
        List<Patient> patients = new ArrayList<>();

        // Mock response with search results
        if (identifier != null) {
            PatientFhirMapper.PatientData mockPatient = createMockPatientByIdentifier(identifier);
            if (mockPatient != null) {
                patients.add(patientMapper.toFhir(mockPatient));
            }
        }

        Bundle bundle = createSearchBundle(patients, "Patient");
        return ResponseEntity.ok()
                .contentType(FHIR_JSON)
                .body(toJson(bundle));
    }

    // ========== Encounter Resources ==========

    @GetMapping(value = "/Encounter/{id}", produces = "application/fhir+json")
    public ResponseEntity<String> getEncounter(@PathVariable String id) {
        // TODO: Fetch from encounter service
        EncounterFhirMapper.EncounterData mockEncounter = createMockEncounter(id);
        Encounter fhirEncounter = encounterMapper.toFhir(mockEncounter);
        return ResponseEntity.ok()
                .contentType(FHIR_JSON)
                .body(toJson(fhirEncounter));
    }

    @GetMapping(value = "/Encounter", produces = "application/fhir+json")
    public ResponseEntity<String> searchEncounters(
            @RequestParam(required = false) String patient,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String _class,
            @RequestParam(required = false, defaultValue = "10") int _count) {

        // TODO: Implement actual search
        List<Encounter> encounters = new ArrayList<>();

        Bundle bundle = createSearchBundle(encounters, "Encounter");
        return ResponseEntity.ok()
                .contentType(FHIR_JSON)
                .body(toJson(bundle));
    }

    // ========== Observation Resources ==========

    @GetMapping(value = "/Observation/{id}", produces = "application/fhir+json")
    public ResponseEntity<String> getObservation(@PathVariable String id) {
        // TODO: Fetch from journal service
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Observation not found");
    }

    @GetMapping(value = "/Observation", produces = "application/fhir+json")
    public ResponseEntity<String> searchObservations(
            @RequestParam(required = false) String patient,
            @RequestParam(required = false) String encounter,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String code,
            @RequestParam(required = false, defaultValue = "10") int _count) {

        List<Observation> observations = new ArrayList<>();

        // Example: If searching for vital signs
        if ("vital-signs".equals(category) && patient != null) {
            // Return mock vital signs
            observations.addAll(createMockVitalSigns(UUID.fromString(patient.replace("Patient/", ""))));
        }

        Bundle bundle = createSearchBundle(observations, "Observation");
        return ResponseEntity.ok()
                .contentType(FHIR_JSON)
                .body(toJson(bundle));
    }

    // ========== Condition Resources ==========

    @GetMapping(value = "/Condition/{id}", produces = "application/fhir+json")
    public ResponseEntity<String> getCondition(@PathVariable String id) {
        // TODO: Fetch from journal service
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Condition not found");
    }

    @GetMapping(value = "/Condition", produces = "application/fhir+json")
    public ResponseEntity<String> searchConditions(
            @RequestParam(required = false) String patient,
            @RequestParam(required = false) String encounter,
            @RequestParam(required = false) String category,
            @RequestParam(required = false, defaultValue = "10") int _count) {

        List<Condition> conditions = new ArrayList<>();

        Bundle bundle = createSearchBundle(conditions, "Condition");
        return ResponseEntity.ok()
                .contentType(FHIR_JSON)
                .body(toJson(bundle));
    }

    // ========== Helper Methods ==========

    private String toJson(Resource resource) {
        return fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(resource);
    }

    private Bundle createSearchBundle(List<? extends Resource> resources, String resourceType) {
        Bundle bundle = new Bundle();
        bundle.setId(UUID.randomUUID().toString());
        bundle.setType(Bundle.BundleType.SEARCHSET);
        bundle.setTotal(resources.size());
        bundle.setTimestamp(new Date());

        // Add link for self
        bundle.addLink()
                .setRelation("self")
                .setUrl("/fhir/" + resourceType);

        for (Resource resource : resources) {
            Bundle.BundleEntryComponent entry = bundle.addEntry();
            entry.setFullUrl("urn:uuid:" + resource.getId());
            entry.setResource(resource);
            entry.getSearch().setMode(Bundle.SearchEntryMode.MATCH);
        }

        return bundle;
    }

    // ========== Mock Data (to be replaced with actual service calls) ==========

    private PatientFhirMapper.PatientData createMockPatient(String id) {
        return new PatientFhirMapper.PatientData(
                UUID.fromString(id),
                "199001011234",
                "Anna",
                null,
                "Andersson",
                "FEMALE",
                LocalDate.of(1990, 1, 1),
                false,
                false,
                null,
                "+46701234567",
                "anna.andersson@email.se",
                "Storgatan 1",
                "Stockholm",
                "11122",
                "Stockholm"
        );
    }

    private PatientFhirMapper.PatientData createMockPatientByIdentifier(String identifier) {
        // Remove system prefix if present
        String personnummer = identifier.contains("|")
                ? identifier.split("\\|")[1]
                : identifier;

        return new PatientFhirMapper.PatientData(
                UUID.randomUUID(),
                personnummer,
                "Test",
                null,
                "Patient",
                "UNKNOWN",
                null,
                false,
                false,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    private EncounterFhirMapper.EncounterData createMockEncounter(String id) {
        return new EncounterFhirMapper.EncounterData(
                UUID.fromString(id),
                UUID.randomUUID(),
                "IN_PROGRESS",
                "EMERGENCY",
                "EMERGENCY_VISIT",
                "Akutmottagning",
                "URGENT",
                Instant.now().minusSeconds(3600),
                null,
                Instant.now().minusSeconds(3500),
                null,
                "SE2321000016-1234",
                "SE2321000016-AKUT",
                null,
                "ORANGE",
                List.of(),
                List.of("Bröstsmärta")
        );
    }

    private List<Observation> createMockVitalSigns(UUID patientId) {
        List<Observation> observations = new ArrayList<>();
        UUID encounterId = UUID.randomUUID();
        Instant now = Instant.now();

        // Heart rate
        observations.add(observationMapper.createVitalSign(
                new ObservationFhirMapper.VitalSignData(
                        UUID.randomUUID(), patientId, encounterId, null,
                        "HEART_RATE", BigDecimal.valueOf(78), now)));

        // Respiratory rate
        observations.add(observationMapper.createVitalSign(
                new ObservationFhirMapper.VitalSignData(
                        UUID.randomUUID(), patientId, encounterId, null,
                        "RESPIRATORY_RATE", BigDecimal.valueOf(16), now)));

        // Temperature
        observations.add(observationMapper.createVitalSign(
                new ObservationFhirMapper.VitalSignData(
                        UUID.randomUUID(), patientId, encounterId, null,
                        "BODY_TEMPERATURE", BigDecimal.valueOf(37.2), now)));

        // SpO2
        observations.add(observationMapper.createVitalSign(
                new ObservationFhirMapper.VitalSignData(
                        UUID.randomUUID(), patientId, encounterId, null,
                        "OXYGEN_SATURATION", BigDecimal.valueOf(98), now)));

        // Blood pressure
        observations.add(observationMapper.createBloodPressure(
                UUID.randomUUID(), patientId, encounterId,
                BigDecimal.valueOf(125), BigDecimal.valueOf(82),
                now, null));

        return observations;
    }
}
