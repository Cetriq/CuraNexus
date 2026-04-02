package se.curanexus.authorization.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.curanexus.authorization.api.dto.*;
import se.curanexus.authorization.domain.User;
import se.curanexus.authorization.service.AuthorizationService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final AuthorizationService authorizationService;

    public UserController(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        User user = authorizationService.createUser(
                request.username(),
                request.email(),
                request.firstName(),
                request.lastName(),
                request.userType()
        );

        if (request.title() != null || request.department() != null) {
            user = authorizationService.updateUser(
                    user.getId(),
                    null,
                    null,
                    request.title(),
                    request.department()
            );
        }

        if (request.hsaId() != null) {
            user = authorizationService.setUserHsaId(user.getId(), request.hsaId());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user));
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String search) {

        List<User> users;
        if (role != null) {
            users = authorizationService.getUsersByRole(role);
        } else if (search != null) {
            users = authorizationService.searchUsers(search);
        } else {
            users = authorizationService.getAllUsers();
        }

        List<UserResponse> responses = users.stream()
                .map(UserResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/active")
    public ResponseEntity<List<UserResponse>> getActiveUsers() {
        List<UserResponse> responses = authorizationService.getActiveUsers().stream()
                .map(UserResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID userId) {
        User user = authorizationService.getUser(userId);
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @GetMapping("/by-username/{username}")
    public ResponseEntity<UserResponse> getUserByUsername(@PathVariable String username) {
        User user = authorizationService.getUserByUsername(username);
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @GetMapping("/by-hsa-id/{hsaId}")
    public ResponseEntity<UserResponse> getUserByHsaId(@PathVariable String hsaId) {
        User user = authorizationService.getUserByHsaId(hsaId);
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @GetMapping("/{userId}/full")
    public ResponseEntity<UserResponse> getUserWithRolesAndPermissions(@PathVariable UUID userId) {
        User user = authorizationService.getUserWithRolesAndPermissions(userId);
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserRequest request) {

        User user = authorizationService.updateUser(
                userId,
                request.firstName(),
                request.lastName(),
                request.title(),
                request.department()
        );

        if (request.hsaId() != null) {
            user = authorizationService.setUserHsaId(userId, request.hsaId());
        }

        return ResponseEntity.ok(UserResponse.from(user));
    }

    @PostMapping("/{userId}/activate")
    public ResponseEntity<Void> activateUser(@PathVariable UUID userId) {
        authorizationService.activateUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/deactivate")
    public ResponseEntity<Void> deactivateUser(@PathVariable UUID userId) {
        authorizationService.deactivateUser(userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
        authorizationService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/roles")
    public ResponseEntity<UserResponse> assignRoles(
            @PathVariable UUID userId,
            @Valid @RequestBody AssignRolesRequest request) {

        User user = authorizationService.assignRolesToUser(userId, request.roleCodes());
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @DeleteMapping("/{userId}/roles/{roleId}")
    public ResponseEntity<UserResponse> removeRole(
            @PathVariable UUID userId,
            @PathVariable UUID roleId) {

        User user = authorizationService.removeRoleFromUser(userId, roleId);
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @GetMapping("/{userId}/context")
    public ResponseEntity<AuthorizationContextResponse> getAuthorizationContext(@PathVariable UUID userId) {
        var context = authorizationService.getAuthorizationContext(userId);
        return ResponseEntity.ok(AuthorizationContextResponse.from(context));
    }
}
