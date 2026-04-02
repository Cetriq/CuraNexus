package se.curanexus.patient.api.dto;

import se.curanexus.patient.domain.RelatedPerson;
import se.curanexus.patient.domain.RelationshipType;

import java.time.LocalDate;
import java.util.UUID;

public record RelatedPersonDto(
        UUID id,
        RelationshipType relationship,
        String personalIdentityNumber,
        String givenName,
        String familyName,
        String phone,
        String email,
        boolean isEmergencyContact,
        boolean isLegalGuardian,
        LocalDate validFrom,
        LocalDate validTo
) {
    public static RelatedPersonDto from(RelatedPerson relatedPerson) {
        return new RelatedPersonDto(
                relatedPerson.getId(),
                relatedPerson.getRelationship(),
                relatedPerson.getPersonalIdentityNumber(),
                relatedPerson.getGivenName(),
                relatedPerson.getFamilyName(),
                relatedPerson.getPhone(),
                relatedPerson.getEmail(),
                relatedPerson.isEmergencyContact(),
                relatedPerson.isLegalGuardian(),
                relatedPerson.getValidFrom(),
                relatedPerson.getValidTo()
        );
    }
}
