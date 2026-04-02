package se.curanexus.triage.api.dto;

import se.curanexus.triage.domain.EscalationRecord;
import se.curanexus.triage.domain.TriagePriority;

import java.time.Instant;
import java.util.UUID;

public record EscalationRecordResponse(
        TriagePriority previousPriority,
        TriagePriority newPriority,
        String reason,
        UUID escalatedBy,
        Instant escalatedAt
) {
    public static EscalationRecordResponse fromEntity(EscalationRecord record) {
        return new EscalationRecordResponse(
                record.getPreviousPriority(),
                record.getNewPriority(),
                record.getReason(),
                record.getEscalatedBy(),
                record.getEscalatedAt()
        );
    }
}
