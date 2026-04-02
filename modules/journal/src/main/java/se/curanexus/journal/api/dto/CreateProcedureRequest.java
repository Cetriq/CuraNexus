package se.curanexus.journal.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateProcedureRequest(
        @NotNull UUID encounterId,
        @NotNull UUID patientId,
        @NotBlank String code,
        String codeSystem,
        String displayText,
        String bodySite,
        String laterality,
        String notes
) {}
