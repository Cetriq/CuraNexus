package se.curanexus.journal.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import se.curanexus.journal.domain.ObservationCategory;
import se.curanexus.journal.domain.ObservationInterpretation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreateObservationRequest(
        @NotNull UUID patientId,
        UUID encounterId,
        @NotBlank String code,
        String codeSystem,
        String displayText,
        @NotNull ObservationCategory category,
        BigDecimal valueNumeric,
        String valueString,
        Boolean valueBoolean,
        String unit,
        BigDecimal referenceRangeLow,
        BigDecimal referenceRangeHigh,
        ObservationInterpretation interpretation,
        @NotNull LocalDateTime observedAt,
        UUID recordedById,
        String recordedByName,
        String method,
        String bodySite,
        String device,
        String notes
) {}
