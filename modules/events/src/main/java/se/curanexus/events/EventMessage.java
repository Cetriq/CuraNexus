package se.curanexus.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Serializable event message for RabbitMQ transport.
 * Wraps domain event data for cross-service communication.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventMessage implements Serializable {

    private UUID eventId;
    private UUID aggregateId;
    private String aggregateType;
    private String eventType;
    private Instant occurredAt;
    private String payload;

    public EventMessage() {
    }

    public EventMessage(UUID eventId, UUID aggregateId, String aggregateType,
                        String eventType, Instant occurredAt, String payload) {
        this.eventId = eventId;
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.eventType = eventType;
        this.occurredAt = occurredAt;
        this.payload = payload;
    }

    public static EventMessage from(DomainEvent event, String payload) {
        return new EventMessage(
                event.getEventId(),
                event.getAggregateId(),
                event.getAggregateType(),
                event.getEventType(),
                event.getOccurredAt(),
                payload
        );
    }

    /**
     * Get the routing key for this event.
     * Format: event.{aggregateType}.{eventType}
     */
    public String getRoutingKey() {
        return String.format("event.%s.%s",
                aggregateType.toLowerCase(),
                eventType.toLowerCase());
    }

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public UUID getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(UUID aggregateId) {
        this.aggregateId = aggregateId;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public void setAggregateType(String aggregateType) {
        this.aggregateType = aggregateType;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
