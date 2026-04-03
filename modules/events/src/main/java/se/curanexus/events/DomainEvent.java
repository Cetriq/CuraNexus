package se.curanexus.events;

import org.springframework.context.ApplicationEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Base class for all domain events in CuraNexus.
 * Extends Spring's ApplicationEvent for seamless integration with Spring's event system.
 */
public abstract class DomainEvent extends ApplicationEvent {

    private final UUID eventId;
    private final Instant occurredAt;

    protected DomainEvent(Object source) {
        super(source);
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
    }

    /**
     * Unique identifier for this event instance.
     */
    public UUID getEventId() {
        return eventId;
    }

    /**
     * Timestamp when this event occurred.
     */
    public Instant getOccurredAt() {
        return occurredAt;
    }

    /**
     * The aggregate ID this event relates to.
     */
    public abstract UUID getAggregateId();

    /**
     * The type of aggregate (e.g., "ENCOUNTER", "TASK", "NOTE").
     */
    public abstract String getAggregateType();

    /**
     * The type of this event (e.g., "CREATED", "STATUS_CHANGED").
     */
    public abstract String getEventType();
}
