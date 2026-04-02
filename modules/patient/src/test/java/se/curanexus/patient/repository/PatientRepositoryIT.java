package se.curanexus.patient.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import se.curanexus.patient.domain.*;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PatientRepositoryIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("curanexus_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private PatientRepository patientRepository;

    @Test
    void save_shouldPersistPatient() {
        // Given
        Patient patient = new Patient("199001011234", "Anna", "Andersson");

        // When
        Patient saved = patientRepository.save(patient);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void findByPersonalIdentityNumber_shouldFindPatient() {
        // Given
        Patient patient = new Patient("199002021234", "Erik", "Eriksson");
        patientRepository.save(patient);

        // When
        Optional<Patient> found = patientRepository.findByPersonalIdentityNumber("199002021234");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getGivenName()).isEqualTo("Erik");
    }

    @Test
    void existsByPersonalIdentityNumber_shouldReturnTrue() {
        // Given
        Patient patient = new Patient("199003031234", "Maria", "Svensson");
        patientRepository.save(patient);

        // When
        boolean exists = patientRepository.existsByPersonalIdentityNumber("199003031234");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void searchPatients_shouldFindByName() {
        // Given
        Patient patient1 = new Patient("199004041234", "Lisa", "Johansson");
        Patient patient2 = new Patient("199005051234", "Lisa", "Karlsson");
        patientRepository.save(patient1);
        patientRepository.save(patient2);

        // When
        Page<Patient> result = patientRepository.searchPatients(null, "Lisa", PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void searchPatients_shouldFindByPersonnummer() {
        // Given
        Patient patient = new Patient("199006061234", "Karin", "Berg");
        patientRepository.save(patient);

        // When
        Page<Patient> result = patientRepository.searchPatients("199006061234", null, PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getGivenName()).isEqualTo("Karin");
    }

    @Test
    void save_shouldPersistPatientWithContacts() {
        // Given
        Patient patient = new Patient("199007071234", "Johan", "Holm");
        ContactInfo contact = new ContactInfo(ContactType.EMAIL, "johan@example.com");
        patient.addContact(contact);

        // When
        Patient saved = patientRepository.save(patient);

        // Then
        Patient found = patientRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getContacts()).hasSize(1);
        assertThat(found.getContacts().get(0).getValue()).isEqualTo("johan@example.com");
    }

    @Test
    void save_shouldPersistPatientWithRelatedPersons() {
        // Given
        Patient patient = new Patient("199008081234", "Sara", "Lindgren");
        RelatedPerson relatedPerson = new RelatedPerson(RelationshipType.SPOUSE, "Peter", "Lindgren");
        patient.addRelatedPerson(relatedPerson);

        // When
        Patient saved = patientRepository.save(patient);

        // Then
        Patient found = patientRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getRelatedPersons()).hasSize(1);
        assertThat(found.getRelatedPersons().get(0).getGivenName()).isEqualTo("Peter");
    }

    @Test
    void save_shouldPersistPatientWithConsents() {
        // Given
        Patient patient = new Patient("199009091234", "Mikael", "Olsson");
        Consent consent = new Consent(ConsentType.DATA_SHARING);
        patient.addConsent(consent);

        // When
        Patient saved = patientRepository.save(patient);

        // Then
        Patient found = patientRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getConsents()).hasSize(1);
        assertThat(found.getConsents().get(0).getType()).isEqualTo(ConsentType.DATA_SHARING);
    }
}
