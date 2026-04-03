package se.curanexus.booking.api.dto;

import se.curanexus.booking.domain.Appointment;
import se.curanexus.booking.domain.AppointmentStatus;
import se.curanexus.booking.domain.AppointmentType;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO för bokningsinformation.
 */
public record AppointmentDto(
        UUID id,
        String bookingReference,
        UUID patientId,
        UUID practitionerId,
        String practitionerHsaId,
        UUID unitId,
        String unitHsaId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        AppointmentStatus status,
        AppointmentType appointmentType,
        String serviceType,
        String reasonText,
        String reasonCode,
        String patientInstructions,
        UUID encounterId,
        Instant checkedInAt,
        Instant createdAt,
        Instant updatedAt
) {
    /**
     * Konvertera från domänentitet till DTO.
     */
    public static AppointmentDto from(Appointment appointment) {
        return new AppointmentDto(
                appointment.getId(),
                appointment.getBookingReference(),
                appointment.getPatientId(),
                appointment.getPractitionerId(),
                appointment.getPractitionerHsaId(),
                appointment.getUnitId(),
                appointment.getUnitHsaId(),
                appointment.getStartTime(),
                appointment.getEndTime(),
                appointment.getStatus(),
                appointment.getAppointmentType(),
                appointment.getServiceType(),
                appointment.getReasonText(),
                appointment.getReasonCode(),
                appointment.getPatientInstructions(),
                appointment.getEncounterId(),
                appointment.getCheckedInAt(),
                appointment.getCreatedAt(),
                appointment.getUpdatedAt()
        );
    }
}
