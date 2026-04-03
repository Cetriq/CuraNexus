package se.curanexus.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.curanexus.events.DomainEvent;
import se.curanexus.notification.domain.StoredEvent;
import se.curanexus.notification.repository.StoredEventRepository;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service for storing and retrieving domain events.
 * Listens to all DomainEvent subclasses and persists them.
 */
@Service
@Transactional
public class EventStoreService {

    private static final Logger log = LoggerFactory.getLogger(EventStoreService.class);

    private final StoredEventRepository repository;
    private final ObjectMapper objectMapper;

    public EventStoreService(StoredEventRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    /**
     * Listen to all domain events and store them asynchronously.
     */
    @EventListener
    @Async
    public void handleDomainEvent(DomainEvent event) {
        try {
            storeEvent(event);
            log.info("Stored event: {} for aggregate {} ({})",
                    event.getEventType(),
                    event.getAggregateId(),
                    event.getAggregateType());
        } catch (Exception e) {
            log.error("Failed to store event: {} for aggregate {}",
                    event.getEventType(), event.getAggregateId(), e);
        }
    }

    /**
     * Store a domain event.
     */
    public StoredEvent storeEvent(DomainEvent event) {
        String payload = serializeEvent(event);

        StoredEvent storedEvent = new StoredEvent(
                event.getEventId(),
                event.getAggregateId(),
                event.getAggregateType(),
                event.getEventType(),
                payload,
                event.getOccurredAt()
        );

        return repository.save(storedEvent);
    }

    /**
     * Get events by aggregate ID.
     */
    @Transactional(readOnly = true)
    public Page<StoredEvent> getEventsByAggregateId(UUID aggregateId, Pageable pageable) {
        return repository.findByAggregateIdOrderByOccurredAtDesc(aggregateId, pageable);
    }

    /**
     * Get events by aggregate type (e.g., "ENCOUNTER", "TASK").
     */
    @Transactional(readOnly = true)
    public Page<StoredEvent> getEventsByAggregateType(String aggregateType, Pageable pageable) {
        return repository.findByAggregateTypeOrderByOccurredAtDesc(aggregateType, pageable);
    }

    /**
     * Get events by event type (e.g., "CREATED", "STATUS_CHANGED").
     */
    @Transactional(readOnly = true)
    public Page<StoredEvent> getEventsByEventType(String eventType, Pageable pageable) {
        return repository.findByEventTypeOrderByOccurredAtDesc(eventType, pageable);
    }

    /**
     * Get events by aggregate type within a date range.
     */
    @Transactional(readOnly = true)
    public Page<StoredEvent> getEventsByAggregateTypeAndDateRange(
            String aggregateType, Instant fromDate, Instant toDate, Pageable pageable) {
        return repository.findByAggregateTypeAndDateRange(aggregateType, fromDate, toDate, pageable);
    }

    /**
     * Get event statistics.
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getEventStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", repository.count());
        stats.put("encounter_events", repository.countByAggregateType("ENCOUNTER"));
        stats.put("task_events", repository.countByAggregateType("TASK"));
        stats.put("note_events", repository.countByAggregateType("NOTE"));
        return stats;
    }

    private String serializeEvent(DomainEvent event) {
        try {
            // Create a simple representation of the event
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("eventId", event.getEventId().toString());
            eventData.put("aggregateId", event.getAggregateId().toString());
            eventData.put("aggregateType", event.getAggregateType());
            eventData.put("eventType", event.getEventType());
            eventData.put("occurredAt", event.getOccurredAt().toString());
            eventData.put("eventClass", event.getClass().getName());

            return objectMapper.writeValueAsString(eventData);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize event, using fallback", e);
            return String.format("{\"eventId\":\"%s\",\"aggregateId\":\"%s\",\"aggregateType\":\"%s\",\"eventType\":\"%s\"}",
                    event.getEventId(), event.getAggregateId(), event.getAggregateType(), event.getEventType());
        }
    }
}
