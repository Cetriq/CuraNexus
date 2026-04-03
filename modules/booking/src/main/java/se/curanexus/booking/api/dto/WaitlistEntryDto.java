package se.curanexus.booking.api.dto;

import se.curanexus.booking.domain.WaitlistEntry;
import se.curanexus.booking.domain.WaitlistEntry.WaitlistPriority;
import se.curanexus.booking.domain.WaitlistEntry.WaitlistStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO för väntelistpost.
 */
public record WaitlistEntryDto(
        UUID id,
        UUID patientId,
        UUID practitionerId,
        UUID unitId,
        String serviceType,
        String reasonText,
        WaitlistPriority priority,
        WaitlistStatus status,
        LocalDate preferredDateFrom,
        LocalDate preferredDateTo,
        UUID bookedAppointmentId,
        Instant notifiedAt,
        Instant createdAt
) {
    /**
     * Konvertera från domänentitet till DTO.
     */
    public static WaitlistEntryDto from(WaitlistEntry entry) {
        return new WaitlistEntryDto(
                entry.getId(),
                entry.getPatientId(),
                entry.getPractitionerId(),
                entry.getUnitId(),
                entry.getServiceType(),
                entry.getReasonText(),
                entry.getPriority(),
                entry.getStatus(),
                entry.getPreferredDateFrom(),
                entry.getPreferredDateTo(),
                entry.getBookedAppointmentId(),
                entry.getNotifiedAt(),
                entry.getCreatedAt()
        );
    }
}
