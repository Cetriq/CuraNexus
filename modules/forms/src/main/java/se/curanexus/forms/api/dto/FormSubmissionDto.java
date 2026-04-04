package se.curanexus.forms.api.dto;

import se.curanexus.forms.domain.FormSubmission;
import se.curanexus.forms.domain.SubmissionStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record FormSubmissionDto(
        UUID id,
        UUID templateId,
        String templateCode,
        String templateName,
        Integer templateVersion,
        UUID patientId,
        UUID encounterId,
        SubmissionStatus status,
        UUID submittedBy,
        String submittedByRole,
        Instant startedAt,
        Instant completedAt,
        Instant reviewedAt,
        UUID reviewedBy,
        String reviewerNotes,
        Double totalScore,
        String scoreInterpretation,
        Instant expiresAt,
        String source,
        List<FormAnswerDto> answers
) {
    public static FormSubmissionDto from(FormSubmission submission) {
        return new FormSubmissionDto(
                submission.getId(),
                submission.getTemplate().getId(),
                submission.getTemplate().getCode(),
                submission.getTemplate().getName(),
                submission.getTemplate().getVersion(),
                submission.getPatientId(),
                submission.getEncounterId(),
                submission.getStatus(),
                submission.getSubmittedBy(),
                submission.getSubmittedByRole(),
                submission.getStartedAt(),
                submission.getCompletedAt(),
                submission.getReviewedAt(),
                submission.getReviewedBy(),
                submission.getReviewerNotes(),
                submission.getTotalScore(),
                submission.getScoreInterpretation(),
                submission.getExpiresAt(),
                submission.getSource(),
                submission.getAnswers().stream().map(FormAnswerDto::from).toList()
        );
    }
}
