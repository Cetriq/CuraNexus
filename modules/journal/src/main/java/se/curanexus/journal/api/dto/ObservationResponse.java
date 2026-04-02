package se.curanexus.journal.api.dto;

import se.curanexus.journal.domain.Observation;
import se.curanexus.journal.domain.ObservationCategory;
import se.curanexus.journal.domain.ObservationInterpretation;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record ObservationResponse(
        UUID id,
        UUID encounterId,
        UUID patientId,
        String code,
        String codeSystem,
        String displayText,
        ObservationCategory category,
        BigDecimal valueNumeric,
        String valueString,
        Boolean valueBoolean,
        String unit,
        BigDecimal referenceRangeLow,
        BigDecimal referenceRangeHigh,
        ObservationInterpretation interpretation,
        LocalDateTime observedAt,
        Instant recordedAt,
        UUID recordedById,
        String recordedByName,
        String method,
        String bodySite,
        String device,
        String notes,
        boolean withinReferenceRange
) {
    public static ObservationResponse from(Observation observation) {
        return new ObservationResponse(
                observation.getId(),
                observation.getEncounterId(),
                observation.getPatientId(),
                observation.getCode(),
                observation.getCodeSystem(),
                observation.getDisplayText(),
                observation.getCategory(),
                observation.getValueNumeric(),
                observation.getValueString(),
                observation.getValueBoolean(),
                observation.getUnit(),
                observation.getReferenceRangeLow(),
                observation.getReferenceRangeHigh(),
                observation.getInterpretation(),
                observation.getObservedAt(),
                observation.getRecordedAt(),
                observation.getRecordedById(),
                observation.getRecordedByName(),
                observation.getMethod(),
                observation.getBodySite(),
                observation.getDevice(),
                observation.getNotes(),
                observation.isWithinReferenceRange()
        );
    }
}
