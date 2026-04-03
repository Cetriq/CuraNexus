package se.curanexus.booking.api.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import se.curanexus.booking.domain.AppointmentType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Request för att skapa en ny bokning.
 */
public record CreateAppointmentRequest(
        @NotNull(message = "Patient-ID krävs")
        UUID patientId,

        UUID practitionerId,

        String practitionerHsaId,

        UUID unitId,

        String unitHsaId,

        @NotNull(message = "Starttid krävs")
        @Future(message = "Starttid måste vara i framtiden")
        LocalDateTime startTime,

        @NotNull(message = "Sluttid krävs")
        @Future(message = "Sluttid måste vara i framtiden")
        LocalDateTime endTime,

        AppointmentType appointmentType,

        UUID timeSlotId,

        String serviceType,

        String reasonText,

        String reasonCode,

        String patientInstructions,

        String internalNotes
) {
}
