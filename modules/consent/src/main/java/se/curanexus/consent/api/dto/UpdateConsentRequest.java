package se.curanexus.consent.api.dto;

import java.time.LocalDate;
import java.util.UUID;

public record UpdateConsentRequest(
        String description,
        String scope,
        UUID managingUnitId,
        String managingUnitName,
        LocalDate validFrom,
        LocalDate validUntil,
        String documentReference
) {}
