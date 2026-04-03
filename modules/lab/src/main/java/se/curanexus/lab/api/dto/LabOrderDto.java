package se.curanexus.lab.api.dto;

import se.curanexus.lab.domain.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record LabOrderDto(
        UUID id,
        String orderReference,
        UUID patientId,
        String patientPersonnummer,
        String patientName,
        LabOrderStatus status,
        LabOrderPriority priority,
        UUID orderingUnitId,
        String orderingUnitHsaId,
        String orderingUnitName,
        UUID orderingPractitionerId,
        String orderingPractitionerHsaId,
        String orderingPractitionerName,
        UUID performingLabId,
        String performingLabHsaId,
        String performingLabName,
        String clinicalIndication,
        String diagnosisCode,
        String diagnosisText,
        String relevantMedication,
        Boolean fastingRequired,
        String labComment,
        UUID encounterId,
        UUID referralId,
        Instant createdAt,
        Instant orderedAt,
        Instant receivedAt,
        Instant specimenCollectedAt,
        Instant completedAt,
        List<LabOrderItemDto> orderItems,
        List<LabSpecimenDto> specimens,
        boolean hasCriticalResults
) {
    public static LabOrderDto from(LabOrder o) {
        return new LabOrderDto(
                o.getId(),
                o.getOrderReference(),
                o.getPatientId(),
                o.getPatientPersonnummer(),
                o.getPatientName(),
                o.getStatus(),
                o.getPriority(),
                o.getOrderingUnitId(),
                o.getOrderingUnitHsaId(),
                o.getOrderingUnitName(),
                o.getOrderingPractitionerId(),
                o.getOrderingPractitionerHsaId(),
                o.getOrderingPractitionerName(),
                o.getPerformingLabId(),
                o.getPerformingLabHsaId(),
                o.getPerformingLabName(),
                o.getClinicalIndication(),
                o.getDiagnosisCode(),
                o.getDiagnosisText(),
                o.getRelevantMedication(),
                o.getFastingRequired(),
                o.getLabComment(),
                o.getEncounterId(),
                o.getReferralId(),
                o.getCreatedAt(),
                o.getOrderedAt(),
                o.getReceivedAt(),
                o.getSpecimenCollectedAt(),
                o.getCompletedAt(),
                o.getOrderItems().stream().map(LabOrderItemDto::from).toList(),
                o.getSpecimens().stream().map(LabSpecimenDto::from).toList(),
                o.hasCriticalResults()
        );
    }
}
