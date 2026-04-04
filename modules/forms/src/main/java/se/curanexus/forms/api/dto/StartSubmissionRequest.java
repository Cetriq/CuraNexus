package se.curanexus.forms.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record StartSubmissionRequest(
        @NotBlank String templateCode,
        @NotNull UUID patientId,
        UUID encounterId,
        UUID submittedBy,
        String submittedByRole,
        Integer expiresInMinutes,
        String source,
        String ipAddress
) {
}
