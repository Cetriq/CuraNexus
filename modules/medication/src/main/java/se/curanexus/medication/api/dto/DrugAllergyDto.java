package se.curanexus.medication.api.dto;

import se.curanexus.medication.domain.DrugAllergy;
import se.curanexus.medication.domain.DrugAllergy.AllergySeverity;
import se.curanexus.medication.domain.DrugAllergy.ReactionType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record DrugAllergyDto(
        UUID id,
        UUID patientId,
        UUID medicationId,
        String atcCode,
        String substanceName,
        ReactionType reactionType,
        AllergySeverity severity,
        String reactionDescription,
        LocalDate onsetDate,
        boolean verified,
        UUID verifiedById,
        Instant verifiedAt,
        String source,
        boolean active,
        String notes,
        Instant createdAt
) {
    public static DrugAllergyDto from(DrugAllergy a) {
        return new DrugAllergyDto(
                a.getId(),
                a.getPatientId(),
                a.getMedicationId(),
                a.getAtcCode(),
                a.getSubstanceName(),
                a.getReactionType(),
                a.getSeverity(),
                a.getReactionDescription(),
                a.getOnsetDate(),
                a.isVerified(),
                a.getVerifiedById(),
                a.getVerifiedAt(),
                a.getSource(),
                a.isActive(),
                a.getNotes(),
                a.getCreatedAt()
        );
    }
}
