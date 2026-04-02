package se.curanexus.patient.api.dto;

import se.curanexus.patient.domain.ContactInfo;
import se.curanexus.patient.domain.ContactType;
import se.curanexus.patient.domain.ContactUse;

import java.time.LocalDate;
import java.util.UUID;

public record ContactInfoDto(
        UUID id,
        ContactType type,
        String value,
        ContactUse use,
        boolean primary,
        LocalDate validFrom,
        LocalDate validTo
) {
    public static ContactInfoDto from(ContactInfo contact) {
        return new ContactInfoDto(
                contact.getId(),
                contact.getType(),
                contact.getValue(),
                contact.getUse(),
                contact.isPrimary(),
                contact.getValidFrom(),
                contact.getValidTo()
        );
    }
}
