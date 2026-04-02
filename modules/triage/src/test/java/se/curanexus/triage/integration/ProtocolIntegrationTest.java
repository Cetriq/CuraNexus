package se.curanexus.triage.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Protocol Integration Tests")
class ProtocolIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("List Protocols")
    class ListProtocols {

        @Test
        @DisplayName("should list all active protocols")
        void shouldListAllActiveProtocols() throws Exception {
            mockMvc.perform(get("/api/v1/triage/protocols"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(5)));
        }

        @Test
        @DisplayName("should filter protocols by category")
        void shouldFilterProtocolsByCategory() throws Exception {
            mockMvc.perform(get("/api/v1/triage/protocols")
                            .param("category", "Cardiovascular"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].category").value("Cardiovascular"));
        }

        @Test
        @DisplayName("should return empty list for non-existent category")
        void shouldReturnEmptyListForNonExistentCategory() throws Exception {
            mockMvc.perform(get("/api/v1/triage/protocols")
                            .param("category", "NonExistent"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    @Nested
    @DisplayName("Get Protocol")
    class GetProtocol {

        @Test
        @DisplayName("should return 404 for non-existent protocol")
        void shouldReturn404ForNonExistentProtocol() throws Exception {
            UUID nonExistentId = UUID.randomUUID();

            mockMvc.perform(get("/api/v1/triage/protocols/{id}", nonExistentId))
                    .andExpect(status().isNotFound());
        }
    }
}
