package se.curanexus.authorization.api.dto;

import jakarta.validation.constraints.NotNull;

import java.util.Set;
import java.util.UUID;

public record CheckAccessRequest(
        @NotNull UUID userId,
        UUID patientId,
        String permissionCode,
        Set<String> permissionCodes
) {}
