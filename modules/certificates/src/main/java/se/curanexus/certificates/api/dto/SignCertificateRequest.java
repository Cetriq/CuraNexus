package se.curanexus.certificates.api.dto;

import jakarta.validation.constraints.NotBlank;

public record SignCertificateRequest(
        @NotBlank String signature
) {
}
