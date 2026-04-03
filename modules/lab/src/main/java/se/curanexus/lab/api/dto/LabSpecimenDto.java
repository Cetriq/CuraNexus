package se.curanexus.lab.api.dto;

import se.curanexus.lab.domain.LabSpecimen;
import se.curanexus.lab.domain.SpecimenType;

import java.time.Instant;
import java.util.UUID;

public record LabSpecimenDto(
        UUID id,
        String barcode,
        SpecimenType specimenType,
        String collectionMethod,
        String bodySite,
        String quantity,
        String containerType,
        UUID collectorId,
        String collectorName,
        Instant collectedAt,
        Instant receivedAtLab,
        String qualityStatus,
        String specimenComment,
        Boolean rejected,
        String rejectionReason,
        Instant createdAt
) {
    public static LabSpecimenDto from(LabSpecimen s) {
        return new LabSpecimenDto(
                s.getId(),
                s.getBarcode(),
                s.getSpecimenType(),
                s.getCollectionMethod(),
                s.getBodySite(),
                s.getQuantity(),
                s.getContainerType(),
                s.getCollectorId(),
                s.getCollectorName(),
                s.getCollectedAt(),
                s.getReceivedAtLab(),
                s.getQualityStatus(),
                s.getSpecimenComment(),
                s.getRejected(),
                s.getRejectionReason(),
                s.getCreatedAt()
        );
    }
}
