package se.curanexus.notification.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import se.curanexus.events.EventMessage;
import se.curanexus.events.config.RabbitMQConfig;
import se.curanexus.notification.domain.StoredEvent;
import se.curanexus.notification.repository.StoredEventRepository;

/**
 * RabbitMQ listener for cross-service domain events.
 * Receives events from RabbitMQ and stores them in the event store.
 */
@Component
public class RabbitMQEventListener {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQEventListener.class);

    private final StoredEventRepository repository;
    private final ObjectMapper objectMapper;

    public RabbitMQEventListener(StoredEventRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    /**
     * Listen to all events from RabbitMQ and store them.
     */
    @RabbitListener(queues = RabbitMQConfig.EVENTS_QUEUE)
    @Transactional
    public void handleEventMessage(EventMessage message) {
        try {
            log.info("Received event from RabbitMQ: {} for {} {}",
                    message.getEventType(),
                    message.getAggregateType(),
                    message.getAggregateId());

            // Check if event already exists (idempotency)
            if (repository.existsById(message.getEventId())) {
                log.debug("Event {} already stored, skipping", message.getEventId());
                return;
            }

            StoredEvent storedEvent = new StoredEvent(
                    message.getEventId(),
                    message.getAggregateId(),
                    message.getAggregateType(),
                    message.getEventType(),
                    message.getPayload(),
                    message.getOccurredAt()
            );

            repository.save(storedEvent);

            log.info("Stored event from RabbitMQ: {} for aggregate {} ({})",
                    message.getEventType(),
                    message.getAggregateId(),
                    message.getAggregateType());

        } catch (Exception e) {
            log.error("Failed to process event from RabbitMQ: {} for aggregate {}",
                    message.getEventType(), message.getAggregateId(), e);
            throw e; // Re-throw to trigger retry/DLQ handling
        }
    }
}
