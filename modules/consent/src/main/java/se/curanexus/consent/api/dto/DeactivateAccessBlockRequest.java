package se.curanexus.consent.api.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record DeactivateAccessBlockRequest(
        @NotNull(message = "Deactivated by is required")
        UUID deactivatedBy,

        String reason
) {}
