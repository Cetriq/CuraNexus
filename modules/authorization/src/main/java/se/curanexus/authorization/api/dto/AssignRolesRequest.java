package se.curanexus.authorization.api.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record AssignRolesRequest(
        @NotEmpty Set<String> roleCodes
) {}
