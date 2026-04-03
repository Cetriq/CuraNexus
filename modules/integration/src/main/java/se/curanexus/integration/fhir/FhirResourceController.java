package se.curanexus.integration.fhir;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import se.curanexus.integration.client.EncounterServiceClient;
import se.curanexus.integration.client.JournalServiceClient;
import se.curanexus.integration.client.PatientServiceClient;

import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

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

    private static final Logger log = LoggerFactory.getLogger(FhirResourceController.class);
    private static final MediaType FHIR_JSON = MediaType.parseMediaType("application/fhir+json");

    private final FhirContext fhirContext;
    private final PatientFhirMapper patientMapper;
    private final EncounterFhirMapper encounterMapper;
    private final ObservationFhirMapper observationMapper;
    private final ConditionFhirMapper conditionMapper;
    private final PatientServiceClient patientClient;
    private final EncounterServiceClient encounterClient;
    private final JournalServiceClient journalClient;

    public FhirResourceController(
            FhirContext fhirContext,
            PatientFhirMapper patientMapper,
            EncounterFhirMapper encounterMapper,
            ObservationFhirMapper observationMapper,
            ConditionFhirMapper conditionMapper,
            PatientServiceClient patientClient,
            EncounterServiceClient encounterClient,
            JournalServiceClient journalClient) {
        this.fhirContext = fhirContext;
        this.patientMapper = patientMapper;
        this.encounterMapper = encounterMapper;
        this.observationMapper = observationMapper;
        this.conditionMapper = conditionMapper;
        this.patientClient = patientClient;
        this.encounterClient = encounterClient;
        this.journalClient = journalClient;
    }

    // ========== Patient Resources ==========

    @GetMapping(value = "/Patient/{id}", produces = "application/fhir+json")
    public Mono<ResponseEntity<String>> getPatient(@PathVariable String id) {
        log.info("FHIR Patient read: {}", id);

        UUID patientId;
        try {
            patientId = UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            return Mono.just(createOperationOutcome("Invalid patient ID format", OperationOutcome.IssueType.INVALID));
        }

        return patientClient.getPatient(patientId)
                .map(this::mapToFhirPatient)
                .map(fhirPatient -> ResponseEntity.ok()
                        .contentType(FHIR_JSON)
                        .body(toJson(fhirPatient)))
                .defaultIfEmpty(createOperationOutcome("Patient not found: " + id, OperationOutcome.IssueType.NOTFOUND))
                .onErrorResume(e -> {
                    log.error("Error fetching patient {}: {}", id, e.getMessage());
                    return Mono.just(createOperationOutcome("Error fetching patient", OperationOutcome.IssueType.EXCEPTION));
                });
    }

    @GetMapping(value = "/Patient", produces = "application/fhir+json")
    public Mono<ResponseEntity<String>> searchPatients(
            @RequestParam(required = false) String identifier,
            @RequestParam(required = false) String family,
            @RequestParam(required = false) String given,
            @RequestParam(required = false, defaultValue = "10") int _count) {

        log.info("FHIR Patient search: identifier={}, family={}, given={}", identifier, family, given);

        Mono<List<PatientServiceClient.PatientResponse>> searchResult;

        if (identifier != null) {
            String personnummer = extractPersonnummer(identifier);
            searchResult = patientClient.searchByPersonnummer(personnummer);
        } else if (family != null || given != null) {
            String name = family != null ? family : given;
            searchResult = patientClient.searchByName(name, _count);
        } else {
            Bundle bundle = createSearchBundle(List.of(), "Patient");
            return Mono.just(ResponseEntity.ok()
                    .contentType(FHIR_JSON)
                    .body(toJson(bundle)));
        }

        return searchResult
                .map(patients -> {
                    List<Patient> fhirPatients = patients.stream()
                            .map(this::mapToFhirPatient)
                            .collect(Collectors.toList());
                    Bundle bundle = createSearchBundle(fhirPatients, "Patient");
                    return ResponseEntity.ok()
                            .contentType(FHIR_JSON)
                            .body(toJson(bundle));
                })
                .onErrorResume(e -> {
                    log.error("Error searching patients: {}", e.getMessage());
                    return Mono.just(createOperationOutcome("Error searching patients", OperationOutcome.IssueType.EXCEPTION));
                });
    }

    // ========== Encounter Resources ==========

    @GetMapping(value = "/Encounter/{id}", produces = "application/fhir+json")
    public Mono<ResponseEntity<String>> getEncounter(@PathVariable String id) {
        log.info("FHIR Encounter read: {}", id);

        UUID encounterId;
        try {
            encounterId = UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            return Mono.just(createOperationOutcome("Invalid encounter ID format", OperationOutcome.IssueType.INVALID));
        }

        return encounterClient.getEncounter(encounterId)
                .map(this::mapToFhirEncounter)
                .map(fhirEncounter -> ResponseEntity.ok()
                        .contentType(FHIR_JSON)
                        .body(toJson(fhirEncounter)))
                .defaultIfEmpty(createOperationOutcome("Encounter not found: " + id, OperationOutcome.IssueType.NOTFOUND))
                .onErrorResume(e -> {
                    log.error("Error fetching encounter {}: {}", id, e.getMessage());
                    return Mono.just(createOperationOutcome("Error fetching encounter", OperationOutcome.IssueType.EXCEPTION));
                });
    }

    @GetMapping(value = "/Encounter", produces = "application/fhir+json")
    public Mono<ResponseEntity<String>> searchEncounters(
            @RequestParam(required = false) String patient,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, name = "class") String encounterClass,
            @RequestParam(required = false, defaultValue = "10") int _count) {

        log.info("FHIR Encounter search: patient={}, status={}, class={}", patient, status, encounterClass);

        Mono<List<EncounterServiceClient.EncounterResponse>> searchResult;

        if (patient != null) {
            UUID patientId = extractResourceId(patient);
            if (patientId == null) {
                return Mono.just(createOperationOutcome("Invalid patient reference", OperationOutcome.IssueType.INVALID));
            }
            searchResult = encounterClient.searchByPatient(patientId, _count);
        } else if (status != null) {
            String internalStatus = mapFhirStatusToInternal(status);
            searchResult = encounterClient.searchByStatus(internalStatus, _count);
        } else if (encounterClass != null) {
            String internalClass = mapFhirClassToInternal(encounterClass);
            searchResult = encounterClient.searchByClass(internalClass, _count);
        } else {
            Bundle bundle = createSearchBundle(List.of(), "Encounter");
            return Mono.just(ResponseEntity.ok()
                    .contentType(FHIR_JSON)
                    .body(toJson(bundle)));
        }

        return searchResult
                .map(encounters -> {
                    List<Encounter> fhirEncounters = encounters.stream()
                            .map(this::mapToFhirEncounter)
                            .collect(Collectors.toList());
                    Bundle bundle = createSearchBundle(fhirEncounters, "Encounter");
                    return ResponseEntity.ok()
                            .contentType(FHIR_JSON)
                            .body(toJson(bundle));
                })
                .onErrorResume(e -> {
                    log.error("Error searching encounters: {}", e.getMessage());
                    return Mono.just(createOperationOutcome("Error searching encounters", OperationOutcome.IssueType.EXCEPTION));
                });
    }

    // ========== Observation Resources ==========

    @GetMapping(value = "/Observation/{id}", produces = "application/fhir+json")
    public Mono<ResponseEntity<String>> getObservation(@PathVariable String id) {
        log.info("FHIR Observation read: {}", id);

        UUID observationId;
        try {
            observationId = UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            return Mono.just(createOperationOutcome("Invalid observation ID format", OperationOutcome.IssueType.INVALID));
        }

        return journalClient.getObservation(observationId)
                .map(this::mapToFhirObservation)
                .map(fhirObservation -> ResponseEntity.ok()
                        .contentType(FHIR_JSON)
                        .body(toJson(fhirObservation)))
                .defaultIfEmpty(createOperationOutcome("Observation not found: " + id, OperationOutcome.IssueType.NOTFOUND))
                .onErrorResume(e -> {
                    log.error("Error fetching observation {}: {}", id, e.getMessage());
                    return Mono.just(createOperationOutcome("Error fetching observation", OperationOutcome.IssueType.EXCEPTION));
                });
    }

    @GetMapping(value = "/Observation", produces = "application/fhir+json")
    public Mono<ResponseEntity<String>> searchObservations(
            @RequestParam(required = false) String patient,
            @RequestParam(required = false) String encounter,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String code,
            @RequestParam(required = false, defaultValue = "10") int _count) {

        log.info("FHIR Observation search: patient={}, encounter={}, category={}, code={}", patient, encounter, category, code);

        Mono<List<JournalServiceClient.ObservationResponse>> searchResult;

        if (patient != null) {
            UUID patientId = extractResourceId(patient);
            if (patientId == null) {
                return Mono.just(createOperationOutcome("Invalid patient reference", OperationOutcome.IssueType.INVALID));
            }

            if (category != null) {
                String internalCategory = mapFhirCategoryToInternal(category);
                searchResult = journalClient.searchObservationsByCategory(patientId, internalCategory, _count);
            } else {
                searchResult = journalClient.searchObservationsByPatient(patientId, _count);
            }
        } else if (encounter != null) {
            UUID encounterId = extractResourceId(encounter);
            if (encounterId == null) {
                return Mono.just(createOperationOutcome("Invalid encounter reference", OperationOutcome.IssueType.INVALID));
            }
            searchResult = journalClient.searchObservationsByEncounter(encounterId, _count);
        } else {
            Bundle bundle = createSearchBundle(List.of(), "Observation");
            return Mono.just(ResponseEntity.ok()
                    .contentType(FHIR_JSON)
                    .body(toJson(bundle)));
        }

        return searchResult
                .map(observations -> {
                    List<Observation> fhirObservations = observations.stream()
                            .map(this::mapToFhirObservation)
                            .collect(Collectors.toList());
                    Bundle bundle = createSearchBundle(fhirObservations, "Observation");
                    return ResponseEntity.ok()
                            .contentType(FHIR_JSON)
                            .body(toJson(bundle));
                })
                .onErrorResume(e -> {
                    log.error("Error searching observations: {}", e.getMessage());
                    return Mono.just(createOperationOutcome("Error searching observations", OperationOutcome.IssueType.EXCEPTION));
                });
    }

    // ========== Condition Resources ==========

    @GetMapping(value = "/Condition/{id}", produces = "application/fhir+json")
    public Mono<ResponseEntity<String>> getCondition(@PathVariable String id) {
        log.info("FHIR Condition read: {}", id);

        UUID diagnosisId;
        try {
            diagnosisId = UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            return Mono.just(createOperationOutcome("Invalid condition ID format", OperationOutcome.IssueType.INVALID));
        }

        return journalClient.getDiagnosis(diagnosisId)
                .map(this::mapToFhirCondition)
                .map(fhirCondition -> ResponseEntity.ok()
                        .contentType(FHIR_JSON)
                        .body(toJson(fhirCondition)))
                .defaultIfEmpty(createOperationOutcome("Condition not found: " + id, OperationOutcome.IssueType.NOTFOUND))
                .onErrorResume(e -> {
                    log.error("Error fetching condition {}: {}", id, e.getMessage());
                    return Mono.just(createOperationOutcome("Error fetching condition", OperationOutcome.IssueType.EXCEPTION));
                });
    }

    @GetMapping(value = "/Condition", produces = "application/fhir+json")
    public Mono<ResponseEntity<String>> searchConditions(
            @RequestParam(required = false) String patient,
            @RequestParam(required = false) String encounter,
            @RequestParam(required = false) String category,
            @RequestParam(required = false, defaultValue = "10") int _count) {

        log.info("FHIR Condition search: patient={}, encounter={}, category={}", patient, encounter, category);

        Mono<List<JournalServiceClient.DiagnosisResponse>> searchResult;

        if (patient != null) {
            UUID patientId = extractResourceId(patient);
            if (patientId == null) {
                return Mono.just(createOperationOutcome("Invalid patient reference", OperationOutcome.IssueType.INVALID));
            }
            searchResult = journalClient.searchDiagnosesByPatient(patientId, _count);
        } else if (encounter != null) {
            UUID encounterId = extractResourceId(encounter);
            if (encounterId == null) {
                return Mono.just(createOperationOutcome("Invalid encounter reference", OperationOutcome.IssueType.INVALID));
            }
            searchResult = journalClient.searchDiagnosesByEncounter(encounterId, _count);
        } else {
            Bundle bundle = createSearchBundle(List.of(), "Condition");
            return Mono.just(ResponseEntity.ok()
                    .contentType(FHIR_JSON)
                    .body(toJson(bundle)));
        }

        return searchResult
                .map(diagnoses -> {
                    List<Condition> fhirConditions = diagnoses.stream()
                            .map(this::mapToFhirCondition)
                            .collect(Collectors.toList());
                    Bundle bundle = createSearchBundle(fhirConditions, "Condition");
                    return ResponseEntity.ok()
                            .contentType(FHIR_JSON)
                            .body(toJson(bundle));
                })
                .onErrorResume(e -> {
                    log.error("Error searching conditions: {}", e.getMessage());
                    return Mono.just(createOperationOutcome("Error searching conditions", OperationOutcome.IssueType.EXCEPTION));
                });
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

    private ResponseEntity<String> createOperationOutcome(String message, OperationOutcome.IssueType issueType) {
        OperationOutcome outcome = new OperationOutcome();
        outcome.addIssue()
                .setSeverity(OperationOutcome.IssueSeverity.ERROR)
                .setCode(issueType)
                .setDiagnostics(message);

        HttpStatus status = switch (issueType) {
            case NOTFOUND -> HttpStatus.NOT_FOUND;
            case INVALID -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };

        return ResponseEntity.status(status)
                .contentType(FHIR_JSON)
                .body(toJson(outcome));
    }

    private String extractPersonnummer(String identifier) {
        if (identifier.contains("|")) {
            return identifier.split("\\|")[1];
        }
        return identifier;
    }

    private UUID extractResourceId(String reference) {
        try {
            String id = reference.contains("/")
                    ? reference.substring(reference.lastIndexOf('/') + 1)
                    : reference;
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String mapFhirStatusToInternal(String fhirStatus) {
        return switch (fhirStatus.toLowerCase()) {
            case "planned" -> "PLANNED";
            case "arrived" -> "ARRIVED";
            case "triaged" -> "TRIAGED";
            case "in-progress" -> "IN_PROGRESS";
            case "onleave" -> "ON_HOLD";
            case "finished" -> "FINISHED";
            case "cancelled" -> "CANCELLED";
            default -> fhirStatus.toUpperCase();
        };
    }

    private String mapFhirClassToInternal(String fhirClass) {
        return switch (fhirClass.toUpperCase()) {
            case "IMP" -> "INPATIENT";
            case "AMB" -> "OUTPATIENT";
            case "EMER" -> "EMERGENCY";
            case "HH" -> "HOME_VISIT";
            case "VR" -> "VIRTUAL";
            default -> fhirClass;
        };
    }

    private String mapFhirCategoryToInternal(String fhirCategory) {
        return switch (fhirCategory.toLowerCase()) {
            case "vital-signs" -> "VITAL_SIGNS";
            case "laboratory" -> "LABORATORY";
            case "imaging" -> "IMAGING";
            case "procedure" -> "PROCEDURE";
            case "survey" -> "SURVEY";
            case "exam" -> "EXAM";
            case "therapy" -> "THERAPY";
            case "activity" -> "ACTIVITY";
            default -> fhirCategory.toUpperCase();
        };
    }

    // ========== Mapping Methods ==========

    private Patient mapToFhirPatient(PatientServiceClient.PatientResponse response) {
        PatientFhirMapper.PatientData data = new PatientFhirMapper.PatientData(
                response.id(),
                response.personalIdentityNumber(),
                response.givenName(),
                response.middleName(),
                response.familyName(),
                response.gender(),
                response.dateOfBirth(),
                response.protectedIdentity(),
                response.deceased(),
                response.deceasedDate(),
                null, null, null, null, null, null
        );
        return patientMapper.toFhir(data);
    }

    private Encounter mapToFhirEncounter(EncounterServiceClient.EncounterResponse response) {
        EncounterFhirMapper.EncounterData data = new EncounterFhirMapper.EncounterData(
                response.id(),
                response.patientId(),
                response.status(),
                response.encounterClass(),
                response.type(),
                response.serviceType(),
                response.priority(),
                response.plannedStartTime(),
                response.plannedEndTime(),
                response.actualStartTime(),
                response.actualEndTime(),
                response.responsiblePractitionerHsaId(),
                response.responsibleUnitHsaId(),
                null,
                response.triageLevel(),
                List.of(),
                response.reasonCodes()
        );
        return encounterMapper.toFhir(data);
    }

    private Observation mapToFhirObservation(JournalServiceClient.ObservationResponse response) {
        Observation observation = new Observation();
        observation.setId(response.id().toString());
        observation.setStatus(Observation.ObservationStatus.FINAL);

        if (response.category() != null) {
            CodeableConcept category = new CodeableConcept();
            category.addCoding()
                    .setSystem("http://terminology.hl7.org/CodeSystem/observation-category")
                    .setCode(response.category().toLowerCase().replace("_", "-"));
            observation.addCategory(category);
        }

        CodeableConcept code = new CodeableConcept();
        if (response.codeSystem() != null && response.code() != null) {
            code.addCoding()
                    .setSystem(response.codeSystem())
                    .setCode(response.code())
                    .setDisplay(response.displayText());
        }
        observation.setCode(code);

        if (response.patientId() != null) {
            observation.setSubject(new Reference("Patient/" + response.patientId()));
        }

        if (response.encounterId() != null) {
            observation.setEncounter(new Reference("Encounter/" + response.encounterId()));
        }

        if (response.valueNumeric() != null) {
            Quantity quantity = new Quantity();
            quantity.setValue(response.valueNumeric());
            if (response.unit() != null) {
                quantity.setUnit(response.unit());
            }
            observation.setValue(quantity);
        } else if (response.valueString() != null) {
            observation.setValue(new StringType(response.valueString()));
        } else if (response.valueBoolean() != null) {
            observation.setValue(new BooleanType(response.valueBoolean()));
        }

        if (response.referenceRangeLow() != null || response.referenceRangeHigh() != null) {
            Observation.ObservationReferenceRangeComponent range = observation.addReferenceRange();
            if (response.referenceRangeLow() != null) {
                range.setLow(new Quantity().setValue(response.referenceRangeLow()));
            }
            if (response.referenceRangeHigh() != null) {
                range.setHigh(new Quantity().setValue(response.referenceRangeHigh()));
            }
        }

        if (response.observedAt() != null) {
            observation.setEffective(new DateTimeType(
                    Date.from(response.observedAt().atZone(ZoneId.systemDefault()).toInstant())));
        }

        if (response.interpretation() != null) {
            CodeableConcept interpretation = new CodeableConcept();
            interpretation.addCoding()
                    .setSystem("http://terminology.hl7.org/CodeSystem/v3-ObservationInterpretation")
                    .setCode(response.interpretation());
            observation.addInterpretation(interpretation);
        }

        return observation;
    }

    private Condition mapToFhirCondition(JournalServiceClient.DiagnosisResponse response) {
        // Map diagnosis type to clinical status
        String clinicalStatus = response.resolvedDate() != null ? "resolved" : "active";

        // Map diagnosis type to verification status
        String verificationStatus = "confirmed";

        // Map type to category
        String category = "encounter-diagnosis";
        if ("PROBLEM".equalsIgnoreCase(response.type())) {
            category = "problem-list-item";
        }

        ConditionFhirMapper.ConditionData data = new ConditionFhirMapper.ConditionData(
                response.id(),
                response.patientId(),
                response.encounterId(),
                clinicalStatus,
                verificationStatus,
                category,
                null, // severity
                response.codeSystem(),
                response.code(),
                response.displayText(),
                response.onsetDate(),
                null, // onsetString
                response.resolvedDate(),
                null, // bodySite
                response.recordedAt(),
                response.recordedById(),
                response.recordedById(), // asserterId same as recordedById
                null, // evidenceCode
                null, // note
                null, // laterality
                response.rank() != null && response.rank() == 1 // isPrimary
        );
        return conditionMapper.toFhir(data);
    }
}
