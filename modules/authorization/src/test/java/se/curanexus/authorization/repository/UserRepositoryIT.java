package se.curanexus.authorization.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import se.curanexus.authorization.domain.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void shouldSaveAndFindUser() {
        User user = new User("testuser", "test@example.com", "Test", "User", UserType.INTERNAL);
        user = userRepository.save(user);

        Optional<User> found = userRepository.findById(user.getId());

        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
    }

    @Test
    void shouldFindByUsername() {
        User user = new User("johndoe", "john@example.com", "John", "Doe", UserType.INTERNAL);
        userRepository.save(user);

        Optional<User> found = userRepository.findByUsername("johndoe");

        assertTrue(found.isPresent());
        assertEquals("john@example.com", found.get().getEmail());
    }

    @Test
    void shouldFindByEmail() {
        User user = new User("janedoe", "jane@example.com", "Jane", "Doe", UserType.INTERNAL);
        userRepository.save(user);

        Optional<User> found = userRepository.findByEmail("jane@example.com");

        assertTrue(found.isPresent());
        assertEquals("janedoe", found.get().getUsername());
    }

    @Test
    void shouldFindByHsaId() {
        User user = new User("doctor1", "doctor@example.com", "Doc", "Tor", UserType.INTERNAL);
        user.setHsaId("SE1234567890");
        userRepository.save(user);

        Optional<User> found = userRepository.findByHsaId("SE1234567890");

        assertTrue(found.isPresent());
        assertEquals("doctor1", found.get().getUsername());
    }

    @Test
    void shouldFindActiveUsers() {
        User active1 = new User("active1", "active1@example.com", "Active", "One", UserType.INTERNAL);
        User active2 = new User("active2", "active2@example.com", "Active", "Two", UserType.INTERNAL);
        User inactive = new User("inactive", "inactive@example.com", "In", "Active", UserType.INTERNAL);
        inactive.deactivate();

        userRepository.save(active1);
        userRepository.save(active2);
        userRepository.save(inactive);

        List<User> activeUsers = userRepository.findByActiveTrue();

        assertEquals(2, activeUsers.size());
        assertTrue(activeUsers.stream().allMatch(User::isActive));
    }

    @Test
    void shouldFindByUserType() {
        User internal = new User("internal", "internal@example.com", "In", "Ternal", UserType.INTERNAL);
        User external = new User("external", "external@example.com", "Ex", "Ternal", UserType.EXTERNAL);

        userRepository.save(internal);
        userRepository.save(external);

        List<User> internalUsers = userRepository.findByUserType(UserType.INTERNAL);
        List<User> externalUsers = userRepository.findByUserType(UserType.EXTERNAL);

        assertEquals(1, internalUsers.size());
        assertEquals(1, externalUsers.size());
        assertEquals(UserType.INTERNAL, internalUsers.get(0).getUserType());
        assertEquals(UserType.EXTERNAL, externalUsers.get(0).getUserType());
    }

    @Test
    void shouldSearchByName() {
        User john = new User("john1", "john1@example.com", "John", "Smith", UserType.INTERNAL);
        User johnny = new User("johnny", "johnny@example.com", "Johnny", "Jones", UserType.INTERNAL);
        User jane = new User("jane", "jane@example.com", "Jane", "Doe", UserType.INTERNAL);

        userRepository.save(john);
        userRepository.save(johnny);
        userRepository.save(jane);

        List<User> results = userRepository.searchByName("john");

        assertEquals(2, results.size());
    }

    @Test
    void shouldFindByRoleCode() {
        // First, get or create a role
        Role doctorRole = roleRepository.findByCode("DOCTOR")
                .orElseGet(() -> {
                    Role role = new Role("Doctor", "DOCTOR");
                    return roleRepository.save(role);
                });

        User doctor = new User("docuser", "docuser@example.com", "Doc", "User", UserType.INTERNAL);
        doctor.addRole(doctorRole);
        userRepository.save(doctor);

        User nurse = new User("nurseuser", "nurseuser@example.com", "Nurse", "User", UserType.INTERNAL);
        userRepository.save(nurse);

        List<User> doctors = userRepository.findByRoleCode("DOCTOR");

        assertEquals(1, doctors.size());
        assertEquals("docuser", doctors.get(0).getUsername());
    }

    @Test
    void shouldCheckExistsByUsername() {
        User user = new User("existstest", "exists@example.com", "Ex", "Ists", UserType.INTERNAL);
        userRepository.save(user);

        assertTrue(userRepository.existsByUsername("existstest"));
        assertFalse(userRepository.existsByUsername("notexists"));
    }

    @Test
    void shouldCheckExistsByEmail() {
        User user = new User("emailtest", "emailtest@example.com", "Email", "Test", UserType.INTERNAL);
        userRepository.save(user);

        assertTrue(userRepository.existsByEmail("emailtest@example.com"));
        assertFalse(userRepository.existsByEmail("other@example.com"));
    }
}
