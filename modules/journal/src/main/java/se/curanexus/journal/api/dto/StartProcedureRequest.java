package se.curanexus.journal.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record StartProcedureRequest(
        @NotNull UUID performedById,
        @NotBlank String performedByName
) {}
