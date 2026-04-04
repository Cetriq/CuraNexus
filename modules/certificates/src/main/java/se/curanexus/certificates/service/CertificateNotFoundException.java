package se.curanexus.certificates.service;

import java.util.UUID;

public class CertificateNotFoundException extends RuntimeException {

    public CertificateNotFoundException(UUID id) {
        super("Certificate not found: " + id);
    }

    public CertificateNotFoundException(String certificateNumber) {
        super("Certificate not found with number: " + certificateNumber);
    }
}
