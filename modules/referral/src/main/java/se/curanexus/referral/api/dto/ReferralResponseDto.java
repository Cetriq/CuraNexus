package se.curanexus.referral.api.dto;

import se.curanexus.referral.domain.ReferralPriority;
import se.curanexus.referral.domain.ReferralResponse;
import se.curanexus.referral.domain.ReferralResponseType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ReferralResponseDto(
        UUID id,
        UUID referralId,
        ReferralResponseType responseType,
        UUID responderUnitId,
        String responderUnitName,
        UUID responderId,
        String responderHsaId,
        String responderName,
        String responseText,
        ReferralPriority assessedPriority,
        LocalDate plannedDate,
        String rejectionReason,
        String requestedInformation,
        UUID forwardedToUnitId,
        String forwardedToUnitName,
        String forwardReason,
        Instant createdAt
) {
    public static ReferralResponseDto from(ReferralResponse rr) {
        return new ReferralResponseDto(
                rr.getId(),
                rr.getReferral().getId(),
                rr.getResponseType(),
                rr.getResponderUnitId(),
                rr.getResponderUnitName(),
                rr.getResponderId(),
                rr.getResponderHsaId(),
                rr.getResponderName(),
                rr.getResponseText(),
                rr.getAssessedPriority(),
                rr.getPlannedDate(),
                rr.getRejectionReason(),
                rr.getRequestedInformation(),
                rr.getForwardedToUnitId(),
                rr.getForwardedToUnitName(),
                rr.getForwardReason(),
                rr.getCreatedAt()
        );
    }
}
