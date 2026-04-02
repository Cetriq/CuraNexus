package se.curanexus.task.api.dto;

import se.curanexus.task.domain.Delegation;
import se.curanexus.task.domain.DelegationStatus;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record DelegationResponse(
        UUID id,
        UUID fromUserId,
        UUID toUserId,
        DelegationStatus status,
        LocalDateTime validFrom,
        LocalDateTime validUntil,
        String scope,
        String note,
        Instant revokedAt,
        UUID revokedById,
        Instant createdAt
) {
    public static DelegationResponse from(Delegation delegation) {
        return new DelegationResponse(
                delegation.getId(),
                delegation.getFromUserId(),
                delegation.getToUserId(),
                delegation.getStatus(),
                delegation.getValidFrom(),
                delegation.getValidUntil(),
                delegation.getScope(),
                delegation.getNote(),
                delegation.getRevokedAt(),
                delegation.getRevokedById(),
                delegation.getCreatedAt()
        );
    }
}
