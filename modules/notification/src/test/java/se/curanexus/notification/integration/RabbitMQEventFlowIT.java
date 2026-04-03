package se.curanexus.notification.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import se.curanexus.events.EventMessage;
import se.curanexus.events.config.RabbitMQConfig;
import se.curanexus.notification.domain.StoredEvent;
import se.curanexus.notification.repository.StoredEventRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Integration tests for RabbitMQ event flow.
 * Tests the full flow: publish event → RabbitMQ → listener → event store
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class RabbitMQEventFlowIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("curanexus_test")
            .withUsername("test")
            .withPassword("test");

    @Container
    static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:3-management-alpine")
            .withExposedPorts(5672, 15672);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // PostgreSQL properties
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        // RabbitMQ properties
        registry.add("spring.rabbitmq.host", rabbitmq::getHost);
        registry.add("spring.rabbitmq.port", rabbitmq::getAmqpPort);
        registry.add("spring.rabbitmq.username", () -> "guest");
        registry.add("spring.rabbitmq.password", () -> "guest");
    }

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private StoredEventRepository storedEventRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        storedEventRepository.deleteAll();
    }

    @Test
    void shouldReceiveAndStoreEncounterCreatedEvent() {
        // Given
        UUID eventId = UUID.randomUUID();
        UUID encounterId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        Instant occurredAt = Instant.now();

        String payload = String.format(
            "{\"encounterId\":\"%s\",\"patientId\":\"%s\",\"encounterClass\":\"OUTPATIENT\"}",
            encounterId, patientId
        );

        EventMessage eventMessage = new EventMessage(
                eventId,
                encounterId,
                "Encounter",
                "EncounterCreated",
                occurredAt,
                payload
        );

        // When - publish event to RabbitMQ
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EVENTS_EXCHANGE,
                eventMessage.getRoutingKey(),
                eventMessage
        );

        // Then - event should be stored in the database
        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Optional<StoredEvent> storedEvent = storedEventRepository.findById(eventId);
                    assertThat(storedEvent).isPresent();
                    assertThat(storedEvent.get().getAggregateId()).isEqualTo(encounterId);
                    assertThat(storedEvent.get().getAggregateType()).isEqualTo("Encounter");
                    assertThat(storedEvent.get().getEventType()).isEqualTo("EncounterCreated");
                    assertThat(storedEvent.get().getPayload()).isEqualTo(payload);
                });
    }

    @Test
    void shouldReceiveAndStoreTaskCreatedEvent() {
        // Given
        UUID eventId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        UUID encounterId = UUID.randomUUID();
        Instant occurredAt = Instant.now();

        String payload = String.format(
            "{\"taskId\":\"%s\",\"encounterId\":\"%s\",\"title\":\"Review lab results\",\"priority\":\"HIGH\"}",
            taskId, encounterId
        );

        EventMessage eventMessage = new EventMessage(
                eventId,
                taskId,
                "Task",
                "TaskCreated",
                occurredAt,
                payload
        );

        // When
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EVENTS_EXCHANGE,
                eventMessage.getRoutingKey(),
                eventMessage
        );

        // Then
        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Optional<StoredEvent> storedEvent = storedEventRepository.findById(eventId);
                    assertThat(storedEvent).isPresent();
                    assertThat(storedEvent.get().getAggregateType()).isEqualTo("Task");
                    assertThat(storedEvent.get().getEventType()).isEqualTo("TaskCreated");
                });
    }

    @Test
    void shouldReceiveAndStoreNoteCreatedEvent() {
        // Given
        UUID eventId = UUID.randomUUID();
        UUID noteId = UUID.randomUUID();
        UUID encounterId = UUID.randomUUID();
        Instant occurredAt = Instant.now();

        String payload = String.format(
            "{\"noteId\":\"%s\",\"encounterId\":\"%s\",\"noteType\":\"PROGRESS_NOTE\"}",
            noteId, encounterId
        );

        EventMessage eventMessage = new EventMessage(
                eventId,
                noteId,
                "Note",
                "NoteCreated",
                occurredAt,
                payload
        );

        // When
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EVENTS_EXCHANGE,
                eventMessage.getRoutingKey(),
                eventMessage
        );

        // Then
        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Optional<StoredEvent> storedEvent = storedEventRepository.findById(eventId);
                    assertThat(storedEvent).isPresent();
                    assertThat(storedEvent.get().getAggregateType()).isEqualTo("Note");
                    assertThat(storedEvent.get().getEventType()).isEqualTo("NoteCreated");
                });
    }

    @Test
    void shouldHandleDuplicateEventsIdempotently() {
        // Given - same event ID
        UUID eventId = UUID.randomUUID();
        UUID encounterId = UUID.randomUUID();
        Instant occurredAt = Instant.now();

        String payload1 = "{\"version\":1}";
        String payload2 = "{\"version\":2}";

        EventMessage firstMessage = new EventMessage(
                eventId, encounterId, "Encounter", "EncounterCreated", occurredAt, payload1
        );

        EventMessage duplicateMessage = new EventMessage(
                eventId, encounterId, "Encounter", "EncounterCreated", occurredAt, payload2
        );

        // When - send first event
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EVENTS_EXCHANGE,
                firstMessage.getRoutingKey(),
                firstMessage
        );

        // Wait for first event to be stored
        await().atMost(10, TimeUnit.SECONDS)
                .until(() -> storedEventRepository.findById(eventId).isPresent());

        // Send duplicate event
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EVENTS_EXCHANGE,
                duplicateMessage.getRoutingKey(),
                duplicateMessage
        );

        // Give some time for processing
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Then - should only have one event stored (first one)
        long count = storedEventRepository.count();
        assertThat(count).isEqualTo(1);

        Optional<StoredEvent> storedEvent = storedEventRepository.findById(eventId);
        assertThat(storedEvent).isPresent();
        assertThat(storedEvent.get().getPayload()).isEqualTo(payload1); // First payload, not second
    }

    @Test
    void shouldStoreMultipleEventsInOrder() {
        // Given - multiple different events
        UUID eventId1 = UUID.randomUUID();
        UUID eventId2 = UUID.randomUUID();
        UUID eventId3 = UUID.randomUUID();
        UUID encounterId = UUID.randomUUID();
        Instant occurredAt = Instant.now();

        EventMessage event1 = new EventMessage(
                eventId1, encounterId, "Encounter", "EncounterCreated", occurredAt, "{\"step\":1}"
        );
        EventMessage event2 = new EventMessage(
                eventId2, encounterId, "Encounter", "EncounterStatusChanged", occurredAt.plusMillis(100), "{\"step\":2}"
        );
        EventMessage event3 = new EventMessage(
                eventId3, encounterId, "Encounter", "EncounterCompleted", occurredAt.plusMillis(200), "{\"step\":3}"
        );

        // When
        rabbitTemplate.convertAndSend(RabbitMQConfig.EVENTS_EXCHANGE, event1.getRoutingKey(), event1);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EVENTS_EXCHANGE, event2.getRoutingKey(), event2);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EVENTS_EXCHANGE, event3.getRoutingKey(), event3);

        // Then
        await().atMost(15, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    assertThat(storedEventRepository.count()).isEqualTo(3);
                    assertThat(storedEventRepository.findById(eventId1)).isPresent();
                    assertThat(storedEventRepository.findById(eventId2)).isPresent();
                    assertThat(storedEventRepository.findById(eventId3)).isPresent();
                });
    }

    @Test
    void shouldHandleEventWithRoutingKey() {
        // Given
        UUID eventId = UUID.randomUUID();
        UUID encounterId = UUID.randomUUID();
        Instant occurredAt = Instant.now();

        EventMessage eventMessage = new EventMessage(
                eventId,
                encounterId,
                "Encounter",
                "EncounterCreated",
                occurredAt,
                "{}"
        );

        // Verify routing key format
        String routingKey = eventMessage.getRoutingKey();
        assertThat(routingKey).isEqualTo("event.encounter.encountercreated");

        // When
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EVENTS_EXCHANGE,
                routingKey,
                eventMessage
        );

        // Then
        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Optional<StoredEvent> storedEvent = storedEventRepository.findById(eventId);
                    assertThat(storedEvent).isPresent();
                });
    }
}
