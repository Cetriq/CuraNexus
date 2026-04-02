package se.curanexus.triage.api.dto;

import se.curanexus.triage.domain.ProtocolStep;
import se.curanexus.triage.domain.TriageProtocol;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TriageProtocolResponse(
        UUID id,
        String code,
        String name,
        String description,
        String category,
        String version,
        boolean active,
        List<ProtocolStepResponse> steps,
        List<String> redFlags,
        Instant updatedAt
) {
    public record ProtocolStepResponse(
            int order,
            String instruction,
            String assessmentCriteria,
            List<String> actions
    ) {
        public static ProtocolStepResponse fromEntity(ProtocolStep step) {
            return new ProtocolStepResponse(
                    step.getStepOrder(),
                    step.getInstruction(),
                    step.getAssessmentCriteria(),
                    step.getActions()
            );
        }
    }

    public static TriageProtocolResponse fromEntity(TriageProtocol protocol) {
        return new TriageProtocolResponse(
                protocol.getId(),
                protocol.getCode(),
                protocol.getName(),
                protocol.getDescription(),
                protocol.getCategory(),
                protocol.getVersion(),
                protocol.isActive(),
                protocol.getSteps().stream().map(ProtocolStepResponse::fromEntity).toList(),
                protocol.getRedFlags(),
                protocol.getUpdatedAt()
        );
    }
}
