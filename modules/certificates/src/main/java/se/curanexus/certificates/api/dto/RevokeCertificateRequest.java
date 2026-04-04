package se.curanexus.certificates.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RevokeCertificateRequest(
        @NotBlank @Size(max = 500) String reason
) {
}
