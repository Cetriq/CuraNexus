package se.curanexus.events;

/**
 * Interface for domain event publishing.
 * Allows for easier mocking in tests and different implementations.
 */
public interface DomainEventPublisher {

    /**
     * Publish a domain event.
     * @param event the event to publish
     */
    void publish(DomainEvent event);
}
