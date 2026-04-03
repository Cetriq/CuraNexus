package se.curanexus.medication.api.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import se.curanexus.medication.domain.RouteOfAdministration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreatePrescriptionRequest(
        @NotNull(message = "Patient-ID är obligatoriskt")
        UUID patientId,
        UUID encounterId,
        UUID medicationId,
        String medicationText,
        String atcCode,
        String indication,
        RouteOfAdministration route,
        @NotBlank(message = "Doseringsanvisning är obligatorisk")
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
        Integer dispenseQuantity,
        Integer numberOfRepeats,
        boolean substitutionNotAllowed,
        String substitutionReason,
        String prescriberHsaId,
        String prescriberName,
        String prescriberCode,
        UUID unitId,
        String unitHsaId,
        String pharmacyNote,
        String internalNote,
        boolean activateImmediately
) {
    /**
     * Antingen medicationId eller medicationText måste anges.
     */
    @AssertTrue(message = "Antingen medicationId eller medicationText måste anges")
    public boolean isMedicationSpecified() {
        return medicationId != null || (medicationText != null && !medicationText.isBlank());
    }
}
