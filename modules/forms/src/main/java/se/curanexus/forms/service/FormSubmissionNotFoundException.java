package se.curanexus.forms.service;

import java.util.UUID;

public class FormSubmissionNotFoundException extends RuntimeException {

    public FormSubmissionNotFoundException(UUID id) {
        super("Form submission not found: " + id);
    }
}
