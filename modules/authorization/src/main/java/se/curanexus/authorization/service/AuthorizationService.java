package se.curanexus.authorization.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.curanexus.authorization.domain.*;
import se.curanexus.authorization.repository.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuthorizationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final CareRelationRepository careRelationRepository;

    public AuthorizationService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            CareRelationRepository careRelationRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.careRelationRepository = careRelationRepository;
    }

    // ========== User Management ==========

    public User createUser(String username, String email, String firstName, String lastName, UserType userType) {
        if (userRepository.existsByUsername(username)) {
            throw new DuplicateUserException("username", username);
        }
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateUserException("email", email);
        }

        User user = new User(username, email, firstName, lastName, userType);
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
    }

    @Transactional(readOnly = true)
    public User getUserByHsaId(String hsaId) {
        return userRepository.findByHsaId(hsaId)
                .orElseThrow(() -> new UserNotFoundException("HSA-ID: " + hsaId));
    }

    @Transactional(readOnly = true)
    public User getUserWithRolesAndPermissions(UUID userId) {
        return userRepository.findByIdWithRolesAndPermissions(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<User> getActiveUsers() {
        return userRepository.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public List<User> getUsersByRole(String roleCode) {
        return userRepository.findByRoleCode(roleCode);
    }

    @Transactional(readOnly = true)
    public List<User> searchUsers(String name) {
        return userRepository.searchByName(name);
    }

    public User updateUser(UUID userId, String firstName, String lastName, String title, String department) {
        User user = getUser(userId);
        if (firstName != null) user.setFirstName(firstName);
        if (lastName != null) user.setLastName(lastName);
        if (title != null) user.setTitle(title);
        if (department != null) user.setDepartment(department);
        return userRepository.save(user);
    }

    public User setUserHsaId(UUID userId, String hsaId) {
        if (hsaId != null && userRepository.existsByHsaId(hsaId)) {
            throw new DuplicateUserException("hsaId", hsaId);
        }
        User user = getUser(userId);
        user.setHsaId(hsaId);
        return userRepository.save(user);
    }

    public void activateUser(UUID userId) {
        User user = getUser(userId);
        user.activate();
        userRepository.save(user);
    }

    public void deactivateUser(UUID userId) {
        User user = getUser(userId);
        user.deactivate();
        userRepository.save(user);
    }

    public void deleteUser(UUID userId) {
        User user = getUser(userId);
        userRepository.delete(user);
    }

    // ========== Role Management ==========

    public Role createRole(String name, String code, String description) {
        if (roleRepository.existsByCode(code)) {
            throw new DuplicateRoleException(code);
        }

        Role role = new Role(name, code);
        role.setDescription(description);
        return roleRepository.save(role);
    }

    @Transactional(readOnly = true)
    public Role getRole(UUID roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException(roleId));
    }

    @Transactional(readOnly = true)
    public Role getRoleByCode(String code) {
        return roleRepository.findByCode(code)
                .orElseThrow(() -> new RoleNotFoundException(code));
    }

    @Transactional(readOnly = true)
    public Role getRoleWithPermissions(UUID roleId) {
        return roleRepository.findByIdWithPermissions(roleId)
                .orElseThrow(() -> new RoleNotFoundException(roleId));
    }

    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Role> getSystemRoles() {
        return roleRepository.findBySystemRoleTrue();
    }

    @Transactional(readOnly = true)
    public List<Role> getCustomRoles() {
        return roleRepository.findBySystemRoleFalse();
    }

    public Role updateRole(UUID roleId, String name, String description) {
        Role role = getRole(roleId);
        if (role.isSystemRole()) {
            throw new SystemRoleModificationException(role.getCode());
        }
        if (name != null) role.setName(name);
        if (description != null) role.setDescription(description);
        return roleRepository.save(role);
    }

    public void deleteRole(UUID roleId) {
        Role role = getRole(roleId);
        if (role.isSystemRole()) {
            throw new SystemRoleModificationException(role.getCode());
        }
        if (roleRepository.countUsersWithRole(roleId) > 0) {
            throw new IllegalStateException("Cannot delete role that is assigned to users");
        }
        roleRepository.delete(role);
    }

    // ========== Permission Management ==========

    @Transactional(readOnly = true)
    public Permission getPermission(UUID permissionId) {
        return permissionRepository.findById(permissionId)
                .orElseThrow(() -> new PermissionNotFoundException(permissionId));
    }

    @Transactional(readOnly = true)
    public Permission getPermissionByCode(String code) {
        return permissionRepository.findByCode(code)
                .orElseThrow(() -> new PermissionNotFoundException(code));
    }

    @Transactional(readOnly = true)
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Permission> getPermissionsByResource(ResourceType resource) {
        return permissionRepository.findByResource(resource);
    }

    @Transactional(readOnly = true)
    public Set<Permission> getUserPermissions(UUID userId) {
        return permissionRepository.findByUserId(userId);
    }

    // ========== Role-Permission Assignment ==========

    public Role addPermissionToRole(UUID roleId, UUID permissionId) {
        Role role = getRole(roleId);
        if (role.isSystemRole()) {
            throw new SystemRoleModificationException(role.getCode());
        }
        Permission permission = getPermission(permissionId);
        role.addPermission(permission);
        return roleRepository.save(role);
    }

    public Role addPermissionsToRole(UUID roleId, Set<String> permissionCodes) {
        Role role = getRole(roleId);
        if (role.isSystemRole()) {
            throw new SystemRoleModificationException(role.getCode());
        }
        List<Permission> permissions = permissionRepository.findByCodes(permissionCodes);
        permissions.forEach(role::addPermission);
        return roleRepository.save(role);
    }

    public Role removePermissionFromRole(UUID roleId, UUID permissionId) {
        Role role = getRole(roleId);
        if (role.isSystemRole()) {
            throw new SystemRoleModificationException(role.getCode());
        }
        Permission permission = getPermission(permissionId);
        role.removePermission(permission);
        return roleRepository.save(role);
    }

    // ========== User-Role Assignment ==========

    public User assignRoleToUser(UUID userId, UUID roleId) {
        User user = getUser(userId);
        Role role = getRole(roleId);
        user.addRole(role);
        return userRepository.save(user);
    }

    public User assignRolesToUser(UUID userId, Set<String> roleCodes) {
        User user = getUser(userId);
        List<Role> roles = roleRepository.findByCodes(roleCodes);
        roles.forEach(user::addRole);
        return userRepository.save(user);
    }

    public User removeRoleFromUser(UUID userId, UUID roleId) {
        User user = getUser(userId);
        Role role = getRole(roleId);
        user.removeRole(role);
        return userRepository.save(user);
    }

    // ========== Care Relations ==========

    public CareRelation createCareRelation(UUID userId, UUID patientId, CareRelationType relationType, String reason) {
        // Verify user exists
        getUser(userId);

        CareRelation relation = new CareRelation(userId, patientId, relationType);
        relation.setReason(reason);
        return careRelationRepository.save(relation);
    }

    public CareRelation createCareRelationForEncounter(UUID userId, UUID patientId, UUID encounterId, CareRelationType relationType) {
        getUser(userId);

        CareRelation relation = new CareRelation(userId, patientId, relationType);
        relation.setEncounterId(encounterId);
        return careRelationRepository.save(relation);
    }

    @Transactional(readOnly = true)
    public CareRelation getCareRelation(UUID relationId) {
        return careRelationRepository.findById(relationId)
                .orElseThrow(() -> new CareRelationNotFoundException(relationId));
    }

    @Transactional(readOnly = true)
    public List<CareRelation> getCareRelationsByUser(UUID userId) {
        return careRelationRepository.findByUserIdAndActiveTrue(userId);
    }

    @Transactional(readOnly = true)
    public List<CareRelation> getCareRelationsByPatient(UUID patientId) {
        return careRelationRepository.findByPatientIdAndActiveTrue(patientId);
    }

    @Transactional(readOnly = true)
    public List<CareRelation> getActiveCareRelationsByUser(UUID userId) {
        return careRelationRepository.findCurrentlyValidByUser(userId, LocalDateTime.now());
    }

    public CareRelation updateCareRelationValidity(UUID relationId, LocalDateTime validFrom, LocalDateTime validUntil) {
        CareRelation relation = getCareRelation(relationId);
        if (validFrom != null) relation.setValidFrom(validFrom);
        if (validUntil != null) relation.setValidUntil(validUntil);
        return careRelationRepository.save(relation);
    }

    public void endCareRelation(UUID relationId, UUID endedById) {
        CareRelation relation = getCareRelation(relationId);
        relation.end(endedById);
        careRelationRepository.save(relation);
    }

    // ========== Access Control Checks ==========

    @Transactional(readOnly = true)
    public boolean hasPermission(UUID userId, String permissionCode) {
        User user = getUserWithRolesAndPermissions(userId);
        return user.isActive() && user.hasPermission(permissionCode);
    }

    @Transactional(readOnly = true)
    public boolean hasAnyPermission(UUID userId, Set<String> permissionCodes) {
        User user = getUserWithRolesAndPermissions(userId);
        if (!user.isActive()) return false;
        return permissionCodes.stream().anyMatch(user::hasPermission);
    }

    @Transactional(readOnly = true)
    public boolean hasAllPermissions(UUID userId, Set<String> permissionCodes) {
        User user = getUserWithRolesAndPermissions(userId);
        if (!user.isActive()) return false;
        return permissionCodes.stream().allMatch(user::hasPermission);
    }

    @Transactional(readOnly = true)
    public boolean hasRole(UUID userId, String roleCode) {
        User user = getUser(userId);
        return user.isActive() && user.hasRole(roleCode);
    }

    @Transactional(readOnly = true)
    public boolean hasCareRelation(UUID userId, UUID patientId) {
        return careRelationRepository.hasActiveCareRelation(userId, patientId, LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public boolean canAccessPatient(UUID userId, UUID patientId) {
        // Check if user has patient read permission AND has care relation
        if (!hasPermission(userId, "PATIENT_READ")) {
            return false;
        }
        return hasCareRelation(userId, patientId);
    }

    @Transactional(readOnly = true)
    public void checkPermission(UUID userId, String permissionCode) {
        if (!hasPermission(userId, permissionCode)) {
            Permission permission = getPermissionByCode(permissionCode);
            throw new AccessDeniedException(userId, permission.getResource().name(), permission.getAction().name());
        }
    }

    @Transactional(readOnly = true)
    public void checkCareRelation(UUID userId, UUID patientId) {
        if (!hasCareRelation(userId, patientId)) {
            throw new AccessDeniedException(userId, patientId);
        }
    }

    @Transactional(readOnly = true)
    public void checkPatientAccess(UUID userId, UUID patientId, String permissionCode) {
        checkPermission(userId, permissionCode);
        checkCareRelation(userId, patientId);
    }

    // ========== Batch Operations ==========

    @Transactional(readOnly = true)
    public List<UUID> getAccessiblePatients(UUID userId) {
        if (!hasPermission(userId, "PATIENT_READ")) {
            return Collections.emptyList();
        }
        return careRelationRepository.findPatientIdsByUser(userId);
    }

    @Transactional(readOnly = true)
    public List<UUID> getCareProvidersForPatient(UUID patientId) {
        return careRelationRepository.findUserIdsByPatient(patientId);
    }

    public int deactivateExpiredCareRelations() {
        List<CareRelation> expired = careRelationRepository.findExpiredRelations(LocalDateTime.now());
        expired.forEach(cr -> cr.end(null));
        careRelationRepository.saveAll(expired);
        return expired.size();
    }

    // ========== Authorization Context ==========

    @Transactional(readOnly = true)
    public AuthorizationContext getAuthorizationContext(UUID userId) {
        User user = getUserWithRolesAndPermissions(userId);
        Set<String> roles = user.getRoles().stream()
                .map(Role::getCode)
                .collect(Collectors.toSet());
        Set<String> permissions = user.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(Permission::getCode)
                .collect(Collectors.toSet());
        List<UUID> accessiblePatients = getAccessiblePatients(userId);

        return new AuthorizationContext(userId, user.getUsername(), roles, permissions, accessiblePatients, user.isActive());
    }

    public record AuthorizationContext(
            UUID userId,
            String username,
            Set<String> roles,
            Set<String> permissions,
            List<UUID> accessiblePatients,
            boolean active
    ) {
        public boolean hasPermission(String permissionCode) {
            return active && permissions.contains(permissionCode);
        }

        public boolean hasRole(String roleCode) {
            return active && roles.contains(roleCode);
        }

        public boolean canAccessPatient(UUID patientId) {
            return active && accessiblePatients.contains(patientId);
        }
    }
}
