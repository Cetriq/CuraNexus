package se.curanexus.authorization.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void shouldCreateUserWithRequiredFields() {
        User user = new User("johndoe", "john@example.com", "John", "Doe", UserType.INTERNAL);

        assertEquals("johndoe", user.getUsername());
        assertEquals("john@example.com", user.getEmail());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals(UserType.INTERNAL, user.getUserType());
        assertTrue(user.isActive());
        assertNotNull(user.getCreatedAt());
        assertTrue(user.getRoles().isEmpty());
    }

    @Test
    void shouldReturnFullName() {
        User user = new User("johndoe", "john@example.com", "John", "Doe", UserType.INTERNAL);

        assertEquals("John Doe", user.getFullName());
    }

    @Test
    void shouldAddRole() {
        User user = new User("johndoe", "john@example.com", "John", "Doe", UserType.INTERNAL);
        Role role = new Role("Doctor", "DOCTOR");

        user.addRole(role);

        assertEquals(1, user.getRoles().size());
        assertTrue(user.hasRole("DOCTOR"));
    }

    @Test
    void shouldRemoveRole() {
        User user = new User("johndoe", "john@example.com", "John", "Doe", UserType.INTERNAL);
        Role role = new Role("Doctor", "DOCTOR");
        user.addRole(role);

        user.removeRole(role);

        assertEquals(0, user.getRoles().size());
        assertFalse(user.hasRole("DOCTOR"));
    }

    @Test
    void shouldCheckPermissionThroughRole() {
        User user = new User("johndoe", "john@example.com", "John", "Doe", UserType.INTERNAL);
        Role role = new Role("Doctor", "DOCTOR");
        Permission permission = new Permission("PATIENT_READ", "Read Patient", ResourceType.PATIENT, ActionType.READ);
        role.addPermission(permission);
        user.addRole(role);

        assertTrue(user.hasPermission("PATIENT_READ"));
        assertFalse(user.hasPermission("PATIENT_DELETE"));
    }

    @Test
    void shouldDeactivateUser() {
        User user = new User("johndoe", "john@example.com", "John", "Doe", UserType.INTERNAL);
        assertTrue(user.isActive());

        user.deactivate();

        assertFalse(user.isActive());
    }

    @Test
    void shouldActivateUser() {
        User user = new User("johndoe", "john@example.com", "John", "Doe", UserType.INTERNAL);
        user.deactivate();

        user.activate();

        assertTrue(user.isActive());
    }

    @Test
    void shouldSetOptionalFields() {
        User user = new User("johndoe", "john@example.com", "John", "Doe", UserType.INTERNAL);

        user.setTitle("Senior Doctor");
        user.setDepartment("Cardiology");
        user.setHsaId("SE1234567890");

        assertEquals("Senior Doctor", user.getTitle());
        assertEquals("Cardiology", user.getDepartment());
        assertEquals("SE1234567890", user.getHsaId());
    }

    @Test
    void shouldUpdateUserAndSetTimestamp() {
        User user = new User("johndoe", "john@example.com", "John", "Doe", UserType.INTERNAL);
        assertNull(user.getUpdatedAt());

        user.setFirstName("Jonathan");

        assertEquals("Jonathan", user.getFirstName());
        assertNotNull(user.getUpdatedAt());
    }
}
