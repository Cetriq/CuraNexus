package se.curanexus.authorization.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    @Test
    void shouldCreateRoleWithRequiredFields() {
        Role role = new Role("Doctor", "DOCTOR");

        assertEquals("Doctor", role.getName());
        assertEquals("DOCTOR", role.getCode());
        assertFalse(role.isSystemRole());
        assertNotNull(role.getCreatedAt());
        assertTrue(role.getPermissions().isEmpty());
    }

    @Test
    void shouldAddPermission() {
        Role role = new Role("Doctor", "DOCTOR");
        Permission permission = new Permission("PATIENT_READ", "Read Patient", ResourceType.PATIENT, ActionType.READ);

        role.addPermission(permission);

        assertEquals(1, role.getPermissionCount());
        assertTrue(role.hasPermission("PATIENT_READ"));
        assertNotNull(role.getUpdatedAt());
    }

    @Test
    void shouldRemovePermission() {
        Role role = new Role("Doctor", "DOCTOR");
        Permission permission = new Permission("PATIENT_READ", "Read Patient", ResourceType.PATIENT, ActionType.READ);
        role.addPermission(permission);

        role.removePermission(permission);

        assertEquals(0, role.getPermissionCount());
        assertFalse(role.hasPermission("PATIENT_READ"));
    }

    @Test
    void shouldSetDescription() {
        Role role = new Role("Doctor", "DOCTOR");

        role.setDescription("Licensed physician with full clinical access");

        assertEquals("Licensed physician with full clinical access", role.getDescription());
        assertNotNull(role.getUpdatedAt());
    }

    @Test
    void shouldSetSystemRole() {
        Role role = new Role("Doctor", "DOCTOR");
        assertFalse(role.isSystemRole());

        role.setSystemRole(true);

        assertTrue(role.isSystemRole());
    }

    @Test
    void shouldNotAddDuplicatePermissions() {
        Role role = new Role("Doctor", "DOCTOR");
        Permission permission = new Permission("PATIENT_READ", "Read Patient", ResourceType.PATIENT, ActionType.READ);

        role.addPermission(permission);
        role.addPermission(permission);

        assertEquals(1, role.getPermissionCount());
    }

    @Test
    void shouldCheckMultiplePermissions() {
        Role role = new Role("Doctor", "DOCTOR");
        Permission read = new Permission("PATIENT_READ", "Read Patient", ResourceType.PATIENT, ActionType.READ);
        Permission create = new Permission("NOTE_CREATE", "Create Note", ResourceType.NOTE, ActionType.CREATE);

        role.addPermission(read);
        role.addPermission(create);

        assertTrue(role.hasPermission("PATIENT_READ"));
        assertTrue(role.hasPermission("NOTE_CREATE"));
        assertFalse(role.hasPermission("PATIENT_DELETE"));
        assertEquals(2, role.getPermissionCount());
    }
}
