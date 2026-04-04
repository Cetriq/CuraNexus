package se.curanexus.consent.api.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ActivateConsentRequest(
        @NotNull(message = "Given by is required")
        UUID givenBy,

        String givenByName,

        String collectionMethod
) {}
