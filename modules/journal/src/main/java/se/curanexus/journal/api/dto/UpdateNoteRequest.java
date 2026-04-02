package se.curanexus.journal.api.dto;

public record UpdateNoteRequest(
        String title,
        String content
) {}
