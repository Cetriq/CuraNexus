package se.curanexus.medication.api.dto;

import se.curanexus.medication.domain.Prescription;
import se.curanexus.medication.domain.PrescriptionStatus;
import se.curanexus.medication.domain.RouteOfAdministration;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PrescriptionDto(
        UUID id,
        UUID patientId,
        UUID encounterId,
        UUID medicationId,
        String medicationName,
        String medicationText,
        String atcCode,
        PrescriptionStatus status,
        String indication,
        RouteOfAdministration route,
        String dosageInstruction,
        BigDecimal doseQuantity,
        String doseUnit,
        Integer frequency,
        Integer frequencyPeriodHours,
        boolean asNeeded,
        BigDecimal maxDosePerDay,
        LocalDate startDate,
        LocalDate endDate,
        Integer durationDays,
        LocalDate effectiveEndDate,
        Integer dispenseQuantity,
        Integer numberOfRepeats,
        boolean substitutionNotAllowed,
        String substitutionReason,
        UUID prescriberId,
        String prescriberHsaId,
        String prescriberName,
        String prescriberCode,
        UUID unitId,
        String unitHsaId,
        String pharmacyNote,
        Instant createdAt,
        Instant activatedAt,
        Instant discontinuedAt,
        String discontinuationReason
) {
    public static PrescriptionDto from(Prescription p) {
        return new PrescriptionDto(
                p.getId(),
                p.getPatientId(),
                p.getEncounterId(),
                p.getMedication() != null ? p.getMedication().getId() : null,
                p.getMedicationName(),
                p.getMedicationText(),
                p.getAtcCode(),
                p.getStatus(),
                p.getIndication(),
                p.getRoute(),
                p.getDosageInstruction(),
                p.getDoseQuantity(),
                p.getDoseUnit(),
                p.getFrequency(),
                p.getFrequencyPeriodHours(),
                p.isAsNeeded(),
                p.getMaxDosePerDay(),
                p.getStartDate(),
                p.getEndDate(),
                p.getDurationDays(),
                p.getEffectiveEndDate(),
                p.getDispenseQuantity(),
                p.getNumberOfRepeats(),
                p.isSubstitutionNotAllowed(),
                p.getSubstitutionReason(),
                p.getPrescriberId(),
                p.getPrescriberHsaId(),
                p.getPrescriberName(),
                p.getPrescriberCode(),
                p.getUnitId(),
                p.getUnitHsaId(),
                p.getPharmacyNote(),
                p.getCreatedAt(),
                p.getActivatedAt(),
                p.getDiscontinuedAt(),
                p.getDiscontinuationReason()
        );
    }
}
