package se.curanexus.authorization.api.dto;

import jakarta.validation.constraints.Size;

public record UpdateRoleRequest(
        @Size(max = 100) String name,
        @Size(max = 500) String description
) {}
