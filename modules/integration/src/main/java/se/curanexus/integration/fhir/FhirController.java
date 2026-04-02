package se.curanexus.integration.fhir;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r4.model.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Date;

@RestController
@RequestMapping("/fhir")
public class FhirController {

    private final FhirContext fhirContext;

    public FhirController(FhirContext fhirContext) {
        this.fhirContext = fhirContext;
    }

    @GetMapping(value = "/metadata", produces = "application/fhir+json")
    public Mono<ResponseEntity<String>> getCapabilityStatement() {
        CapabilityStatement capabilityStatement = buildCapabilityStatement();
        String json = fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(capabilityStatement);
        return Mono.just(ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/fhir+json"))
                .body(json));
    }

    private CapabilityStatement buildCapabilityStatement() {
        CapabilityStatement cs = new CapabilityStatement();
        cs.setId("curanexus-capability-statement");
        cs.setUrl("http://curanexus.se/fhir/CapabilityStatement/curanexus");
        cs.setVersion("1.0.0");
        cs.setName("CuraNexusCapabilityStatement");
        cs.setTitle("Cura Nexus FHIR Capability Statement");
        cs.setStatus(Enumerations.PublicationStatus.ACTIVE);
        cs.setExperimental(false);
        cs.setDate(new Date());
        cs.setPublisher("Cura Nexus");
        cs.setDescription("FHIR R4 Capability Statement for Cura Nexus Healthcare Platform");
        cs.setKind(CapabilityStatement.CapabilityStatementKind.INSTANCE);
        cs.setFhirVersion(Enumerations.FHIRVersion._4_0_1);
        cs.addFormat("application/fhir+json");
        cs.addFormat("application/fhir+xml");

        // Software information
        CapabilityStatement.CapabilityStatementSoftwareComponent software = cs.getSoftware();
        software.setName("Cura Nexus Gateway");
        software.setVersion("1.0.0");

        // REST capabilities
        CapabilityStatement.CapabilityStatementRestComponent rest = cs.addRest();
        rest.setMode(CapabilityStatement.RestfulCapabilityMode.SERVER);
        rest.setDocumentation("RESTful FHIR Server for Cura Nexus");

        // Security
        CapabilityStatement.CapabilityStatementRestSecurityComponent security = rest.getSecurity();
        security.setCors(true);
        security.setDescription("OAuth2 / OpenID Connect authentication required");

        // Patient resource
        CapabilityStatement.CapabilityStatementRestResourceComponent patientResource = rest.addResource();
        patientResource.setType("Patient");
        patientResource.setProfile("http://hl7.org/fhir/StructureDefinition/Patient");
        patientResource.addInteraction().setCode(CapabilityStatement.TypeRestfulInteraction.READ);
        patientResource.addInteraction().setCode(CapabilityStatement.TypeRestfulInteraction.SEARCHTYPE);
        patientResource.setVersioning(CapabilityStatement.ResourceVersionPolicy.VERSIONED);
        patientResource.setReadHistory(false);
        patientResource.setUpdateCreate(false);
        patientResource.setConditionalCreate(false);
        patientResource.setConditionalRead(CapabilityStatement.ConditionalReadStatus.NOTSUPPORTED);
        patientResource.setConditionalUpdate(false);
        patientResource.setConditionalDelete(CapabilityStatement.ConditionalDeleteStatus.NOTSUPPORTED);

        // Patient search parameters
        patientResource.addSearchParam()
                .setName("identifier")
                .setType(Enumerations.SearchParamType.TOKEN)
                .setDocumentation("Patient identifier (personnummer)");
        patientResource.addSearchParam()
                .setName("family")
                .setType(Enumerations.SearchParamType.STRING)
                .setDocumentation("Family name");
        patientResource.addSearchParam()
                .setName("given")
                .setType(Enumerations.SearchParamType.STRING)
                .setDocumentation("Given name");

        // Encounter resource
        CapabilityStatement.CapabilityStatementRestResourceComponent encounterResource = rest.addResource();
        encounterResource.setType("Encounter");
        encounterResource.setProfile("http://hl7.org/fhir/StructureDefinition/Encounter");
        encounterResource.addInteraction().setCode(CapabilityStatement.TypeRestfulInteraction.READ);
        encounterResource.addInteraction().setCode(CapabilityStatement.TypeRestfulInteraction.SEARCHTYPE);
        encounterResource.setVersioning(CapabilityStatement.ResourceVersionPolicy.VERSIONED);

        // Encounter search parameters
        encounterResource.addSearchParam()
                .setName("patient")
                .setType(Enumerations.SearchParamType.REFERENCE)
                .setDocumentation("Patient reference");
        encounterResource.addSearchParam()
                .setName("status")
                .setType(Enumerations.SearchParamType.TOKEN)
                .setDocumentation("Encounter status");

        // Observation resource
        CapabilityStatement.CapabilityStatementRestResourceComponent observationResource = rest.addResource();
        observationResource.setType("Observation");
        observationResource.setProfile("http://hl7.org/fhir/StructureDefinition/Observation");
        observationResource.addInteraction().setCode(CapabilityStatement.TypeRestfulInteraction.READ);
        observationResource.addInteraction().setCode(CapabilityStatement.TypeRestfulInteraction.SEARCHTYPE);

        // Condition resource (diagnoses)
        CapabilityStatement.CapabilityStatementRestResourceComponent conditionResource = rest.addResource();
        conditionResource.setType("Condition");
        conditionResource.setProfile("http://hl7.org/fhir/StructureDefinition/Condition");
        conditionResource.addInteraction().setCode(CapabilityStatement.TypeRestfulInteraction.READ);
        conditionResource.addInteraction().setCode(CapabilityStatement.TypeRestfulInteraction.SEARCHTYPE);

        // Procedure resource
        CapabilityStatement.CapabilityStatementRestResourceComponent procedureResource = rest.addResource();
        procedureResource.setType("Procedure");
        procedureResource.setProfile("http://hl7.org/fhir/StructureDefinition/Procedure");
        procedureResource.addInteraction().setCode(CapabilityStatement.TypeRestfulInteraction.READ);
        procedureResource.addInteraction().setCode(CapabilityStatement.TypeRestfulInteraction.SEARCHTYPE);

        return cs;
    }
}
