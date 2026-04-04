package se.curanexus.forms.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.curanexus.forms.api.dto.*;
import se.curanexus.forms.domain.*;
import se.curanexus.forms.repository.FormAnswerRepository;
import se.curanexus.forms.repository.FormSubmissionRepository;
import se.curanexus.forms.repository.FormTemplateRepository;

import java.time.Instant;
import java.util.*;

@Service
@Transactional
public class FormSubmissionService {

    private final FormSubmissionRepository submissionRepository;
    private final FormTemplateRepository templateRepository;
    private final FormAnswerRepository answerRepository;

    public FormSubmissionService(FormSubmissionRepository submissionRepository,
                                  FormTemplateRepository templateRepository,
                                  FormAnswerRepository answerRepository) {
        this.submissionRepository = submissionRepository;
        this.templateRepository = templateRepository;
        this.answerRepository = answerRepository;
    }

    @Transactional(readOnly = true)
    public FormSubmissionDto getSubmission(UUID id) {
        FormSubmission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new FormSubmissionNotFoundException(id));
        return FormSubmissionDto.from(submission);
    }

    @Transactional(readOnly = true)
    public List<FormSubmissionSummaryDto> getPatientSubmissions(UUID patientId, SubmissionStatus status) {
        List<FormSubmission> submissions;
        if (status != null) {
            submissions = submissionRepository.findByPatientIdAndStatus(patientId, status);
        } else {
            submissions = submissionRepository.findByPatientId(patientId);
        }
        return submissions.stream()
                .map(FormSubmissionSummaryDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FormSubmissionSummaryDto> getEncounterSubmissions(UUID encounterId) {
        return submissionRepository.findByEncounterId(encounterId).stream()
                .map(FormSubmissionSummaryDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<FormSubmissionSummaryDto> searchSubmissions(UUID patientId, UUID encounterId,
                                                             String templateCode, SubmissionStatus status,
                                                             Pageable pageable) {
        // Simple implementation - just use patient filter
        if (patientId != null) {
            return submissionRepository.findByPatientId(patientId, pageable)
                    .map(FormSubmissionSummaryDto::from);
        }
        return Page.empty(pageable);
    }

    @Transactional(readOnly = true)
    public List<FormSubmissionSummaryDto> getPendingReviewSubmissions() {
        return submissionRepository.findPendingReview(Pageable.unpaged()).stream()
                .map(FormSubmissionSummaryDto::from)
                .toList();
    }

    public FormSubmissionDto startSubmission(StartSubmissionRequest request) {
        FormTemplate template = templateRepository.findActiveByCode(request.templateCode())
                .orElseThrow(() -> new FormTemplateNotFoundException(request.templateCode()));

        FormSubmission submission = new FormSubmission(template, request.patientId());
        submission.setEncounterId(request.encounterId());
        submission.setSubmittedBy(request.submittedBy());
        submission.setSubmittedByRole(request.submittedByRole());
        submission.setSource(request.source());
        submission.setIpAddress(request.ipAddress());

        if (request.expiresInMinutes() != null) {
            submission.setExpiresAt(Instant.now().plusSeconds(request.expiresInMinutes() * 60L));
        }

        FormSubmission saved = submissionRepository.save(submission);
        return FormSubmissionDto.from(saved);
    }

    public FormSubmissionDto saveAnswers(UUID submissionId, SaveAnswersRequest request) {
        FormSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new FormSubmissionNotFoundException(submissionId));

        if (submission.getStatus() != SubmissionStatus.IN_PROGRESS) {
            throw new IllegalStateException("Can only save answers for submissions with status IN_PROGRESS");
        }

        if (submission.isExpired()) {
            submission.expire();
            submissionRepository.save(submission);
            throw new IllegalStateException("Submission has expired");
        }

        for (AnswerRequest answerRequest : request.answers()) {
            FormAnswer existingAnswer = submission.getAnswerForField(answerRequest.fieldKey());

            if (existingAnswer != null) {
                updateAnswer(existingAnswer, answerRequest);
            } else {
                FormAnswer newAnswer = createAnswer(answerRequest, submission.getTemplate());
                submission.addAnswer(newAnswer);
            }
        }

        FormSubmission saved = submissionRepository.save(submission);
        return FormSubmissionDto.from(saved);
    }

    public FormSubmissionDto completeSubmission(UUID submissionId) {
        FormSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new FormSubmissionNotFoundException(submissionId));

        if (submission.getStatus() != SubmissionStatus.IN_PROGRESS) {
            throw new IllegalStateException("Can only complete submissions with status IN_PROGRESS");
        }

        if (submission.isExpired()) {
            submission.expire();
            submissionRepository.save(submission);
            throw new IllegalStateException("Submission has expired");
        }

        // Validate required fields
        validateRequiredFields(submission);

        // Calculate score if formula exists
        Double score = calculateScoreInternal(submission);
        if (score != null) {
            submission.setTotalScore(score);
        }

        submission.complete();
        FormSubmission saved = submissionRepository.save(submission);
        return FormSubmissionDto.from(saved);
    }

    public FormSubmissionDto reviewSubmission(UUID submissionId, ReviewSubmissionRequest request) {
        FormSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new FormSubmissionNotFoundException(submissionId));

        if (submission.getStatus() != SubmissionStatus.COMPLETED) {
            throw new IllegalStateException("Can only review submissions with status COMPLETED");
        }

        submission.review(request.reviewedBy(), request.reviewNotes());

        FormSubmission saved = submissionRepository.save(submission);
        return FormSubmissionDto.from(saved);
    }

    public FormSubmissionDto cancelSubmission(UUID submissionId) {
        FormSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new FormSubmissionNotFoundException(submissionId));

        if (submission.getStatus() != SubmissionStatus.IN_PROGRESS) {
            throw new IllegalStateException("Can only cancel submissions with status IN_PROGRESS");
        }

        submission.cancel();
        FormSubmission saved = submissionRepository.save(submission);
        return FormSubmissionDto.from(saved);
    }

    public Double calculateScore(UUID submissionId) {
        FormSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new FormSubmissionNotFoundException(submissionId));

        Double score = calculateScoreInternal(submission);
        if (score != null) {
            submission.setTotalScore(score);
            submissionRepository.save(submission);
        }
        return score;
    }

    public void deleteSubmission(UUID submissionId) {
        FormSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new FormSubmissionNotFoundException(submissionId));

        if (submission.getStatus() == SubmissionStatus.COMPLETED ||
            submission.getStatus() == SubmissionStatus.REVIEWED) {
            throw new IllegalStateException("Cannot delete completed or reviewed submissions");
        }

        submissionRepository.delete(submission);
    }

    @Transactional
    public int expireOldSubmissions() {
        List<FormSubmission> expired = submissionRepository.findExpiredSubmissions(Instant.now());
        for (FormSubmission submission : expired) {
            submission.expire();
        }
        submissionRepository.saveAll(expired);
        return expired.size();
    }

    private void validateRequiredFields(FormSubmission submission) {
        FormTemplate template = submission.getTemplate();
        List<String> missingFields = new ArrayList<>();

        for (FormField field : template.getFields()) {
            if (Boolean.TRUE.equals(field.getRequired())) {
                FormAnswer answer = submission.getAnswerForField(field.getFieldKey());
                if (answer == null || isAnswerEmpty(answer)) {
                    missingFields.add(field.getFieldKey());
                }
            }
        }

        if (!missingFields.isEmpty()) {
            Map<String, List<String>> errors = new HashMap<>();
            for (String field : missingFields) {
                errors.put(field, List.of("Field is required"));
            }
            throw new FormValidationException("Required fields are missing: " + String.join(", ", missingFields), errors);
        }
    }

    private boolean isAnswerEmpty(FormAnswer answer) {
        return answer.getValueText() == null
                && answer.getValueNumber() == null
                && answer.getValueBoolean() == null
                && answer.getValueDatetime() == null
                && answer.getValueArray() == null
                && answer.getFileReference() == null;
    }

    private Double calculateScoreInternal(FormSubmission submission) {
        String formula = submission.getTemplate().getScoringFormula();
        if (formula == null || formula.isBlank()) {
            return null;
        }

        // Simple SUM-based scoring
        if (formula.toUpperCase().startsWith("SUM")) {
            double total = submission.getAnswers().stream()
                    .filter(a -> a.getValueNumber() != null)
                    .mapToDouble(FormAnswer::getValueNumber)
                    .sum();
            return total;
        }

        return null;
    }

    private FormAnswer createAnswer(AnswerRequest request, FormTemplate template) {
        // Get field type from template if possible, otherwise default to TEXT
        FieldType fieldType = template.getFields().stream()
                .filter(f -> f.getFieldKey().equals(request.fieldKey()))
                .findFirst()
                .map(FormField::getFieldType)
                .orElse(FieldType.TEXT);

        FormAnswer answer = new FormAnswer(request.fieldKey(), fieldType);
        updateAnswer(answer, request);
        return answer;
    }

    private void updateAnswer(FormAnswer answer, AnswerRequest request) {
        if (request.valueText() != null) {
            answer.setTextValue(request.valueText());
        }
        if (request.valueNumber() != null) {
            answer.setNumericValue(request.valueNumber());
        }
        if (request.valueBoolean() != null) {
            answer.setBooleanValue(request.valueBoolean());
        }
        if (request.valueDatetime() != null) {
            answer.setDatetimeValue(request.valueDatetime());
        }
        if (request.valueArray() != null) {
            answer.setArrayValue(request.valueArray());
        }
        if (request.fileReference() != null) {
            answer.setFileReference(request.fileReference());
        }
        if (request.codeSystem() != null) {
            answer.setCodedValue(request.codeSystem(), request.code(), request.codeDisplay());
        }
    }
}
