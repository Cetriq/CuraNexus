package se.curanexus.consent.api.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CheckAccessRequest(
        @NotNull(message = "Patient ID is required")
        UUID patientId,

        UUID unitId,

        UUID practitionerId,

        String dataCategory
) {}
