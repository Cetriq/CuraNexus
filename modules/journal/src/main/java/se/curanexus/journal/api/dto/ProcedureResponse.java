package se.curanexus.journal.api.dto;

import se.curanexus.journal.domain.Procedure;
import se.curanexus.journal.domain.ProcedureStatus;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProcedureResponse(
        UUID id,
        UUID encounterId,
        UUID patientId,
        String code,
        String codeSystem,
        String displayText,
        ProcedureStatus status,
        LocalDateTime performedAt,
        UUID performedById,
        String performedByName,
        String bodySite,
        String laterality,
        String outcome,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {
    public static ProcedureResponse from(Procedure procedure) {
        return new ProcedureResponse(
                procedure.getId(),
                procedure.getEncounterId(),
                procedure.getPatientId(),
                procedure.getCode(),
                procedure.getCodeSystem(),
                procedure.getDisplayText(),
                procedure.getStatus(),
                procedure.getPerformedAt(),
                procedure.getPerformedById(),
                procedure.getPerformedByName(),
                procedure.getBodySite(),
                procedure.getLaterality(),
                procedure.getOutcome(),
                procedure.getNotes(),
                procedure.getCreatedAt(),
                procedure.getUpdatedAt()
        );
    }
}
