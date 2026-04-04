package se.curanexus.forms.api.dto;

import se.curanexus.forms.domain.FormSubmission;
import se.curanexus.forms.domain.SubmissionStatus;

import java.time.Instant;
import java.util.UUID;

public record FormSubmissionSummaryDto(
        UUID id,
        String templateCode,
        String templateName,
        UUID patientId,
        UUID encounterId,
        SubmissionStatus status,
        Instant startedAt,
        Instant completedAt,
        Double totalScore,
        String scoreInterpretation
) {
    public static FormSubmissionSummaryDto from(FormSubmission submission) {
        return new FormSubmissionSummaryDto(
                submission.getId(),
                submission.getTemplate().getCode(),
                submission.getTemplate().getName(),
                submission.getPatientId(),
                submission.getEncounterId(),
                submission.getStatus(),
                submission.getStartedAt(),
                submission.getCompletedAt(),
                submission.getTotalScore(),
                submission.getScoreInterpretation()
        );
    }
}
