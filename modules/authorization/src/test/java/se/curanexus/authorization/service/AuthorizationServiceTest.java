package se.curanexus.authorization.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.curanexus.authorization.domain.*;
import se.curanexus.authorization.repository.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private CareRelationRepository careRelationRepository;

    private AuthorizationService service;

    @BeforeEach
    void setUp() {
        service = new AuthorizationService(
                userRepository,
                roleRepository,
                permissionRepository,
                careRelationRepository
        );
    }

    // ========== User Management Tests ==========

    @Test
    void shouldCreateUser() {
        when(userRepository.existsByUsername("johndoe")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User user = service.createUser("johndoe", "john@example.com", "John", "Doe", UserType.INTERNAL);

        assertNotNull(user);
        assertEquals("johndoe", user.getUsername());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowOnDuplicateUsername() {
        when(userRepository.existsByUsername("johndoe")).thenReturn(true);

        assertThrows(DuplicateUserException.class, () ->
                service.createUser("johndoe", "john@example.com", "John", "Doe", UserType.INTERNAL)
        );
    }

    @Test
    void shouldThrowOnDuplicateEmail() {
        when(userRepository.existsByUsername("johndoe")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThrows(DuplicateUserException.class, () ->
                service.createUser("johndoe", "john@example.com", "John", "Doe", UserType.INTERNAL)
        );
    }

    @Test
    void shouldGetUserById() {
        UUID userId = UUID.randomUUID();
        User user = new User("johndoe", "john@example.com", "John", "Doe", UserType.INTERNAL);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User result = service.getUser(userId);

        assertEquals(user, result);
    }

    @Test
    void shouldThrowOnUserNotFound() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> service.getUser(userId));
    }

    // ========== Role Management Tests ==========

    @Test
    void shouldCreateRole() {
        when(roleRepository.existsByCode("CUSTOM_ROLE")).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenAnswer(i -> i.getArgument(0));

        Role role = service.createRole("Custom Role", "CUSTOM_ROLE", "A custom role");

        assertNotNull(role);
        assertEquals("CUSTOM_ROLE", role.getCode());
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void shouldThrowOnDuplicateRoleCode() {
        when(roleRepository.existsByCode("ADMIN")).thenReturn(true);

        assertThrows(DuplicateRoleException.class, () ->
                service.createRole("Admin", "ADMIN", "Administrator role")
        );
    }

    @Test
    void shouldNotModifySystemRole() {
        UUID roleId = UUID.randomUUID();
        Role systemRole = new Role("Admin", "ADMIN");
        systemRole.setSystemRole(true);
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(systemRole));

        assertThrows(SystemRoleModificationException.class, () ->
                service.updateRole(roleId, "New Name", "New Description")
        );
    }

    @Test
    void shouldNotDeleteSystemRole() {
        UUID roleId = UUID.randomUUID();
        Role systemRole = new Role("Admin", "ADMIN");
        systemRole.setSystemRole(true);
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(systemRole));

        assertThrows(SystemRoleModificationException.class, () ->
                service.deleteRole(roleId)
        );
    }

    // ========== Care Relation Tests ==========

    @Test
    void shouldCreateCareRelation() {
        UUID userId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        User user = new User("johndoe", "john@example.com", "John", "Doe", UserType.INTERNAL);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(careRelationRepository.save(any(CareRelation.class))).thenAnswer(i -> i.getArgument(0));

        CareRelation relation = service.createCareRelation(userId, patientId, CareRelationType.PRIMARY_CARE, "Primary care assignment");

        assertNotNull(relation);
        assertEquals(userId, relation.getUserId());
        assertEquals(patientId, relation.getPatientId());
        assertEquals(CareRelationType.PRIMARY_CARE, relation.getRelationType());
    }

    @Test
    void shouldEndCareRelation() {
        UUID relationId = UUID.randomUUID();
        UUID endedById = UUID.randomUUID();
        CareRelation relation = new CareRelation(UUID.randomUUID(), UUID.randomUUID(), CareRelationType.PRIMARY_CARE);
        when(careRelationRepository.findById(relationId)).thenReturn(Optional.of(relation));
        when(careRelationRepository.save(any(CareRelation.class))).thenAnswer(i -> i.getArgument(0));

        service.endCareRelation(relationId, endedById);

        assertFalse(relation.isActive());
        assertEquals(endedById, relation.getEndedById());
        verify(careRelationRepository).save(relation);
    }

    // ========== Access Control Tests ==========

    @Test
    void shouldCheckPermission() {
        UUID userId = UUID.randomUUID();
        User user = new User("johndoe", "john@example.com", "John", "Doe", UserType.INTERNAL);
        Role role = new Role("Doctor", "DOCTOR");
        Permission permission = new Permission("PATIENT_READ", "Read Patient", ResourceType.PATIENT, ActionType.READ);
        role.addPermission(permission);
        user.addRole(role);
        when(userRepository.findByIdWithRolesAndPermissions(userId)).thenReturn(Optional.of(user));

        assertTrue(service.hasPermission(userId, "PATIENT_READ"));
        assertFalse(service.hasPermission(userId, "PATIENT_DELETE"));
    }

    @Test
    void shouldCheckCareRelation() {
        UUID userId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        when(careRelationRepository.hasActiveCareRelation(eq(userId), eq(patientId), any(LocalDateTime.class)))
                .thenReturn(true);

        assertTrue(service.hasCareRelation(userId, patientId));
    }

    @Test
    void shouldCheckPatientAccess() {
        UUID userId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        User user = new User("johndoe", "john@example.com", "John", "Doe", UserType.INTERNAL);
        Role role = new Role("Doctor", "DOCTOR");
        Permission permission = new Permission("PATIENT_READ", "Read Patient", ResourceType.PATIENT, ActionType.READ);
        role.addPermission(permission);
        user.addRole(role);
        when(userRepository.findByIdWithRolesAndPermissions(userId)).thenReturn(Optional.of(user));
        when(careRelationRepository.hasActiveCareRelation(eq(userId), eq(patientId), any(LocalDateTime.class)))
                .thenReturn(true);

        assertTrue(service.canAccessPatient(userId, patientId));
    }

    @Test
    void shouldDenyAccessWithoutPermission() {
        UUID userId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        User user = new User("johndoe", "john@example.com", "John", "Doe", UserType.INTERNAL);
        when(userRepository.findByIdWithRolesAndPermissions(userId)).thenReturn(Optional.of(user));

        assertFalse(service.canAccessPatient(userId, patientId));
    }

    @Test
    void shouldDenyAccessWithoutCareRelation() {
        UUID userId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        User user = new User("johndoe", "john@example.com", "John", "Doe", UserType.INTERNAL);
        Role role = new Role("Doctor", "DOCTOR");
        Permission permission = new Permission("PATIENT_READ", "Read Patient", ResourceType.PATIENT, ActionType.READ);
        role.addPermission(permission);
        user.addRole(role);
        when(userRepository.findByIdWithRolesAndPermissions(userId)).thenReturn(Optional.of(user));
        when(careRelationRepository.hasActiveCareRelation(eq(userId), eq(patientId), any(LocalDateTime.class)))
                .thenReturn(false);

        assertFalse(service.canAccessPatient(userId, patientId));
    }

    @Test
    void shouldThrowAccessDeniedOnCheckPermission() {
        UUID userId = UUID.randomUUID();
        User user = new User("johndoe", "john@example.com", "John", "Doe", UserType.INTERNAL);
        Permission permission = new Permission("PATIENT_READ", "Read Patient", ResourceType.PATIENT, ActionType.READ);
        when(userRepository.findByIdWithRolesAndPermissions(userId)).thenReturn(Optional.of(user));
        when(permissionRepository.findByCode("PATIENT_READ")).thenReturn(Optional.of(permission));

        assertThrows(AccessDeniedException.class, () ->
                service.checkPermission(userId, "PATIENT_READ")
        );
    }

    // ========== User-Role Assignment Tests ==========

    @Test
    void shouldAssignRoleToUser() {
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        User user = new User("johndoe", "john@example.com", "John", "Doe", UserType.INTERNAL);
        Role role = new Role("Doctor", "DOCTOR");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = service.assignRoleToUser(userId, roleId);

        assertTrue(result.hasRole("DOCTOR"));
    }

    // ========== Authorization Context Tests ==========

    @Test
    void shouldBuildAuthorizationContext() {
        UUID userId = UUID.randomUUID();
        User user = new User("johndoe", "john@example.com", "John", "Doe", UserType.INTERNAL);
        Role role = new Role("Doctor", "DOCTOR");
        Permission permission = new Permission("PATIENT_READ", "Read Patient", ResourceType.PATIENT, ActionType.READ);
        role.addPermission(permission);
        user.addRole(role);
        when(userRepository.findByIdWithRolesAndPermissions(userId)).thenReturn(Optional.of(user));
        when(careRelationRepository.findPatientIdsByUser(userId)).thenReturn(List.of(UUID.randomUUID()));

        var context = service.getAuthorizationContext(userId);

        assertNotNull(context);
        assertEquals("johndoe", context.username());
        assertTrue(context.roles().contains("DOCTOR"));
        assertTrue(context.permissions().contains("PATIENT_READ"));
        assertEquals(1, context.accessiblePatients().size());
        assertTrue(context.active());
    }
}
