package se.curanexus.events.journal;

import se.curanexus.events.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Event published when a clinical note is signed.
 */
public class NoteSignedEvent extends DomainEvent {

    private final UUID noteId;
    private final UUID encounterId;
    private final UUID patientId;
    private final UUID signedById;
    private final String signedByName;
    private final Instant signedAt;

    public NoteSignedEvent(
            Object source,
            UUID noteId,
            UUID encounterId,
            UUID patientId,
            UUID signedById,
            String signedByName,
            Instant signedAt) {
        super(source);
        this.noteId = noteId;
        this.encounterId = encounterId;
        this.patientId = patientId;
        this.signedById = signedById;
        this.signedByName = signedByName;
        this.signedAt = signedAt;
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
        return "SIGNED";
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

    public UUID getSignedById() {
        return signedById;
    }

    public String getSignedByName() {
        return signedByName;
    }

    public Instant getSignedAt() {
        return signedAt;
    }
}
