package se.curanexus.forms.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import se.curanexus.forms.api.dto.*;
import se.curanexus.forms.domain.*;
import se.curanexus.forms.repository.FormFieldRepository;
import se.curanexus.forms.repository.FormTemplateRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FormTemplateServiceTest {

    @Mock
    private FormTemplateRepository templateRepository;

    @Mock
    private FormFieldRepository fieldRepository;

    @InjectMocks
    private FormTemplateService service;

    private FormTemplate testTemplate;
    private UUID templateId;

    @BeforeEach
    void setUp() {
        templateId = UUID.randomUUID();
        testTemplate = new FormTemplate("PHQ9", "PHQ-9 Depression Screening", FormType.SCREENING);
        // Set id via reflection for test
        try {
            var idField = FormTemplate.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(testTemplate, templateId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        testTemplate.setDescription("Patient Health Questionnaire");
        testTemplate.setCategory("Mental Health");
        testTemplate.setEstimatedDurationMinutes(10);
    }

    @Nested
    @DisplayName("createTemplate")
    class CreateTemplate {

        @Test
        @DisplayName("should create template with fields")
        void shouldCreateTemplateWithFields() {
            CreateFormFieldRequest fieldRequest = new CreateFormFieldRequest(
                    "q1",
                    FieldType.SCALE,
                    "Little interest or pleasure?",
                    null, null, "0 = Not at all, 3 = Nearly every day",
                    1, true, null, null, null, null,
                    0, 3, 1, null, null, null, null
            );

            CreateFormTemplateRequest request = new CreateFormTemplateRequest(
                    "PHQ9",
                    "PHQ-9 Depression Screening",
                    "Patient Health Questionnaire",
                    FormType.SCREENING,
                    "Mental Health",
                    10,
                    "Answer all questions",
                    "SUM(q1,q2,q3)",
                    null,
                    UUID.randomUUID(),
                    List.of(fieldRequest)
            );

            when(templateRepository.existsByCode("PHQ9")).thenReturn(false);
            when(templateRepository.save(any(FormTemplate.class))).thenAnswer(invocation -> {
                FormTemplate saved = invocation.getArgument(0);
                try {
                    var idField = FormTemplate.class.getDeclaredField("id");
                    idField.setAccessible(true);
                    idField.set(saved, templateId);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return saved;
            });

            FormTemplateDto result = service.createTemplate(request);

            assertThat(result).isNotNull();
            assertThat(result.code()).isEqualTo("PHQ9");
            assertThat(result.name()).isEqualTo("PHQ-9 Depression Screening");
            assertThat(result.status()).isEqualTo(FormStatus.DRAFT);
            assertThat(result.fields()).hasSize(1);

            verify(templateRepository).save(any(FormTemplate.class));
        }

        @Test
        @DisplayName("should throw when code already exists")
        void shouldThrowWhenCodeExists() {
            CreateFormTemplateRequest request = new CreateFormTemplateRequest(
                    "PHQ9", "Name", null, FormType.SCREENING,
                    null, null, null, null, null, null, null
            );

            when(templateRepository.existsByCode("PHQ9")).thenReturn(true);

            assertThatThrownBy(() -> service.createTemplate(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("PHQ9");
        }
    }

    @Nested
    @DisplayName("getTemplate")
    class GetTemplate {

        @Test
        @DisplayName("should return template when found")
        void shouldReturnTemplateWhenFound() {
            when(templateRepository.findById(templateId)).thenReturn(Optional.of(testTemplate));

            FormTemplateDto result = service.getTemplate(templateId);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(templateId);
            assertThat(result.code()).isEqualTo("PHQ9");
        }

        @Test
        @DisplayName("should throw exception when not found")
        void shouldThrowExceptionWhenNotFound() {
            UUID unknownId = UUID.randomUUID();
            when(templateRepository.findById(unknownId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getTemplate(unknownId))
                    .isInstanceOf(FormTemplateNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getActiveTemplateByCode")
    class GetActiveTemplateByCode {

        @Test
        @DisplayName("should return active template by code")
        void shouldReturnActiveTemplateByCode() {
            testTemplate.publish();
            when(templateRepository.findActiveByCode("PHQ9"))
                    .thenReturn(Optional.of(testTemplate));

            FormTemplateDto result = service.getActiveTemplateByCode("PHQ9");

            assertThat(result).isNotNull();
            assertThat(result.code()).isEqualTo("PHQ9");
            assertThat(result.status()).isEqualTo(FormStatus.ACTIVE);
        }

        @Test
        @DisplayName("should throw when no active template exists")
        void shouldThrowWhenNoActiveTemplateExists() {
            when(templateRepository.findActiveByCode("UNKNOWN"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getActiveTemplateByCode("UNKNOWN"))
                    .isInstanceOf(FormTemplateNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("publishTemplate")
    class PublishTemplate {

        @Test
        @DisplayName("should publish draft template with fields")
        void shouldPublishDraftTemplateWithFields() {
            FormField field = new FormField("q1", FieldType.TEXT, "Question 1");
            testTemplate.addField(field);

            when(templateRepository.findById(templateId)).thenReturn(Optional.of(testTemplate));
            when(templateRepository.findActiveByCode("PHQ9")).thenReturn(Optional.empty());
            when(templateRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            FormTemplateDto result = service.publishTemplate(templateId);

            assertThat(result.status()).isEqualTo(FormStatus.ACTIVE);
        }

        @Test
        @DisplayName("should throw when template has no fields")
        void shouldThrowWhenTemplateHasNoFields() {
            when(templateRepository.findById(templateId)).thenReturn(Optional.of(testTemplate));

            assertThatThrownBy(() -> service.publishTemplate(templateId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("fields");
        }

        @Test
        @DisplayName("should throw when template is not draft")
        void shouldThrowWhenTemplateIsNotDraft() {
            FormField field = new FormField("q1", FieldType.TEXT, "Question 1");
            testTemplate.addField(field);
            testTemplate.publish(); // Now it's ACTIVE

            when(templateRepository.findById(templateId)).thenReturn(Optional.of(testTemplate));

            assertThatThrownBy(() -> service.publishTemplate(templateId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("draft");
        }
    }

    @Nested
    @DisplayName("createNewVersion")
    class CreateNewVersion {

        @Test
        @DisplayName("should create new version from existing template")
        void shouldCreateNewVersionFromExistingTemplate() {
            FormField field = new FormField("q1", FieldType.SCALE, "Question 1");
            testTemplate.addField(field);
            testTemplate.publish();

            when(templateRepository.findById(templateId)).thenReturn(Optional.of(testTemplate));
            when(templateRepository.save(any())).thenAnswer(invocation -> {
                FormTemplate saved = invocation.getArgument(0);
                try {
                    var idField = FormTemplate.class.getDeclaredField("id");
                    idField.setAccessible(true);
                    idField.set(saved, UUID.randomUUID());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return saved;
            });

            FormTemplateDto result = service.createNewVersion(templateId);

            assertThat(result.version()).isEqualTo(2);
            assertThat(result.status()).isEqualTo(FormStatus.DRAFT);
            assertThat(result.code()).isEqualTo("PHQ9");
        }

        @Test
        @DisplayName("should throw when creating version from draft")
        void shouldThrowWhenCreatingVersionFromDraft() {
            when(templateRepository.findById(templateId)).thenReturn(Optional.of(testTemplate));

            assertThatThrownBy(() -> service.createNewVersion(templateId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("DRAFT");
        }
    }

    @Nested
    @DisplayName("deleteTemplate")
    class DeleteTemplate {

        @Test
        @DisplayName("should delete draft template")
        void shouldDeleteDraftTemplate() {
            when(templateRepository.findById(templateId)).thenReturn(Optional.of(testTemplate));

            service.deleteTemplate(templateId);

            verify(templateRepository).delete(testTemplate);
        }

        @Test
        @DisplayName("should throw when deleting non-draft template")
        void shouldThrowWhenDeletingNonDraftTemplate() {
            FormField field = new FormField("q1", FieldType.TEXT, "Question 1");
            testTemplate.addField(field);
            testTemplate.publish();

            when(templateRepository.findById(templateId)).thenReturn(Optional.of(testTemplate));

            assertThatThrownBy(() -> service.deleteTemplate(templateId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("draft");
        }
    }

    @Nested
    @DisplayName("searchTemplates")
    class SearchTemplates {

        @Test
        @DisplayName("should search templates with filters")
        void shouldSearchTemplatesWithFilters() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<FormTemplate> page = new PageImpl<>(List.of(testTemplate), pageable, 1);

            when(templateRepository.searchTemplates(
                    eq(FormStatus.ACTIVE),
                    eq(FormType.SCREENING),
                    eq("Mental Health"),
                    eq(pageable)
            )).thenReturn(page);

            Page<FormTemplateSummaryDto> result = service.searchTemplates(
                    FormType.SCREENING,
                    "Mental Health",
                    FormStatus.ACTIVE,
                    null,
                    null,
                    pageable
            );

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).code()).isEqualTo("PHQ9");
        }
    }
}
