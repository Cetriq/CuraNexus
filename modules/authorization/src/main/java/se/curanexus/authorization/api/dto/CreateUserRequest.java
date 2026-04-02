package se.curanexus.authorization.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import se.curanexus.authorization.domain.UserType;

public record CreateUserRequest(
        @NotBlank @Size(max = 50) String username,
        @NotBlank @Email @Size(max = 200) String email,
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @NotNull UserType userType,
        @Size(max = 100) String title,
        @Size(max = 100) String department,
        @Size(max = 50) String hsaId
) {}
