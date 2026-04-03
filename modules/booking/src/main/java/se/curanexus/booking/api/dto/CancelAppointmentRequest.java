package se.curanexus.booking.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request för att avboka en tid.
 */
public record CancelAppointmentRequest(
        @NotBlank(message = "Orsak till avbokning krävs")
        String reason,

        boolean byPatient
) {
}
