package se.curanexus.journal.service.exception;

import se.curanexus.journal.domain.NoteStatus;

import java.util.UUID;

public class InvalidNoteStateException extends RuntimeException {

    private final UUID noteId;
    private final NoteStatus currentStatus;
    private final String attemptedAction;

    public InvalidNoteStateException(UUID noteId, NoteStatus currentStatus, String attemptedAction) {
        super(String.format("Cannot %s note %s in status %s", attemptedAction, noteId, currentStatus));
        this.noteId = noteId;
        this.currentStatus = currentStatus;
        this.attemptedAction = attemptedAction;
    }

    public UUID getNoteId() {
        return noteId;
    }

    public NoteStatus getCurrentStatus() {
        return currentStatus;
    }

    public String getAttemptedAction() {
        return attemptedAction;
    }
}
