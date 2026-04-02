package se.curanexus.journal.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import se.curanexus.journal.domain.ClinicalNote;
import se.curanexus.journal.domain.NoteStatus;
import se.curanexus.journal.domain.NoteType;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ClinicalNoteRepositoryIT {

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
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired
    private ClinicalNoteRepository repository;

    @Test
    void shouldSaveAndFindNote() {
        UUID encounterId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();

        ClinicalNote note = new ClinicalNote(encounterId, patientId, NoteType.PROGRESS, authorId, "Dr. Smith");
        note.setTitle("Progress Note");
        note.setContent("Patient is improving");

        ClinicalNote saved = repository.save(note);

        assertNotNull(saved.getId());
        assertEquals(NoteType.PROGRESS, saved.getType());
        assertEquals(NoteStatus.DRAFT, saved.getStatus());
    }

    @Test
    void shouldFindByEncounterId() {
        UUID encounterId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();

        repository.save(new ClinicalNote(encounterId, patientId, NoteType.PROGRESS, authorId, "Dr. Smith"));
        repository.save(new ClinicalNote(encounterId, patientId, NoteType.NURSING, authorId, "Nurse Jane"));
        repository.save(new ClinicalNote(UUID.randomUUID(), patientId, NoteType.DISCHARGE, authorId, "Dr. Jones"));

        List<ClinicalNote> notes = repository.findByEncounterIdOrderByCreatedAtDesc(encounterId);

        assertEquals(2, notes.size());
    }

    @Test
    void shouldFindByPatientId() {
        UUID encounterId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();

        repository.save(new ClinicalNote(encounterId, patientId, NoteType.PROGRESS, authorId, "Dr. Smith"));
        repository.save(new ClinicalNote(UUID.randomUUID(), patientId, NoteType.DISCHARGE, authorId, "Dr. Jones"));
        repository.save(new ClinicalNote(UUID.randomUUID(), UUID.randomUUID(), NoteType.ADMISSION, authorId, "Dr. Brown"));

        List<ClinicalNote> notes = repository.findByPatientIdOrderByCreatedAtDesc(patientId);

        assertEquals(2, notes.size());
    }

    @Test
    void shouldFindDraftsByEncounter() {
        UUID encounterId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();

        ClinicalNote draft = new ClinicalNote(encounterId, patientId, NoteType.PROGRESS, authorId, "Dr. Smith");
        repository.save(draft);

        ClinicalNote signed = new ClinicalNote(encounterId, patientId, NoteType.NURSING, authorId, "Nurse Jane");
        signed.sign(UUID.randomUUID(), "Dr. Jones");
        repository.save(signed);

        List<ClinicalNote> drafts = repository.findDraftsByEncounter(encounterId);

        assertEquals(1, drafts.size());
        assertEquals(NoteStatus.DRAFT, drafts.get(0).getStatus());
    }

    @Test
    void shouldCountDraftsByEncounter() {
        UUID encounterId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();

        repository.save(new ClinicalNote(encounterId, patientId, NoteType.PROGRESS, authorId, "Dr. Smith"));
        repository.save(new ClinicalNote(encounterId, patientId, NoteType.NURSING, authorId, "Nurse Jane"));

        long count = repository.countDraftsByEncounter(encounterId);

        assertEquals(2, count);
    }
}
