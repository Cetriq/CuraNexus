package se.curanexus.journal.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import se.curanexus.journal.domain.NoteType;

import java.util.UUID;

public record CreateNoteRequest(
        @NotNull UUID encounterId,
        @NotNull UUID patientId,
        @NotNull NoteType type,
        @NotNull UUID authorId,
        @NotBlank String authorName,
        String title,
        String content
) {}
