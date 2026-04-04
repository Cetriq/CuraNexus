package se.curanexus.consent.api.dto;

import se.curanexus.consent.domain.Consent;
import se.curanexus.consent.domain.ConsentStatus;
import se.curanexus.consent.domain.ConsentType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ConsentDto(
        UUID id,
        UUID patientId,
        ConsentType type,
        ConsentStatus status,
        String description,
        String scope,
        UUID managingUnitId,
        String managingUnitName,
        Instant givenAt,
        UUID givenBy,
        String givenByName,
        String representativeRelation,
        String collectionMethod,
        LocalDate validFrom,
        LocalDate validUntil,
        Instant withdrawnAt,
        String withdrawalReason,
        UUID recordedBy,
        String recordedByName,
        String documentReference,
        boolean valid,
        Instant createdAt,
        Instant updatedAt
) {
    public static ConsentDto fromEntity(Consent consent) {
        return new ConsentDto(
                consent.getId(),
                consent.getPatientId(),
                consent.getType(),
                consent.getStatus(),
                consent.getDescription(),
                consent.getScope(),
                consent.getManagingUnitId(),
                consent.getManagingUnitName(),
                consent.getGivenAt(),
                consent.getGivenBy(),
                consent.getGivenByName(),
                consent.getRepresentativeRelation(),
                consent.getCollectionMethod(),
                consent.getValidFrom(),
                consent.getValidUntil(),
                consent.getWithdrawnAt(),
                consent.getWithdrawalReason(),
                consent.getRecordedBy(),
                consent.getRecordedByName(),
                consent.getDocumentReference(),
                consent.isValid(),
                consent.getCreatedAt(),
                consent.getUpdatedAt()
        );
    }
}
