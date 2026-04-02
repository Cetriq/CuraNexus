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
import se.curanexus.audit.domain.AuditEvent;
import se.curanexus.audit.domain.AuditEventType;
import se.curanexus.audit.domain.ResourceType;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("AuditEventRepository Integration Tests")
class AuditEventRepositoryIT {

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
    private AuditEventRepository repository;

    private UUID userId;
    private UUID patientId;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        userId = UUID.randomUUID();
        patientId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should save and retrieve audit event")
    void shouldSaveAndRetrieveAuditEvent() {
        AuditEvent event = new AuditEvent(AuditEventType.CREATE, userId, ResourceType.PATIENT);
        event.setPatientId(patientId);
        event.setAction("CREATE_PATIENT");
        event.setUsername("dr.smith");

        AuditEvent saved = repository.save(event);

        assertThat(saved.getId()).isNotNull();
        assertThat(repository.findById(saved.getId())).isPresent();
    }

    @Test
    @DisplayName("Should find events by user ID")
    void shouldFindEventsByUserId() {
        AuditEvent event1 = new AuditEvent(AuditEventType.READ, userId, ResourceType.PATIENT);
        AuditEvent event2 = new AuditEvent(AuditEventType.UPDATE, userId, ResourceType.PATIENT);
        AuditEvent event3 = new AuditEvent(AuditEventType.READ, UUID.randomUUID(), ResourceType.PATIENT);

        repository.save(event1);
        repository.save(event2);
        repository.save(event3);

        var result = repository.findByUserId(userId, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("Should find events by patient ID")
    void shouldFindEventsByPatientId() {
        AuditEvent event1 = new AuditEvent(AuditEventType.READ, userId, ResourceType.JOURNAL_ENTRY);
        event1.setPatientId(patientId);
        AuditEvent event2 = new AuditEvent(AuditEventType.READ, userId, ResourceType.PATIENT);
        event2.setPatientId(patientId);
        AuditEvent event3 = new AuditEvent(AuditEventType.READ, userId, ResourceType.PATIENT);
        event3.setPatientId(UUID.randomUUID());

        repository.save(event1);
        repository.save(event2);
        repository.save(event3);

        var result = repository.findByPatientId(patientId, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("Should search events with multiple filters")
    void shouldSearchEventsWithMultipleFilters() {
        Instant now = Instant.now();
        Instant yesterday = now.minus(1, ChronoUnit.DAYS);
        Instant twoDaysAgo = now.minus(2, ChronoUnit.DAYS);

        AuditEvent event1 = new AuditEvent(AuditEventType.READ, userId, ResourceType.PATIENT);
        AuditEvent event2 = new AuditEvent(AuditEventType.UPDATE, userId, ResourceType.PATIENT);
        AuditEvent event3 = new AuditEvent(AuditEventType.READ, UUID.randomUUID(), ResourceType.PATIENT);

        repository.save(event1);
        repository.save(event2);
        repository.save(event3);

        // Search by user and event type
        var result = repository.searchEvents(
                userId, null, null, AuditEventType.READ, null, null, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEventType()).isEqualTo(AuditEventType.READ);
    }

    @Test
    @DisplayName("Should count events by user in time period")
    void shouldCountEventsByUserInTimePeriod() {
        Instant now = Instant.now();
        Instant oneHourAgo = now.minus(1, ChronoUnit.HOURS);
        Instant twoHoursAgo = now.minus(2, ChronoUnit.HOURS);

        repository.save(new AuditEvent(AuditEventType.READ, userId, ResourceType.PATIENT));
        repository.save(new AuditEvent(AuditEventType.UPDATE, userId, ResourceType.PATIENT));
        repository.save(new AuditEvent(AuditEventType.DELETE, userId, ResourceType.PATIENT));

        long count = repository.countByUserIdAndTimestampBetween(userId, twoHoursAgo, now.plus(1, ChronoUnit.HOURS));

        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("Should count distinct patients accessed by user")
    void shouldCountDistinctPatientsAccessedByUser() {
        UUID patient1 = UUID.randomUUID();
        UUID patient2 = UUID.randomUUID();
        Instant now = Instant.now();
        Instant oneHourAgo = now.minus(1, ChronoUnit.HOURS);

        AuditEvent event1 = new AuditEvent(AuditEventType.READ, userId, ResourceType.PATIENT);
        event1.setPatientId(patient1);
        AuditEvent event2 = new AuditEvent(AuditEventType.READ, userId, ResourceType.PATIENT);
        event2.setPatientId(patient1); // Same patient
        AuditEvent event3 = new AuditEvent(AuditEventType.READ, userId, ResourceType.PATIENT);
        event3.setPatientId(patient2); // Different patient

        repository.save(event1);
        repository.save(event2);
        repository.save(event3);

        long count = repository.countDistinctPatientsAccessedByUser(userId, oneHourAgo, now.plus(1, ChronoUnit.HOURS));

        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Should group events by type")
    void shouldGroupEventsByType() {
        Instant now = Instant.now();
        Instant oneHourAgo = now.minus(1, ChronoUnit.HOURS);

        repository.save(new AuditEvent(AuditEventType.READ, userId, ResourceType.PATIENT));
        repository.save(new AuditEvent(AuditEventType.READ, userId, ResourceType.PATIENT));
        repository.save(new AuditEvent(AuditEventType.UPDATE, userId, ResourceType.PATIENT));

        var result = repository.countByUserIdGroupByEventType(userId, oneHourAgo, now.plus(1, ChronoUnit.HOURS));

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("Should count total events in period")
    void shouldCountTotalEventsInPeriod() {
        Instant now = Instant.now();
        Instant oneHourAgo = now.minus(1, ChronoUnit.HOURS);

        repository.save(new AuditEvent(AuditEventType.READ, UUID.randomUUID(), ResourceType.PATIENT));
        repository.save(new AuditEvent(AuditEventType.READ, UUID.randomUUID(), ResourceType.PATIENT));
        repository.save(new AuditEvent(AuditEventType.READ, UUID.randomUUID(), ResourceType.PATIENT));

        long count = repository.countInPeriod(oneHourAgo, now.plus(1, ChronoUnit.HOURS));

        assertThat(count).isEqualTo(3);
    }
}
