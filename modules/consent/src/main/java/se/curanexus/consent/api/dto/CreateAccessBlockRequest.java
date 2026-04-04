package se.curanexus.consent.api.dto;

import jakarta.validation.constraints.NotNull;
import se.curanexus.consent.domain.AccessBlockType;

import java.time.LocalDate;
import java.util.UUID;

public record CreateAccessBlockRequest(
        @NotNull(message = "Patient ID is required")
        UUID patientId,

        @NotNull(message = "Block type is required")
        AccessBlockType blockType,

        UUID blockedUnitId,

        String blockedUnitName,

        UUID blockedPractitionerId,

        String blockedPractitionerName,

        String blockedDataCategory,

        String reason,

        LocalDate validFrom,

        LocalDate validUntil,

        UUID requestedBy,

        String requestedByName
) {}
