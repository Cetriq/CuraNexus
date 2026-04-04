package se.curanexus.forms.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.curanexus.forms.api.dto.*;
import se.curanexus.forms.domain.*;
import se.curanexus.forms.repository.FormAnswerRepository;
import se.curanexus.forms.repository.FormSubmissionRepository;
import se.curanexus.forms.repository.FormTemplateRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FormSubmissionServiceTest {

    @Mock
    private FormSubmissionRepository submissionRepository;

    @Mock
    private FormTemplateRepository templateRepository;

    @Mock
    private FormAnswerRepository answerRepository;

    @InjectMocks
    private FormSubmissionService service;

    private FormTemplate testTemplate;
    private FormSubmission testSubmission;
    private UUID templateId;
    private UUID submissionId;
    private UUID patientId;

    @BeforeEach
    void setUp() {
        templateId = UUID.randomUUID();
        submissionId = UUID.randomUUID();
        patientId = UUID.randomUUID();

        testTemplate = new FormTemplate("PHQ9", "PHQ-9", FormType.SCREENING);
        setId(testTemplate, templateId);
        testTemplate.setScoringFormula("SUM(q1,q2,q3)");
        testTemplate.publish();

        FormField field1 = new FormField("q1", FieldType.SCALE, "Question 1");
        field1.setRequired(true);
        testTemplate.addField(field1);

        FormField field2 = new FormField("q2", FieldType.SCALE, "Question 2");
        field2.setRequired(true);
        testTemplate.addField(field2);

        FormField field3 = new FormField("q3", FieldType.SCALE, "Question 3");
        field3.setRequired(false);
        testTemplate.addField(field3);

        testSubmission = new FormSubmission(testTemplate, patientId);
        setId(testSubmission, submissionId);
    }

    private void setId(Object entity, UUID id) {
        try {
            var idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Nested
    @DisplayName("startSubmission")
    class StartSubmission {

        @Test
        @DisplayName("should start new submission")
        void shouldStartNewSubmission() {
            StartSubmissionRequest request = new StartSubmissionRequest(
                    "PHQ9",
                    patientId,
                    null,
                    UUID.randomUUID(),
                    "PATIENT",
                    60,
                    "WEB",
                    "192.168.1.1"
            );

            when(templateRepository.findActiveByCode("PHQ9"))
                    .thenReturn(Optional.of(testTemplate));
            when(submissionRepository.save(any())).thenAnswer(invocation -> {
                FormSubmission saved = invocation.getArgument(0);
                setId(saved, submissionId);
                return saved;
            });

            FormSubmissionDto result = service.startSubmission(request);

            assertThat(result).isNotNull();
            assertThat(result.templateCode()).isEqualTo("PHQ9");
            assertThat(result.patientId()).isEqualTo(patientId);
            assertThat(result.status()).isEqualTo(SubmissionStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("should throw when template not found")
        void shouldThrowWhenTemplateNotFound() {
            StartSubmissionRequest request = new StartSubmissionRequest(
                    "UNKNOWN", patientId, null, null, null, null, null, null
            );

            when(templateRepository.findActiveByCode("UNKNOWN"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.startSubmission(request))
                    .isInstanceOf(FormTemplateNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("saveAnswers")
    class SaveAnswers {

        @Test
        @DisplayName("should save answers")
        void shouldSaveAnswers() {
            AnswerRequest answer1 = new AnswerRequest(
                    "q1", null, 2.0, null, null, null, null, null, null, null
            );
            AnswerRequest answer2 = new AnswerRequest(
                    "q2", null, 1.0, null, null, null, null, null, null, null
            );
            SaveAnswersRequest request = new SaveAnswersRequest(List.of(answer1, answer2), false);

            when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(testSubmission));
            when(submissionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            FormSubmissionDto result = service.saveAnswers(submissionId, request);

            assertThat(result).isNotNull();
            verify(submissionRepository).save(any());
        }

        @Test
        @DisplayName("should throw when submission not in progress")
        void shouldThrowWhenSubmissionNotInProgress() {
            testSubmission.complete();
            when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(testSubmission));

            SaveAnswersRequest request = new SaveAnswersRequest(List.of(), false);

            assertThatThrownBy(() -> service.saveAnswers(submissionId, request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("IN_PROGRESS");
        }
    }

    @Nested
    @DisplayName("completeSubmission")
    class CompleteSubmission {

        @Test
        @DisplayName("should complete submission with all required answers")
        void shouldCompleteSubmissionWithAllRequiredAnswers() {
            FormAnswer answer1 = new FormAnswer("q1", FieldType.SCALE);
            answer1.setNumericValue(2.0);
            testSubmission.addAnswer(answer1);

            FormAnswer answer2 = new FormAnswer("q2", FieldType.SCALE);
            answer2.setNumericValue(1.0);
            testSubmission.addAnswer(answer2);

            when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(testSubmission));
            when(submissionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            FormSubmissionDto result = service.completeSubmission(submissionId);

            assertThat(result.status()).isEqualTo(SubmissionStatus.COMPLETED);
        }

        @Test
        @DisplayName("should throw when required answers are missing")
        void shouldThrowWhenRequiredAnswersMissing() {
            FormAnswer answer1 = new FormAnswer("q1", FieldType.SCALE);
            answer1.setNumericValue(2.0);
            testSubmission.addAnswer(answer1);
            // q2 is missing but required

            when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(testSubmission));

            assertThatThrownBy(() -> service.completeSubmission(submissionId))
                    .isInstanceOf(FormValidationException.class)
                    .hasMessageContaining("q2");
        }
    }

    @Nested
    @DisplayName("reviewSubmission")
    class ReviewSubmission {

        @Test
        @DisplayName("should review completed submission")
        void shouldReviewCompletedSubmission() {
            FormAnswer answer1 = new FormAnswer("q1", FieldType.SCALE);
            answer1.setNumericValue(2.0);
            testSubmission.addAnswer(answer1);
            FormAnswer answer2 = new FormAnswer("q2", FieldType.SCALE);
            answer2.setNumericValue(1.0);
            testSubmission.addAnswer(answer2);
            testSubmission.complete();

            UUID reviewerId = UUID.randomUUID();
            ReviewSubmissionRequest request = new ReviewSubmissionRequest(
                    reviewerId, "DOCTOR", "Looks good", true
            );

            when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(testSubmission));
            when(submissionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            FormSubmissionDto result = service.reviewSubmission(submissionId, request);

            assertThat(result.status()).isEqualTo(SubmissionStatus.REVIEWED);
        }

        @Test
        @DisplayName("should throw when submission not completed")
        void shouldThrowWhenSubmissionNotCompleted() {
            ReviewSubmissionRequest request = new ReviewSubmissionRequest(
                    UUID.randomUUID(), "DOCTOR", null, true
            );

            when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(testSubmission));

            assertThatThrownBy(() -> service.reviewSubmission(submissionId, request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("COMPLETED");
        }
    }

    @Nested
    @DisplayName("calculateScore")
    class CalculateScore {

        @Test
        @DisplayName("should calculate score using SUM formula")
        void shouldCalculateScoreUsingSumFormula() {
            FormAnswer answer1 = new FormAnswer("q1", FieldType.SCALE);
            answer1.setNumericValue(2.0);
            testSubmission.addAnswer(answer1);

            FormAnswer answer2 = new FormAnswer("q2", FieldType.SCALE);
            answer2.setNumericValue(3.0);
            testSubmission.addAnswer(answer2);

            FormAnswer answer3 = new FormAnswer("q3", FieldType.SCALE);
            answer3.setNumericValue(1.0);
            testSubmission.addAnswer(answer3);

            when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(testSubmission));
            when(submissionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            Double score = service.calculateScore(submissionId);

            assertThat(score).isEqualTo(6.0);
        }

        @Test
        @DisplayName("should return null when no formula")
        void shouldReturnNullWhenNoFormula() {
            testTemplate.setScoringFormula(null);

            when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(testSubmission));

            Double score = service.calculateScore(submissionId);

            assertThat(score).isNull();
        }
    }

    @Nested
    @DisplayName("cancelSubmission")
    class CancelSubmission {

        @Test
        @DisplayName("should cancel in-progress submission")
        void shouldCancelInProgressSubmission() {
            when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(testSubmission));
            when(submissionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            FormSubmissionDto result = service.cancelSubmission(submissionId);

            assertThat(result.status()).isEqualTo(SubmissionStatus.CANCELLED);
        }

        @Test
        @DisplayName("should throw when submission already completed")
        void shouldThrowWhenSubmissionAlreadyCompleted() {
            FormAnswer answer1 = new FormAnswer("q1", FieldType.SCALE);
            answer1.setNumericValue(2.0);
            testSubmission.addAnswer(answer1);
            FormAnswer answer2 = new FormAnswer("q2", FieldType.SCALE);
            answer2.setNumericValue(1.0);
            testSubmission.addAnswer(answer2);
            testSubmission.complete();

            when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(testSubmission));

            assertThatThrownBy(() -> service.cancelSubmission(submissionId))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("getPatientSubmissions")
    class GetPatientSubmissions {

        @Test
        @DisplayName("should get patient submissions with status filter")
        void shouldGetPatientSubmissionsWithStatusFilter() {
            when(submissionRepository.findByPatientIdAndStatus(patientId, SubmissionStatus.COMPLETED))
                    .thenReturn(List.of(testSubmission));

            List<FormSubmissionSummaryDto> result = service.getPatientSubmissions(
                    patientId, SubmissionStatus.COMPLETED
            );

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("should get all patient submissions without filter")
        void shouldGetAllPatientSubmissionsWithoutFilter() {
            when(submissionRepository.findByPatientId(patientId))
                    .thenReturn(List.of(testSubmission));

            List<FormSubmissionSummaryDto> result = service.getPatientSubmissions(patientId, null);

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("deleteSubmission")
    class DeleteSubmission {

        @Test
        @DisplayName("should delete in-progress submission")
        void shouldDeleteInProgressSubmission() {
            when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(testSubmission));

            service.deleteSubmission(submissionId);

            verify(submissionRepository).delete(testSubmission);
        }

        @Test
        @DisplayName("should throw when deleting completed submission")
        void shouldThrowWhenDeletingCompletedSubmission() {
            FormAnswer answer1 = new FormAnswer("q1", FieldType.SCALE);
            answer1.setNumericValue(2.0);
            testSubmission.addAnswer(answer1);
            FormAnswer answer2 = new FormAnswer("q2", FieldType.SCALE);
            answer2.setNumericValue(1.0);
            testSubmission.addAnswer(answer2);
            testSubmission.complete();

            when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(testSubmission));

            assertThatThrownBy(() -> service.deleteSubmission(submissionId))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
