package se.curanexus.journal.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ClinicalNoteTest {

    @Test
    void shouldCreateNoteWithDraftStatus() {
        UUID encounterId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();

        ClinicalNote note = new ClinicalNote(encounterId, patientId, NoteType.PROGRESS, authorId, "Dr. Smith");

        assertEquals(encounterId, note.getEncounterId());
        assertEquals(patientId, note.getPatientId());
        assertEquals(NoteType.PROGRESS, note.getType());
        assertEquals(authorId, note.getAuthorId());
        assertEquals("Dr. Smith", note.getAuthorName());
        assertEquals(NoteStatus.DRAFT, note.getStatus());
        assertNotNull(note.getCreatedAt());
        assertTrue(note.canEdit());
        assertTrue(note.canSign());
    }

    @Test
    void shouldSignDraftNote() {
        ClinicalNote note = createTestNote();
        UUID signedById = UUID.randomUUID();

        note.sign(signedById, "Dr. Johnson");

        assertEquals(NoteStatus.FINAL, note.getStatus());
        assertEquals(signedById, note.getSignedById());
        assertEquals("Dr. Johnson", note.getSignedByName());
        assertNotNull(note.getSignedAt());
        assertFalse(note.canEdit());
        assertFalse(note.canSign());
    }

    @Test
    void shouldNotSignFinalNote() {
        ClinicalNote note = createTestNote();
        note.sign(UUID.randomUUID(), "Dr. Johnson");

        assertThrows(IllegalStateException.class, () ->
                note.sign(UUID.randomUUID(), "Dr. Smith"));
    }

    @Test
    void shouldAmendFinalNote() {
        ClinicalNote note = createTestNote();
        note.setContent("Original content");
        note.sign(UUID.randomUUID(), "Dr. Johnson");

        note.amend("Amended content");

        assertEquals(NoteStatus.AMENDED, note.getStatus());
        assertEquals("Amended content", note.getContent());
        assertNotNull(note.getUpdatedAt());
    }

    @Test
    void shouldNotAmendDraftNote() {
        ClinicalNote note = createTestNote();

        assertThrows(IllegalStateException.class, () ->
                note.amend("New content"));
    }

    @Test
    void shouldCancelDraftNote() {
        ClinicalNote note = createTestNote();

        note.cancel();

        assertEquals(NoteStatus.CANCELLED, note.getStatus());
        assertFalse(note.canEdit());
        assertFalse(note.canSign());
    }

    @Test
    void shouldCancelFinalNote() {
        ClinicalNote note = createTestNote();
        note.sign(UUID.randomUUID(), "Dr. Johnson");

        note.cancel();

        assertEquals(NoteStatus.CANCELLED, note.getStatus());
    }

    @Test
    void shouldNotCancelAlreadyCancelledNote() {
        ClinicalNote note = createTestNote();
        note.cancel();

        assertThrows(IllegalStateException.class, note::cancel);
    }

    @Test
    void shouldNotEditFinalNote() {
        ClinicalNote note = createTestNote();
        note.sign(UUID.randomUUID(), "Dr. Johnson");

        assertFalse(note.canEdit());
    }

    private ClinicalNote createTestNote() {
        return new ClinicalNote(
                UUID.randomUUID(),
                UUID.randomUUID(),
                NoteType.PROGRESS,
                UUID.randomUUID(),
                "Dr. Test"
        );
    }
}
