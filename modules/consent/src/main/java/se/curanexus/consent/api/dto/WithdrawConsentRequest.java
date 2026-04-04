package se.curanexus.consent.api.dto;

import jakarta.validation.constraints.NotBlank;

public record WithdrawConsentRequest(
        @NotBlank(message = "Withdrawal reason is required")
        String reason
) {}
