package se.curanexus.forms.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SaveAnswersRequest(
        @NotEmpty @Valid List<AnswerRequest> answers,
        Boolean autoSave
) {
}
