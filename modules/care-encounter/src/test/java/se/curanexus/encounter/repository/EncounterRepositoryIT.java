package se.curanexus.encounter.repository;

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
import se.curanexus.encounter.domain.*;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EncounterRepositoryIT {

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
    private EncounterRepository encounterRepository;

    @Test
    void save_shouldPersistEncounter() {
        // Given
        UUID patientId = UUID.randomUUID();
        Encounter encounter = new Encounter(patientId, EncounterClass.OUTPATIENT);

        // When
        Encounter saved = encounterRepository.save(encounter);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(EncounterStatus.PLANNED);
    }

    @Test
    void findByPatientId_shouldReturnEncounters() {
        // Given
        UUID patientId = UUID.randomUUID();
        Encounter encounter1 = new Encounter(patientId, EncounterClass.OUTPATIENT);
        Encounter encounter2 = new Encounter(patientId, EncounterClass.EMERGENCY);
        encounterRepository.save(encounter1);
        encounterRepository.save(encounter2);

        // When
        Page<Encounter> result = encounterRepository.findByPatientId(patientId, PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void findByPatientIdAndStatus_shouldFilterByStatus() {
        // Given
        UUID patientId = UUID.randomUUID();
        Encounter planned = new Encounter(patientId, EncounterClass.OUTPATIENT);
        Encounter arrived = new Encounter(patientId, EncounterClass.OUTPATIENT);
        arrived.transitionTo(EncounterStatus.ARRIVED);

        encounterRepository.save(planned);
        encounterRepository.save(arrived);

        // When
        Page<Encounter> result = encounterRepository.findByPatientIdAndStatus(
                patientId, EncounterStatus.ARRIVED, PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(EncounterStatus.ARRIVED);
    }

    @Test
    void save_shouldPersistEncounterWithParticipants() {
        // Given
        UUID patientId = UUID.randomUUID();
        Encounter encounter = new Encounter(patientId, EncounterClass.OUTPATIENT);
        Participant participant = new Participant(ParticipantType.PRACTITIONER);
        participant.setPractitionerId(UUID.randomUUID());
        participant.setRole(ParticipantRole.PRIMARY);
        encounter.addParticipant(participant);

        // When
        Encounter saved = encounterRepository.save(encounter);

        // Then
        Encounter found = encounterRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getParticipants()).hasSize(1);
        assertThat(found.getParticipants().get(0).getRole()).isEqualTo(ParticipantRole.PRIMARY);
    }

    @Test
    void save_shouldPersistEncounterWithReasons() {
        // Given
        UUID patientId = UUID.randomUUID();
        Encounter encounter = new Encounter(patientId, EncounterClass.OUTPATIENT);
        EncounterReason reason = new EncounterReason(ReasonType.CHIEF_COMPLAINT);
        reason.setCode("J06.9");
        reason.setCodeSystem("ICD-10-SE");
        reason.setDisplayText("Upper respiratory infection");
        encounter.addReason(reason);

        // When
        Encounter saved = encounterRepository.save(encounter);

        // Then
        Encounter found = encounterRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getReasons()).hasSize(1);
        assertThat(found.getReasons().get(0).getCode()).isEqualTo("J06.9");
    }

    @Test
    void searchEncounters_shouldFindByMultipleCriteria() {
        // Given
        UUID patientId = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();

        Encounter encounter = new Encounter(patientId, EncounterClass.OUTPATIENT);
        encounter.setResponsibleUnitId(unitId);
        encounterRepository.save(encounter);

        // When
        Page<Encounter> result = encounterRepository.searchEncounters(
                patientId,
                EncounterStatus.PLANNED,
                EncounterClass.OUTPATIENT,
                unitId,
                null,
                null,
                PageRequest.of(0, 10)
        );

        // Then
        assertThat(result.getContent()).hasSize(1);
    }
}
