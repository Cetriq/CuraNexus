package se.curanexus.integration.fhir;

import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static se.curanexus.integration.fhir.SwedishFhirExtensions.*;

class PatientFhirMapperTest {

    private PatientFhirMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new PatientFhirMapper();
    }

    @Test
    void shouldMapBasicPatientData() {
        // Given
        PatientFhirMapper.PatientData patientData = new PatientFhirMapper.PatientData(
                UUID.randomUUID(),
                "199001011234",
                "Anna",
                "Maria",
                "Andersson",
                "FEMALE",
                LocalDate.of(1990, 1, 1),
                false,
                false,
                null,
                "+46701234567",
                "anna@example.se",
                "Storgatan 1",
                "Stockholm",
                "11122",
                "Stockholm kommun"
        );

        // When
        Patient fhirPatient = mapper.toFhir(patientData);

        // Then
        assertThat(fhirPatient.getId()).isEqualTo(patientData.id().toString());
        assertThat(fhirPatient.getActive()).isTrue();
        assertThat(fhirPatient.getGender()).isEqualTo(Enumerations.AdministrativeGender.FEMALE);
        assertThat(fhirPatient.getBirthDate()).isNotNull();
    }

    @Test
    void shouldMapPersonnummerAsIdentifier() {
        // Given
        PatientFhirMapper.PatientData patientData = createMinimalPatient("199001011234");

        // When
        Patient fhirPatient = mapper.toFhir(patientData);

        // Then
        assertThat(fhirPatient.getIdentifier()).hasSize(1);
        Identifier identifier = fhirPatient.getIdentifierFirstRep();
        assertThat(identifier.getSystem()).isEqualTo(PERSONNUMMER_SYSTEM);
        assertThat(identifier.getValue()).isEqualTo("199001011234");
        assertThat(identifier.getUse()).isEqualTo(Identifier.IdentifierUse.OFFICIAL);
    }

    @Test
    void shouldDetectSamordningsnummer() {
        // Given - samordningsnummer has day + 60
        PatientFhirMapper.PatientData patientData = createMinimalPatient("199001611234");

        // When
        Patient fhirPatient = mapper.toFhir(patientData);

        // Then
        Identifier identifier = fhirPatient.getIdentifierFirstRep();
        assertThat(identifier.getSystem()).isEqualTo(SAMORDNINGSNUMMER_SYSTEM);
    }

    @Test
    void shouldMapName() {
        // Given
        PatientFhirMapper.PatientData patientData = new PatientFhirMapper.PatientData(
                UUID.randomUUID(),
                "199001011234",
                "Anna",
                "Maria",
                "Andersson",
                "FEMALE",
                LocalDate.of(1990, 1, 1),
                false, false, null,
                null, null, null, null, null, null
        );

        // When
        Patient fhirPatient = mapper.toFhir(patientData);

        // Then
        assertThat(fhirPatient.getName()).hasSize(1);
        HumanName name = fhirPatient.getNameFirstRep();
        assertThat(name.getFamily()).isEqualTo("Andersson");
        assertThat(name.getGiven()).extracting(StringType::getValue)
                .containsExactly("Anna", "Maria");
        assertThat(name.getText()).isEqualTo("Anna Maria Andersson");
    }

    @Test
    void shouldMapProtectedIdentityExtension() {
        // Given
        PatientFhirMapper.PatientData patientData = new PatientFhirMapper.PatientData(
                UUID.randomUUID(),
                "199001011234",
                "Secret",
                null,
                "Person",
                "UNKNOWN",
                null,
                true, // protected identity
                false, null,
                null, null, null, null, null, null
        );

        // When
        Patient fhirPatient = mapper.toFhir(patientData);

        // Then
        assertThat(fhirPatient.getExtension()).hasSize(1);
        Extension ext = fhirPatient.getExtensionByUrl(PROTECTED_IDENTITY_URL);
        assertThat(ext).isNotNull();
        assertThat(((BooleanType) ext.getValue()).booleanValue()).isTrue();
    }

    @Test
    void shouldMapDeceasedPatient() {
        // Given
        LocalDate deceasedDate = LocalDate.of(2024, 3, 15);
        PatientFhirMapper.PatientData patientData = new PatientFhirMapper.PatientData(
                UUID.randomUUID(),
                "199001011234",
                "Test",
                null,
                "Patient",
                "MALE",
                LocalDate.of(1990, 1, 1),
                false,
                true, // deceased
                deceasedDate,
                null, null, null, null, null, null
        );

        // When
        Patient fhirPatient = mapper.toFhir(patientData);

        // Then
        assertThat(fhirPatient.getActive()).isFalse();
        assertThat(fhirPatient.getDeceased()).isInstanceOf(DateTimeType.class);
    }

    @Test
    void shouldMapTelecom() {
        // Given
        PatientFhirMapper.PatientData patientData = new PatientFhirMapper.PatientData(
                UUID.randomUUID(),
                "199001011234",
                "Test",
                null,
                "Patient",
                "MALE",
                null,
                false, false, null,
                "+46701234567",
                "test@example.se",
                null, null, null, null
        );

        // When
        Patient fhirPatient = mapper.toFhir(patientData);

        // Then
        assertThat(fhirPatient.getTelecom()).hasSize(2);
        assertThat(fhirPatient.getTelecom())
                .anyMatch(t -> t.getSystem() == ContactPoint.ContactPointSystem.PHONE
                        && t.getValue().equals("+46701234567"));
        assertThat(fhirPatient.getTelecom())
                .anyMatch(t -> t.getSystem() == ContactPoint.ContactPointSystem.EMAIL
                        && t.getValue().equals("test@example.se"));
    }

    @Test
    void shouldMapAddress() {
        // Given
        PatientFhirMapper.PatientData patientData = new PatientFhirMapper.PatientData(
                UUID.randomUUID(),
                "199001011234",
                "Test",
                null,
                "Patient",
                "MALE",
                null,
                false, false, null,
                null, null,
                "Storgatan 1",
                "Stockholm",
                "11122",
                "Stockholm kommun"
        );

        // When
        Patient fhirPatient = mapper.toFhir(patientData);

        // Then
        assertThat(fhirPatient.getAddress()).hasSize(1);
        Address address = fhirPatient.getAddressFirstRep();
        assertThat(address.getLine()).extracting(StringType::getValue)
                .containsExactly("Storgatan 1");
        assertThat(address.getCity()).isEqualTo("Stockholm");
        assertThat(address.getPostalCode()).isEqualTo("11122");
        assertThat(address.getDistrict()).isEqualTo("Stockholm kommun");
        assertThat(address.getCountry()).isEqualTo("SE");
    }

    @Test
    void shouldIncludeSwedishPatientProfile() {
        // Given
        PatientFhirMapper.PatientData patientData = createMinimalPatient("199001011234");

        // When
        Patient fhirPatient = mapper.toFhir(patientData);

        // Then
        assertThat(fhirPatient.getMeta().getProfile())
                .extracting(CanonicalType::getValue)
                .contains(SWEDISH_PATIENT_PROFILE);
    }

    @Test
    void shouldMapAllGenders() {
        assertGenderMapping("MALE", Enumerations.AdministrativeGender.MALE);
        assertGenderMapping("FEMALE", Enumerations.AdministrativeGender.FEMALE);
        assertGenderMapping("OTHER", Enumerations.AdministrativeGender.OTHER);
        assertGenderMapping("UNKNOWN", Enumerations.AdministrativeGender.UNKNOWN);
    }

    private void assertGenderMapping(String input, Enumerations.AdministrativeGender expected) {
        PatientFhirMapper.PatientData patientData = new PatientFhirMapper.PatientData(
                UUID.randomUUID(), "199001011234", "Test", null, "Patient",
                input, null, false, false, null,
                null, null, null, null, null, null
        );
        Patient fhirPatient = mapper.toFhir(patientData);
        assertThat(fhirPatient.getGender()).isEqualTo(expected);
    }

    private PatientFhirMapper.PatientData createMinimalPatient(String personnummer) {
        return new PatientFhirMapper.PatientData(
                UUID.randomUUID(),
                personnummer,
                "Test",
                null,
                "Patient",
                "UNKNOWN",
                null,
                false, false, null,
                null, null, null, null, null, null
        );
    }
}
