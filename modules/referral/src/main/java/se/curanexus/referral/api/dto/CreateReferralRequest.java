package se.curanexus.referral.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import se.curanexus.referral.domain.ReferralPriority;
import se.curanexus.referral.domain.ReferralType;

import java.time.LocalDate;
import java.util.UUID;

public record CreateReferralRequest(
        @NotNull UUID patientId,
        String patientPersonnummer,
        String patientName,
        @NotNull ReferralType referralType,
        ReferralPriority priority,
        String senderUnitHsaId,
        String senderUnitName,
        String senderPractitionerHsaId,
        String senderPractitionerName,
        UUID receiverUnitId,
        String receiverUnitHsaId,
        String receiverUnitName,
        String requestedSpecialty,
        @NotBlank String reason,
        String diagnosisCode,
        String diagnosisText,
        String clinicalHistory,
        String currentStatus,
        String examinationsDone,
        String currentMedication,
        String allergies,
        String additionalInfo,
        UUID sourceEncounterId,
        LocalDate requestedDate,
        LocalDate validUntil,
        boolean sendImmediately
) {
}
