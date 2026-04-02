package se.curanexus.authorization.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import se.curanexus.authorization.domain.CareRelationType;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateCareRelationRequest(
        @NotNull UUID userId,
        @NotNull UUID patientId,
        UUID encounterId,
        @NotNull CareRelationType relationType,
        LocalDateTime validFrom,
        LocalDateTime validUntil,
        @Size(max = 500) String reason
) {}
