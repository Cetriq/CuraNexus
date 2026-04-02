package se.curanexus.audit.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import se.curanexus.audit.domain.AccessLog;
import se.curanexus.audit.domain.AccessType;
import se.curanexus.audit.domain.ResourceType;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("AccessLogRepository Integration Tests")
class AccessLogRepositoryIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("audit_test")
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
    private AccessLogRepository repository;

    private UUID userId;
    private UUID patientId;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        userId = UUID.randomUUID();
        patientId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should save and retrieve access log")
    void shouldSaveAndRetrieveAccessLog() {
        AccessLog log = new AccessLog(userId, patientId, ResourceType.PATIENT, AccessType.VIEW);
        log.setUsername("dr.smith");
        log.setCareRelationType("PHYSICIAN");

        AccessLog saved = repository.save(log);

        assertThat(saved.getId()).isNotNull();
        assertThat(repository.findById(saved.getId())).isPresent();
    }

    @Test
    @DisplayName("Should find logs by patient ID")
    void shouldFindLogsByPatientId() {
        AccessLog log1 = new AccessLog(userId, patientId, ResourceType.PATIENT, AccessType.VIEW);
        AccessLog log2 = new AccessLog(userId, patientId, ResourceType.JOURNAL_ENTRY, AccessType.VIEW);
        AccessLog log3 = new AccessLog(userId, UUID.randomUUID(), ResourceType.PATIENT, AccessType.VIEW);

        repository.save(log1);
        repository.save(log2);
        repository.save(log3);

        var result = repository.findByPatientId(patientId, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("Should find logs by patient ID and timestamp range")
    void shouldFindLogsByPatientIdAndTimestampRange() {
        Instant now = Instant.now();
        Instant oneHourAgo = now.minus(1, ChronoUnit.HOURS);

        AccessLog log1 = new AccessLog(userId, patientId, ResourceType.PATIENT, AccessType.VIEW);
        AccessLog log2 = new AccessLog(userId, patientId, ResourceType.JOURNAL_ENTRY, AccessType.EDIT);

        repository.save(log1);
        repository.save(log2);

        var result = repository.findByPatientIdAndTimestampBetween(patientId, oneHourAgo, now.plus(1, ChronoUnit.HOURS));

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("Should search access logs with filters")
    void shouldSearchAccessLogsWithFilters() {
        Instant now = Instant.now();
        Instant oneHourAgo = now.minus(1, ChronoUnit.HOURS);

        AccessLog log1 = new AccessLog(userId, patientId, ResourceType.PATIENT, AccessType.VIEW);
        AccessLog log2 = new AccessLog(UUID.randomUUID(), patientId, ResourceType.PATIENT, AccessType.VIEW);
        AccessLog log3 = new AccessLog(userId, UUID.randomUUID(), ResourceType.PATIENT, AccessType.VIEW);

        repository.save(log1);
        repository.save(log2);
        repository.save(log3);

        // Search by both patient and user
        var result = repository.searchAccessLogs(patientId, userId, oneHourAgo, now.plus(1, ChronoUnit.HOURS), PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Should count accesses by patient in period")
    void shouldCountAccessesByPatientInPeriod() {
        Instant now = Instant.now();
        Instant oneHourAgo = now.minus(1, ChronoUnit.HOURS);

        repository.save(new AccessLog(userId, patientId, ResourceType.PATIENT, AccessType.VIEW));
        repository.save(new AccessLog(UUID.randomUUID(), patientId, ResourceType.PATIENT, AccessType.EDIT));
        repository.save(new AccessLog(userId, UUID.randomUUID(), ResourceType.PATIENT, AccessType.VIEW));

        long count = repository.countByPatientIdAndTimestampBetween(patientId, oneHourAgo, now.plus(1, ChronoUnit.HOURS));

        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Should count distinct users accessing patient")
    void shouldCountDistinctUsersAccessingPatient() {
        Instant now = Instant.now();
        Instant oneHourAgo = now.minus(1, ChronoUnit.HOURS);
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();

        repository.save(new AccessLog(user1, patientId, ResourceType.PATIENT, AccessType.VIEW));
        repository.save(new AccessLog(user1, patientId, ResourceType.JOURNAL_ENTRY, AccessType.VIEW)); // Same user
        repository.save(new AccessLog(user2, patientId, ResourceType.PATIENT, AccessType.VIEW)); // Different user

        long count = repository.countDistinctUsersByPatientId(patientId, oneHourAgo, now.plus(1, ChronoUnit.HOURS));

        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Should get access stats by patient")
    void shouldGetAccessStatsByPatient() {
        Instant now = Instant.now();
        Instant oneHourAgo = now.minus(1, ChronoUnit.HOURS);

        AccessLog log1 = new AccessLog(userId, patientId, ResourceType.PATIENT, AccessType.VIEW);
        log1.setUsername("dr.smith");
        log1.setCareRelationType("PHYSICIAN");

        AccessLog log2 = new AccessLog(userId, patientId, ResourceType.JOURNAL_ENTRY, AccessType.VIEW);
        log2.setUsername("dr.smith");
        log2.setCareRelationType("PHYSICIAN");

        repository.save(log1);
        repository.save(log2);

        var result = repository.getAccessStatsByPatient(patientId, oneHourAgo, now.plus(1, ChronoUnit.HOURS));

        assertThat(result).isNotEmpty();
        assertThat(result.get(0)[1]).isEqualTo("dr.smith"); // username
        assertThat(result.get(0)[2]).isEqualTo(2L); // count
    }

    @Test
    @DisplayName("Should count by resource type")
    void shouldCountByResourceType() {
        Instant now = Instant.now();
        Instant oneHourAgo = now.minus(1, ChronoUnit.HOURS);

        repository.save(new AccessLog(userId, patientId, ResourceType.PATIENT, AccessType.VIEW));
        repository.save(new AccessLog(userId, patientId, ResourceType.JOURNAL_ENTRY, AccessType.VIEW));
        repository.save(new AccessLog(userId, patientId, ResourceType.JOURNAL_ENTRY, AccessType.VIEW));

        var result = repository.countByPatientIdGroupByResourceType(patientId, oneHourAgo, now.plus(1, ChronoUnit.HOURS));

        assertThat(result).hasSize(2);
    }
}
