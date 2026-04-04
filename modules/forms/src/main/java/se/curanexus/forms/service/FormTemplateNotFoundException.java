package se.curanexus.forms.service;

import java.util.UUID;

public class FormTemplateNotFoundException extends RuntimeException {

    public FormTemplateNotFoundException(UUID id) {
        super("Form template not found: " + id);
    }

    public FormTemplateNotFoundException(String code) {
        super("Form template not found with code: " + code);
    }

    public FormTemplateNotFoundException(String code, Integer version) {
        super("Form template not found: " + code + " v" + version);
    }
}
