package se.curanexus.journal.api.dto;

import jakarta.validation.constraints.NotBlank;

public record AmendNoteRequest(
        @NotBlank String newContent
) {}
