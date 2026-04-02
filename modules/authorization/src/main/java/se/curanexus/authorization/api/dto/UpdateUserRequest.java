package se.curanexus.authorization.api.dto;

import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Size(max = 100) String firstName,
        @Size(max = 100) String lastName,
        @Size(max = 100) String title,
        @Size(max = 100) String department,
        @Size(max = 50) String hsaId
) {}
