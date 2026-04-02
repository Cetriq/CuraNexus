package se.curanexus.authorization.api.dto;

import se.curanexus.authorization.domain.CareRelation;
import se.curanexus.authorization.domain.CareRelationType;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record CareRelationResponse(
        UUID id,
        UUID userId,
        UUID patientId,
        UUID encounterId,
        CareRelationType relationType,
        LocalDateTime validFrom,
        LocalDateTime validUntil,
        String reason,
        boolean active,
        boolean currentlyActive,
        Instant createdAt,
        Instant endedAt,
        UUID endedById
) {
    public static CareRelationResponse from(CareRelation relation) {
        return new CareRelationResponse(
                relation.getId(),
                relation.getUserId(),
                relation.getPatientId(),
                relation.getEncounterId(),
                relation.getRelationType(),
                relation.getValidFrom(),
                relation.getValidUntil(),
                relation.getReason(),
                relation.isActive(),
                relation.isCurrentlyActive(),
                relation.getCreatedAt(),
                relation.getEndedAt(),
                relation.getEndedById()
        );
    }
}
