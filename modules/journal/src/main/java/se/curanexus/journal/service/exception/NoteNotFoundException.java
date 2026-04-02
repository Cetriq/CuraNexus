package se.curanexus.journal.service.exception;

import java.util.UUID;

public class NoteNotFoundException extends RuntimeException {

    private final UUID noteId;

    public NoteNotFoundException(UUID noteId) {
        super("Clinical note not found: " + noteId);
        this.noteId = noteId;
    }

    public UUID getNoteId() {
        return noteId;
    }
}
