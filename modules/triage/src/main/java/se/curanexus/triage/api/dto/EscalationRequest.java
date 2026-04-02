package se.curanexus.triage.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import se.curanexus.triage.domain.TriagePriority;

import java.util.UUID;

public record EscalationRequest(
        @NotNull(message = "New priority is required")
        TriagePriority newPriority,

        @Size(max = 500)
        String reason,

        UUID escalatedBy
) {}
