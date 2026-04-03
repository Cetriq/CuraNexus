package se.curanexus.lab.api.dto;

import se.curanexus.lab.domain.AbnormalFlag;
import se.curanexus.lab.domain.LabResult;
import se.curanexus.lab.domain.ResultStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LabResultDto(
        UUID id,
        ResultStatus status,
        BigDecimal valueNumeric,
        String unit,
        String valueText,
        BigDecimal referenceLow,
        BigDecimal referenceHigh,
        String referenceRangeText,
        AbnormalFlag abnormalFlag,
        Boolean isCritical,
        String method,
        String instrument,
        String performingDepartment,
        UUID analyzerId,
        String analyzerName,
        UUID reviewerId,
        String reviewerName,
        String labComment,
        Instant analyzedAt,
        Instant reviewedAt,
        Instant resultedAt,
        // From order item
        String testCode,
        String testName
) {
    public static LabResultDto from(LabResult r) {
        return new LabResultDto(
                r.getId(),
                r.getStatus(),
                r.getValueNumeric(),
                r.getUnit(),
                r.getValueText(),
                r.getReferenceLow(),
                r.getReferenceHigh(),
                r.getReferenceRangeText(),
                r.getAbnormalFlag(),
                r.getIsCritical(),
                r.getMethod(),
                r.getInstrument(),
                r.getPerformingDepartment(),
                r.getAnalyzerId(),
                r.getAnalyzerName(),
                r.getReviewerId(),
                r.getReviewerName(),
                r.getLabComment(),
                r.getAnalyzedAt(),
                r.getReviewedAt(),
                r.getResultedAt(),
                r.getOrderItem() != null ? r.getOrderItem().getTestCode() : null,
                r.getOrderItem() != null ? r.getOrderItem().getTestName() : null
        );
    }
}
