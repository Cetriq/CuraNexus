package se.curanexus.task.api;

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
import se.curanexus.task.api.dto.*;
import se.curanexus.task.domain.*;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class TaskControllerIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("curanexus_test")
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
    void shouldCreateAndGetTask() throws Exception {
        CreateTaskRequest request = new CreateTaskRequest(
                "Review lab results",
                "Check patient's blood work",
                TaskCategory.LAB,
                TaskPriority.HIGH,
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                UUID.randomUUID(),
                LocalDateTime.now().plusHours(2),
                null,
                null
        );

        MvcResult result = mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Review lab results"))
                .andExpect(jsonPath("$.category").value("LAB"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andReturn();

        TaskResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), TaskResponse.class);

        mockMvc.perform(get("/api/v1/tasks/{taskId}", response.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.id().toString()));
    }

    @Test
    void shouldAssignAndStartTask() throws Exception {
        CreateTaskRequest createRequest = new CreateTaskRequest(
                "Test task", null, TaskCategory.CLINICAL, TaskPriority.NORMAL,
                null, null, null, UUID.randomUUID(), null, null, null
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        TaskResponse created = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), TaskResponse.class);

        AssignTaskRequest assignRequest = new AssignTaskRequest(UUID.randomUUID(), null);

        mockMvc.perform(post("/api/v1/tasks/{taskId}/assign", created.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ASSIGNED"));

        mockMvc.perform(post("/api/v1/tasks/{taskId}/start", created.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.startedAt").exists());
    }

    @Test
    void shouldCompleteTask() throws Exception {
        CreateTaskRequest createRequest = new CreateTaskRequest(
                "Complete me", null, TaskCategory.CLINICAL, TaskPriority.NORMAL,
                null, null, null, UUID.randomUUID(), null, null, null
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        TaskResponse created = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), TaskResponse.class);

        CompleteTaskRequest completeRequest = new CompleteTaskRequest("All done", "Success");

        mockMvc.perform(post("/api/v1/tasks/{taskId}/complete", created.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(completeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.completionNote").value("All done"))
                .andExpect(jsonPath("$.outcome").value("Success"));
    }

    @Test
    void shouldReturnNotFoundForNonExistentTask() throws Exception {
        mockMvc.perform(get("/api/v1/tasks/{taskId}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Task Not Found"));
    }

    @Test
    void shouldCreateReminder() throws Exception {
        CreateReminderRequest request = new CreateReminderRequest(
                UUID.randomUUID(),
                "Follow up with patient",
                LocalDateTime.now().plusHours(1),
                UUID.randomUUID(),
                null,
                null,
                false,
                null
        );

        mockMvc.perform(post("/api/v1/reminders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.message").value("Follow up with patient"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void shouldCreateDelegation() throws Exception {
        CreateDelegationRequest request = new CreateDelegationRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(7),
                "All tasks",
                "Vacation coverage"
        );

        mockMvc.perform(post("/api/v1/delegations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.scope").value("All tasks"));
    }

    @Test
    void shouldCreateWatch() throws Exception {
        CreateWatchRequest request = new CreateWatchRequest(
                UUID.randomUUID(),
                WatchType.PATIENT,
                UUID.randomUUID(),
                true,
                "Monitor for discharge"
        );

        mockMvc.perform(post("/api/v1/watches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.watchType").value("PATIENT"))
                .andExpect(jsonPath("$.active").value(true));
    }
}
