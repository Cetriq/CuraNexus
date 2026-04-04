package se.curanexus.forms.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ReviewSubmissionRequest(
        @NotNull UUID reviewedBy,
        @Size(max = 50) String reviewedByRole,
        @Size(max = 2000) String reviewNotes,
        Boolean approved
) {
}
