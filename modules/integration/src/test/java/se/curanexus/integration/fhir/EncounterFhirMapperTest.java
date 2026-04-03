package se.curanexus.integration.fhir;

import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static se.curanexus.integration.fhir.SwedishFhirExtensions.*;

class EncounterFhirMapperTest {

    private EncounterFhirMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new EncounterFhirMapper();
    }

    @Test
    void shouldMapBasicEncounterData() {
        // Given
        EncounterFhirMapper.EncounterData encounterData = createMinimalEncounter();

        // When
        Encounter fhirEncounter = mapper.toFhir(encounterData);

        // Then
        assertThat(fhirEncounter.getId()).isEqualTo(encounterData.id().toString());
        assertThat(fhirEncounter.getStatus()).isEqualTo(Encounter.EncounterStatus.INPROGRESS);
        assertThat(fhirEncounter.getClass_().getCode()).isEqualTo("EMER");
    }

    @Test
    void shouldMapPatientReference() {
        // Given
        UUID patientId = UUID.randomUUID();
        EncounterFhirMapper.EncounterData encounterData = createEncounterWithPatient(patientId);

        // When
        Encounter fhirEncounter = mapper.toFhir(encounterData);

        // Then
        assertThat(fhirEncounter.getSubject().getReference()).isEqualTo("Patient/" + patientId);
    }

    @Test
    void shouldMapAllEncounterStatuses() {
        assertStatusMapping("PLANNED", Encounter.EncounterStatus.PLANNED);
        assertStatusMapping("ARRIVED", Encounter.EncounterStatus.ARRIVED);
        assertStatusMapping("TRIAGED", Encounter.EncounterStatus.TRIAGED);
        assertStatusMapping("IN_PROGRESS", Encounter.EncounterStatus.INPROGRESS);
        assertStatusMapping("ON_HOLD", Encounter.EncounterStatus.ONLEAVE);
        assertStatusMapping("FINISHED", Encounter.EncounterStatus.FINISHED);
        assertStatusMapping("CANCELLED", Encounter.EncounterStatus.CANCELLED);
    }

    @Test
    void shouldMapEncounterClasses() {
        assertClassMapping("INPATIENT", "IMP");
        assertClassMapping("OUTPATIENT", "AMB");
        assertClassMapping("EMERGENCY", "EMER");
        assertClassMapping("HOME_VISIT", "HH");
        assertClassMapping("VIRTUAL", "VR");
    }

    @Test
    void shouldMapPeriod() {
        // Given
        Instant start = Instant.now().minusSeconds(3600);
        Instant end = Instant.now();
        EncounterFhirMapper.EncounterData encounterData = new EncounterFhirMapper.EncounterData(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "FINISHED",
                "OUTPATIENT",
                null, null, null,
                null, null,
                start, end,
                null, null, null, null, null, null
        );

        // When
        Encounter fhirEncounter = mapper.toFhir(encounterData);

        // Then
        assertThat(fhirEncounter.getPeriod()).isNotNull();
        assertThat(fhirEncounter.getPeriod().getStart()).isNotNull();
        assertThat(fhirEncounter.getPeriod().getEnd()).isNotNull();
    }

    @Test
    void shouldMapResponsiblePractitionerHsaId() {
        // Given
        String hsaId = "SE2321000016-1234";
        EncounterFhirMapper.EncounterData encounterData = new EncounterFhirMapper.EncounterData(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "IN_PROGRESS",
                "EMERGENCY",
                null, null, null,
                null, null, null, null,
                hsaId, null, null, null, null, null
        );

        // When
        Encounter fhirEncounter = mapper.toFhir(encounterData);

        // Then
        Extension ext = fhirEncounter.getExtensionByUrl(RESPONSIBLE_HSA_ID_URL);
        assertThat(ext).isNotNull();
        Identifier identifier = (Identifier) ext.getValue();
        assertThat(identifier.getSystem()).isEqualTo(HSA_ID_SYSTEM);
        assertThat(identifier.getValue()).isEqualTo(hsaId);
    }

    @Test
    void shouldMapResponsibleUnitHsaId() {
        // Given
        String unitHsaId = "SE2321000016-AKUT";
        EncounterFhirMapper.EncounterData encounterData = new EncounterFhirMapper.EncounterData(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "IN_PROGRESS",
                "EMERGENCY",
                null, null, null,
                null, null, null, null,
                null, unitHsaId, null, null, null, null
        );

        // When
        Encounter fhirEncounter = mapper.toFhir(encounterData);

        // Then
        Extension ext = fhirEncounter.getExtensionByUrl(RESPONSIBLE_UNIT_HSA_ID_URL);
        assertThat(ext).isNotNull();
        Identifier identifier = (Identifier) ext.getValue();
        assertThat(identifier.getSystem()).isEqualTo(HSA_ID_SYSTEM);
        assertThat(identifier.getValue()).isEqualTo(unitHsaId);
    }

    @Test
    void shouldMapTriageLevel() {
        // Given
        EncounterFhirMapper.EncounterData encounterData = new EncounterFhirMapper.EncounterData(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "TRIAGED",
                "EMERGENCY",
                null, null, null,
                null, null, null, null,
                null, null, null, "ORANGE", null, null
        );

        // When
        Encounter fhirEncounter = mapper.toFhir(encounterData);

        // Then
        Extension ext = fhirEncounter.getExtensionByUrl(TRIAGE_LEVEL_URL);
        assertThat(ext).isNotNull();
        CodeableConcept cc = (CodeableConcept) ext.getValue();
        assertThat(cc.getCodingFirstRep().getSystem()).isEqualTo(RETTS_SYSTEM);
        assertThat(cc.getCodingFirstRep().getCode()).isEqualTo("ORANGE");
        assertThat(cc.getCodingFirstRep().getDisplay()).contains("brådskande");
    }

    @Test
    void shouldMapParticipants() {
        // Given
        UUID practitionerId = UUID.randomUUID();
        EncounterFhirMapper.ParticipantData participant = new EncounterFhirMapper.ParticipantData(
                practitionerId,
                "PRIMARY_PERFORMER",
                Instant.now().minusSeconds(3600),
                null
        );

        EncounterFhirMapper.EncounterData encounterData = new EncounterFhirMapper.EncounterData(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "IN_PROGRESS",
                "OUTPATIENT",
                null, null, null,
                null, null, null, null,
                null, null, null, null,
                List.of(participant), null
        );

        // When
        Encounter fhirEncounter = mapper.toFhir(encounterData);

        // Then
        assertThat(fhirEncounter.getParticipant()).hasSize(1);
        Encounter.EncounterParticipantComponent pc = fhirEncounter.getParticipantFirstRep();
        assertThat(pc.getIndividual().getReference()).isEqualTo("Practitioner/" + practitionerId);
        assertThat(pc.getTypeFirstRep().getCodingFirstRep().getCode()).isEqualTo("PPRF");
    }

    @Test
    void shouldMapPriority() {
        // Given
        EncounterFhirMapper.EncounterData encounterData = new EncounterFhirMapper.EncounterData(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "IN_PROGRESS",
                "EMERGENCY",
                null, null, "URGENT",
                null, null, null, null,
                null, null, null, null, null, null
        );

        // When
        Encounter fhirEncounter = mapper.toFhir(encounterData);

        // Then
        assertThat(fhirEncounter.getPriority()).isNotNull();
        assertThat(fhirEncounter.getPriority().getCodingFirstRep().getCode()).isEqualTo("UR");
    }

    @Test
    void shouldMapReasonCodes() {
        // Given
        EncounterFhirMapper.EncounterData encounterData = new EncounterFhirMapper.EncounterData(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "IN_PROGRESS",
                "EMERGENCY",
                null, null, null,
                null, null, null, null,
                null, null, null, null,
                null, List.of("Bröstsmärta", "Andningsbesvär")
        );

        // When
        Encounter fhirEncounter = mapper.toFhir(encounterData);

        // Then
        assertThat(fhirEncounter.getReasonCode()).hasSize(2);
        assertThat(fhirEncounter.getReasonCode())
                .extracting(cc -> cc.getText())
                .containsExactly("Bröstsmärta", "Andningsbesvär");
    }

    @Test
    void shouldIncludeSwedishEncounterProfile() {
        // Given
        EncounterFhirMapper.EncounterData encounterData = createMinimalEncounter();

        // When
        Encounter fhirEncounter = mapper.toFhir(encounterData);

        // Then
        assertThat(fhirEncounter.getMeta().getProfile())
                .extracting(CanonicalType::getValue)
                .contains(SWEDISH_ENCOUNTER_PROFILE);
    }

    private void assertStatusMapping(String input, Encounter.EncounterStatus expected) {
        EncounterFhirMapper.EncounterData encounterData = new EncounterFhirMapper.EncounterData(
                UUID.randomUUID(), UUID.randomUUID(), input, "OUTPATIENT",
                null, null, null, null, null, null, null,
                null, null, null, null, null, null
        );
        Encounter fhirEncounter = mapper.toFhir(encounterData);
        assertThat(fhirEncounter.getStatus()).isEqualTo(expected);
    }

    private void assertClassMapping(String input, String expectedCode) {
        EncounterFhirMapper.EncounterData encounterData = new EncounterFhirMapper.EncounterData(
                UUID.randomUUID(), UUID.randomUUID(), "IN_PROGRESS", input,
                null, null, null, null, null, null, null,
                null, null, null, null, null, null
        );
        Encounter fhirEncounter = mapper.toFhir(encounterData);
        assertThat(fhirEncounter.getClass_().getCode()).isEqualTo(expectedCode);
    }

    private EncounterFhirMapper.EncounterData createMinimalEncounter() {
        return new EncounterFhirMapper.EncounterData(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "IN_PROGRESS",
                "EMERGENCY",
                null, null, null,
                null, null, null, null,
                null, null, null, null, null, null
        );
    }

    private EncounterFhirMapper.EncounterData createEncounterWithPatient(UUID patientId) {
        return new EncounterFhirMapper.EncounterData(
                UUID.randomUUID(),
                patientId,
                "IN_PROGRESS",
                "EMERGENCY",
                null, null, null,
                null, null, null, null,
                null, null, null, null, null, null
        );
    }
}
