package se.curanexus.journal.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SignNoteRequest(
        @NotNull UUID signedById,
        @NotBlank String signedByName
) {}
