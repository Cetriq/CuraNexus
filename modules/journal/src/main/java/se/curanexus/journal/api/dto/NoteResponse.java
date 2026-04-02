package se.curanexus.journal.api.dto;

import se.curanexus.journal.domain.ClinicalNote;
import se.curanexus.journal.domain.NoteStatus;
import se.curanexus.journal.domain.NoteType;

import java.time.Instant;
import java.util.UUID;

public record NoteResponse(
        UUID id,
        UUID encounterId,
        UUID patientId,
        NoteType type,
        String title,
        String content,
        NoteStatus status,
        UUID authorId,
        String authorName,
        UUID signedById,
        String signedByName,
        Instant signedAt,
        Instant createdAt,
        Instant updatedAt
) {
    public static NoteResponse from(ClinicalNote note) {
        return new NoteResponse(
                note.getId(),
                note.getEncounterId(),
                note.getPatientId(),
                note.getType(),
                note.getTitle(),
                note.getContent(),
                note.getStatus(),
                note.getAuthorId(),
                note.getAuthorName(),
                note.getSignedById(),
                note.getSignedByName(),
                note.getSignedAt(),
                note.getCreatedAt(),
                note.getUpdatedAt()
        );
    }
}
