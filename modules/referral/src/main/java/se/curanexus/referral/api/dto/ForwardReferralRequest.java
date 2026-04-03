package se.curanexus.referral.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ForwardReferralRequest(
        @NotNull UUID targetUnitId,
        String targetUnitHsaId,
        String targetUnitName,
        @NotBlank String forwardReason
) {
}
