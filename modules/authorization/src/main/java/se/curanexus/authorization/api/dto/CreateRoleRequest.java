package se.curanexus.authorization.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record CreateRoleRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 50) String code,
        @Size(max = 500) String description,
        Set<String> permissionCodes
) {}
