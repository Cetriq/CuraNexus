package se.curanexus.referral.api.dto;

import jakarta.validation.constraints.NotNull;
import se.curanexus.referral.domain.ReferralPriority;

import java.time.LocalDate;

public record AssessReferralRequest(
        @NotNull AssessmentDecision decision,
        ReferralPriority assessedPriority,
        LocalDate plannedDate,
        String rejectionReason,
        String requestedInformation,
        String responseText
) {
    public enum AssessmentDecision {
        ACCEPT,
        REJECT,
        REQUEST_INFORMATION
    }
}
