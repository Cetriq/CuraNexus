package se.curanexus.lab.api.dto;

import se.curanexus.lab.domain.AbnormalFlag;

import java.math.BigDecimal;

public record RegisterResultRequest(
        BigDecimal valueNumeric,
        String unit,
        String valueText,
        BigDecimal referenceLow,
        BigDecimal referenceHigh,
        String referenceRangeText,
        AbnormalFlag abnormalFlag,
        String method,
        String instrument,
        String performingDepartment,
        String labComment
) {
}
