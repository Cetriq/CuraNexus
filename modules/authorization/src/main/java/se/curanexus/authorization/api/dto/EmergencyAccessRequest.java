package se.curanexus.authorization.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request for emergency access (nödåtkomst).
 * Emergency access allows bypassing care relation requirements but requires
 * an explicit reason and is specially logged.
 */
public record EmergencyAccessRequest(
        @NotNull UUID userId,
        @NotNull UUID patientId,
        @NotBlank String reason
) {}
