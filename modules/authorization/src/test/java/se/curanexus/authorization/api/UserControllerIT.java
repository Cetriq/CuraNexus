package se.curanexus.authorization.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import se.curanexus.authorization.api.dto.CreateUserRequest;
import se.curanexus.authorization.api.dto.UpdateUserRequest;
import se.curanexus.authorization.api.dto.AssignRolesRequest;
import se.curanexus.authorization.domain.UserType;

import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class UserControllerIT {

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
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateUser() throws Exception {
        CreateUserRequest request = new CreateUserRequest(
                "newuser",
                "newuser@example.com",
                "New",
                "User",
                UserType.INTERNAL,
                "Doctor",
                "Cardiology",
                null
        );

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.firstName").value("New"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.userType").value("INTERNAL"))
                .andExpect(jsonPath("$.title").value("Doctor"))
                .andExpect(jsonPath("$.department").value("Cardiology"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    void shouldGetUserById() throws Exception {
        // First create a user
        CreateUserRequest createRequest = new CreateUserRequest(
                "getbyiduser",
                "getbyiduser@example.com",
                "Get",
                "ById",
                UserType.INTERNAL,
                null, null, null
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String userId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asText();

        // Then get by ID
        mockMvc.perform(get("/api/v1/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.username").value("getbyiduser"));
    }

    @Test
    void shouldGetUserByUsername() throws Exception {
        CreateUserRequest createRequest = new CreateUserRequest(
                "byusernameuser",
                "byusernameuser@example.com",
                "By",
                "Username",
                UserType.INTERNAL,
                null, null, null
        );

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/users/by-username/byusernameuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("byusernameuser"));
    }

    @Test
    void shouldUpdateUser() throws Exception {
        CreateUserRequest createRequest = new CreateUserRequest(
                "updateuser",
                "updateuser@example.com",
                "Update",
                "User",
                UserType.INTERNAL,
                null, null, null
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String userId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asText();

        UpdateUserRequest updateRequest = new UpdateUserRequest(
                "Updated",
                "UserUpdated",
                "Senior Doctor",
                "Neurology",
                null
        );

        mockMvc.perform(put("/api/v1/users/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.lastName").value("UserUpdated"))
                .andExpect(jsonPath("$.title").value("Senior Doctor"))
                .andExpect(jsonPath("$.department").value("Neurology"));
    }

    @Test
    void shouldDeactivateAndActivateUser() throws Exception {
        CreateUserRequest createRequest = new CreateUserRequest(
                "activateuser",
                "activateuser@example.com",
                "Activate",
                "User",
                UserType.INTERNAL,
                null, null, null
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String userId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asText();

        // Deactivate
        mockMvc.perform(post("/api/v1/users/" + userId + "/deactivate"))
                .andExpect(status().isNoContent());

        // Verify deactivated
        mockMvc.perform(get("/api/v1/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        // Activate
        mockMvc.perform(post("/api/v1/users/" + userId + "/activate"))
                .andExpect(status().isNoContent());

        // Verify activated
        mockMvc.perform(get("/api/v1/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldAssignRolesToUser() throws Exception {
        CreateUserRequest createRequest = new CreateUserRequest(
                "roleuser",
                "roleuser@example.com",
                "Role",
                "User",
                UserType.INTERNAL,
                null, null, null
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String userId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asText();

        // Assign DOCTOR role (seeded by migration)
        AssignRolesRequest assignRequest = new AssignRolesRequest(Set.of("DOCTOR"));

        mockMvc.perform(post("/api/v1/users/" + userId + "/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles", hasSize(1)))
                .andExpect(jsonPath("$.roles[0].code").value("DOCTOR"));
    }

    @Test
    void shouldGetAuthorizationContext() throws Exception {
        CreateUserRequest createRequest = new CreateUserRequest(
                "contextuser",
                "contextuser@example.com",
                "Context",
                "User",
                UserType.INTERNAL,
                null, null, null
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String userId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asText();

        // Assign DOCTOR role
        AssignRolesRequest assignRequest = new AssignRolesRequest(Set.of("DOCTOR"));
        mockMvc.perform(post("/api/v1/users/" + userId + "/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignRequest)))
                .andExpect(status().isOk());

        // Get context
        mockMvc.perform(get("/api/v1/users/" + userId + "/context"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.username").value("contextuser"))
                .andExpect(jsonPath("$.roles", hasItem("DOCTOR")))
                .andExpect(jsonPath("$.permissions", hasItem("PATIENT_READ")))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldReturnNotFoundForNonExistentUser() throws Exception {
        mockMvc.perform(get("/api/v1/users/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
    }

    @Test
    void shouldReturnConflictForDuplicateUsername() throws Exception {
        CreateUserRequest request1 = new CreateUserRequest(
                "duplicateuser",
                "duplicate1@example.com",
                "Duplicate",
                "One",
                UserType.INTERNAL,
                null, null, null
        );

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        CreateUserRequest request2 = new CreateUserRequest(
                "duplicateuser",
                "duplicate2@example.com",
                "Duplicate",
                "Two",
                UserType.INTERNAL,
                null, null, null
        );

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_USER"));
    }

    @Test
    void shouldReturnValidationErrorForInvalidRequest() throws Exception {
        CreateUserRequest request = new CreateUserRequest(
                "", // Invalid - blank
                "invalid-email", // Invalid - not an email
                "",
                "",
                null, // Invalid - null
                null, null, null
        );

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void shouldDeleteUser() throws Exception {
        CreateUserRequest createRequest = new CreateUserRequest(
                "deleteuser",
                "deleteuser@example.com",
                "Delete",
                "User",
                UserType.INTERNAL,
                null, null, null
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String userId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asText();

        mockMvc.perform(delete("/api/v1/users/" + userId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/users/" + userId))
                .andExpect(status().isNotFound());
    }
}
