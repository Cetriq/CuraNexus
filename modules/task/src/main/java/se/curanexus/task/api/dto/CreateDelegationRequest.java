package se.curanexus.task.api.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateDelegationRequest(
        @NotNull UUID fromUserId,
        @NotNull UUID toUserId,
        @NotNull LocalDateTime validFrom,
        @NotNull LocalDateTime validUntil,
        String scope,
        String note
) {}
