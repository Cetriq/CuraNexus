package se.curanexus.certificates.api.dto;

import se.curanexus.certificates.domain.CertificateTemplate;
import se.curanexus.certificates.domain.CertificateType;
import se.curanexus.certificates.domain.TemplateStatus;

import java.time.Instant;
import java.util.UUID;

public record CertificateTemplateDto(
        UUID id,
        String code,
        String name,
        String description,
        CertificateType type,
        TemplateStatus status,
        Integer version,
        String dataSchema,
        String recipientSystem,
        boolean requiresSignature,
        Integer validityDays,
        Instant createdAt,
        Instant updatedAt,
        Instant publishedAt
) {
    public static CertificateTemplateDto from(CertificateTemplate template) {
        return new CertificateTemplateDto(
                template.getId(),
                template.getCode(),
                template.getName(),
                template.getDescription(),
                template.getType(),
                template.getStatus(),
                template.getVersion(),
                template.getDataSchema(),
                template.getRecipientSystem(),
                template.isRequiresSignature(),
                template.getValidityDays(),
                template.getCreatedAt(),
                template.getUpdatedAt(),
                template.getPublishedAt()
        );
    }
}
