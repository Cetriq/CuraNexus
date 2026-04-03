package se.curanexus.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import se.curanexus.events.encounter.EncounterCreatedEvent;
import se.curanexus.notification.config.TestRabbitMQConfig;
import se.curanexus.notification.domain.StoredEvent;
import se.curanexus.notification.repository.StoredEventRepository;
import se.curanexus.notification.service.EventStoreService;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestRabbitMQConfig.class)
class EventStoreServiceTest {

    @Autowired
    private EventStoreService eventStoreService;

    @Autowired
    private StoredEventRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void shouldStoreEncounterCreatedEvent() {
        // Given
        UUID encounterId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        EncounterCreatedEvent event = new EncounterCreatedEvent(
                this,
                encounterId,
                patientId,
                "OUTPATIENT",
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instant.now()
        );

        // When
        StoredEvent stored = eventStoreService.storeEvent(event);

        // Then
        assertNotNull(stored.getId());
        assertEquals(event.getEventId(), stored.getEventId());
        assertEquals(encounterId, stored.getAggregateId());
        assertEquals("ENCOUNTER", stored.getAggregateType());
        assertEquals("CREATED", stored.getEventType());
        assertNotNull(stored.getPayload());
        assertFalse(stored.isProcessed());
    }

    @Test
    void shouldRetrieveEventsByAggregateId() {
        // Given
        UUID encounterId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();

        EncounterCreatedEvent event = new EncounterCreatedEvent(
                this, encounterId, patientId, "OUTPATIENT",
                null, null, Instant.now()
        );
        eventStoreService.storeEvent(event);

        // When
        Page<StoredEvent> result = eventStoreService.getEventsByAggregateId(
                encounterId, PageRequest.of(0, 10));

        // Then
        assertEquals(1, result.getTotalElements());
        assertEquals(encounterId, result.getContent().get(0).getAggregateId());
    }

    @Test
    void shouldRetrieveEventsByAggregateType() {
        // Given
        for (int i = 0; i < 3; i++) {
            EncounterCreatedEvent event = new EncounterCreatedEvent(
                    this, UUID.randomUUID(), UUID.randomUUID(), "OUTPATIENT",
                    null, null, Instant.now()
            );
            eventStoreService.storeEvent(event);
        }

        // When
        Page<StoredEvent> result = eventStoreService.getEventsByAggregateType(
                "ENCOUNTER", PageRequest.of(0, 10));

        // Then
        assertEquals(3, result.getTotalElements());
    }

    @Test
    void shouldReturnEventStatistics() {
        // Given
        EncounterCreatedEvent event1 = new EncounterCreatedEvent(
                this, UUID.randomUUID(), UUID.randomUUID(), "OUTPATIENT",
                null, null, Instant.now()
        );
        EncounterCreatedEvent event2 = new EncounterCreatedEvent(
                this, UUID.randomUUID(), UUID.randomUUID(), "INPATIENT",
                null, null, Instant.now()
        );
        eventStoreService.storeEvent(event1);
        eventStoreService.storeEvent(event2);

        // When
        Map<String, Long> stats = eventStoreService.getEventStatistics();

        // Then
        assertEquals(2L, stats.get("total"));
        assertEquals(2L, stats.get("encounter_events"));
    }
}
