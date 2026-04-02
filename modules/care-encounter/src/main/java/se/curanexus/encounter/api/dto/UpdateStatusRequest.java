package se.curanexus.encounter.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import se.curanexus.encounter.domain.EncounterStatus;

public record UpdateStatusRequest(
        @NotNull(message = "Status is required")
        EncounterStatus status,

        @Size(max = 500, message = "Reason must not exceed 500 characters")
        String reason
) {
}
