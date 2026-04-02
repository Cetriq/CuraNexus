package se.curanexus.authorization.api.dto;

import se.curanexus.authorization.domain.User;
import se.curanexus.authorization.domain.UserType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String email,
        String firstName,
        String lastName,
        String title,
        String department,
        UserType userType,
        String hsaId,
        boolean active,
        List<RoleSummary> roles,
        Instant createdAt,
        Instant updatedAt
) {
    public static UserResponse from(User user) {
        List<RoleSummary> roleSummaries = user.getRoles().stream()
                .map(r -> new RoleSummary(r.getId(), r.getCode(), r.getName()))
                .toList();

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getTitle(),
                user.getDepartment(),
                user.getUserType(),
                user.getHsaId(),
                user.isActive(),
                roleSummaries,
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    public record RoleSummary(UUID id, String code, String name) {}
}
