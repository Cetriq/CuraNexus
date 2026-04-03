package se.curanexus.booking.api.dto;

import jakarta.validation.constraints.NotNull;
import se.curanexus.booking.domain.WaitlistEntry.WaitlistPriority;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Request för att skapa en väntelistpost.
 */
public record CreateWaitlistEntryRequest(
        @NotNull(message = "Patient-ID krävs")
        UUID patientId,

        UUID practitionerId,

        UUID unitId,

        @NotNull(message = "Tjänstetyp krävs")
        String serviceType,

        String reasonText,

        WaitlistPriority priority,

        LocalDate preferredDateFrom,

        LocalDate preferredDateTo
) {
}
