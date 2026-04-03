package se.curanexus.events.journal;

import se.curanexus.events.DomainEvent;

import java.util.UUID;

/**
 * Event published when a clinical note is created.
 */
public class NoteCreatedEvent extends DomainEvent {

    private final UUID noteId;
    private final UUID encounterId;
    private final UUID patientId;
    private final String noteType;
    private final UUID authorId;
    private final String authorName;
    private final String title;

    public NoteCreatedEvent(
            Object source,
            UUID noteId,
            UUID encounterId,
            UUID patientId,
            String noteType,
            UUID authorId,
            String authorName,
            String title) {
        super(source);
        this.noteId = noteId;
        this.encounterId = encounterId;
        this.patientId = patientId;
        this.noteType = noteType;
        this.authorId = authorId;
        this.authorName = authorName;
        this.title = title;
    }

    @Override
    public UUID getAggregateId() {
        return noteId;
    }

    @Override
    public String getAggregateType() {
        return "NOTE";
    }

    @Override
    public String getEventType() {
        return "CREATED";
    }

    public UUID getNoteId() {
        return noteId;
    }

    public UUID getEncounterId() {
        return encounterId;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public String getNoteType() {
        return noteType;
    }

    public UUID getAuthorId() {
        return authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getTitle() {
        return title;
    }
}
