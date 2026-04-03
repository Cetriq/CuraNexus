package se.curanexus.medication.api.dto;

import jakarta.validation.constraints.NotNull;
import se.curanexus.medication.domain.RouteOfAdministration;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record RecordAdministrationRequest(
        @NotNull UUID prescriptionId,
        UUID encounterId,
        LocalDateTime administeredAt,
        @NotNull BigDecimal doseQuantity,
        @NotNull String doseUnit,
        RouteOfAdministration route,
        String bodySite,
        String method,
        BigDecimal rateQuantity,
        String rateUnit,
        String performerHsaId,
        String performerName,
        String notes,
        String lotNumber
) {
}
