package se.curanexus.notification.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Persisted domain event for audit trail and event replay.
 */
@Entity
@Table(name = "stored_events", indexes = {
    @Index(name = "idx_stored_event_aggregate", columnList = "aggregate_id, aggregate_type"),
    @Index(name = "idx_stored_event_type", columnList = "event_type"),
    @Index(name = "idx_stored_event_occurred", columnList = "occurred_at"),
    @Index(name = "idx_stored_event_processed", columnList = "processed")
})
public class StoredEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "event_id", nullable = false, unique = true)
    private UUID eventId;

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    @Column(name = "aggregate_type", nullable = false, length = 50)
    private String aggregateType;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "payload", columnDefinition = "TEXT", nullable = false)
    private String payload;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "stored_at", nullable = false, updatable = false)
    private Instant storedAt;

    @Column(name = "processed", nullable = false)
    private boolean processed = false;

    @Column(name = "processed_at")
    private Instant processedAt;

    protected StoredEvent() {
    }

    public StoredEvent(UUID eventId, UUID aggregateId, String aggregateType,
                       String eventType, String payload, Instant occurredAt) {
        this.eventId = eventId;
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.eventType = eventType;
        this.payload = payload;
        this.occurredAt = occurredAt;
        this.storedAt = Instant.now();
    }

    public void markAsProcessed() {
        this.processed = true;
        this.processedAt = Instant.now();
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public UUID getEventId() {
        return eventId;
    }

    public UUID getAggregateId() {
        return aggregateId;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public String getEventType() {
        return eventType;
    }

    public String getPayload() {
        return payload;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public Instant getStoredAt() {
        return storedAt;
    }

    public boolean isProcessed() {
        return processed;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }
}
