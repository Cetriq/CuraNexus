package se.curanexus.booking.api.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Request för att omboka en tid.
 */
public record RescheduleRequest(
        @NotNull(message = "Ny starttid krävs")
        @Future(message = "Ny starttid måste vara i framtiden")
        LocalDateTime newStartTime,

        @NotNull(message = "Ny sluttid krävs")
        @Future(message = "Ny sluttid måste vara i framtiden")
        LocalDateTime newEndTime,

        UUID newTimeSlotId
) {
}
