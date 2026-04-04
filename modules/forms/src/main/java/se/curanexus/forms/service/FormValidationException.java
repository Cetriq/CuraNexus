package se.curanexus.forms.service;

import java.util.List;
import java.util.Map;

public class FormValidationException extends RuntimeException {

    private final Map<String, List<String>> fieldErrors;

    public FormValidationException(String message) {
        super(message);
        this.fieldErrors = Map.of();
    }

    public FormValidationException(String message, Map<String, List<String>> fieldErrors) {
        super(message);
        this.fieldErrors = fieldErrors;
    }

    public Map<String, List<String>> getFieldErrors() {
        return fieldErrors;
    }
}
