package se.curanexus.medication.api.dto;

import se.curanexus.medication.domain.AdministrationStatus;
import se.curanexus.medication.domain.MedicationAdministration;
import se.curanexus.medication.domain.RouteOfAdministration;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record MedicationAdministrationDto(
        UUID id,
        UUID patientId,
        UUID prescriptionId,
        UUID encounterId,
        AdministrationStatus status,
        LocalDateTime scheduledAt,
        LocalDateTime administeredAt,
        BigDecimal doseQuantity,
        String doseUnit,
        RouteOfAdministration route,
        String bodySite,
        String method,
        BigDecimal rateQuantity,
        String rateUnit,
        UUID performerId,
        String performerHsaId,
        String performerName,
        String notGivenReason,
        String notes,
        String lotNumber,
        Instant createdAt
) {
    public static MedicationAdministrationDto from(MedicationAdministration ma) {
        return new MedicationAdministrationDto(
                ma.getId(),
                ma.getPatientId(),
                ma.getPrescription() != null ? ma.getPrescription().getId() : null,
                ma.getEncounterId(),
                ma.getStatus(),
                ma.getScheduledAt(),
                ma.getAdministeredAt(),
                ma.getDoseQuantity(),
                ma.getDoseUnit(),
                ma.getRoute(),
                ma.getBodySite(),
                ma.getMethod(),
                ma.getRateQuantity(),
                ma.getRateUnit(),
                ma.getPerformerId(),
                ma.getPerformerHsaId(),
                ma.getPerformerName(),
                ma.getNotGivenReason(),
                ma.getNotes(),
                ma.getLotNumber(),
                ma.getCreatedAt()
        );
    }
}
