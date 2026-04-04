package se.curanexus.certificates.service;

import java.util.UUID;

public class CertificateTemplateNotFoundException extends RuntimeException {

    public CertificateTemplateNotFoundException(UUID id) {
        super("Certificate template not found: " + id);
    }

    public CertificateTemplateNotFoundException(String code) {
        super("Certificate template not found with code: " + code);
    }
}
