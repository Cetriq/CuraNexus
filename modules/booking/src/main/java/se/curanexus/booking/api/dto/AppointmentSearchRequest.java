package se.curanexus.booking.api.dto;

import se.curanexus.booking.domain.AppointmentStatus;
import se.curanexus.booking.domain.AppointmentType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Request för att söka bokningar.
 */
public record AppointmentSearchRequest(
        UUID patientId,
        UUID practitionerId,
        UUID unitId,
        AppointmentStatus status,
        AppointmentType appointmentType,
        LocalDateTime fromDate,
        LocalDateTime toDate
) {
}
