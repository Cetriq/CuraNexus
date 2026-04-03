package se.curanexus.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import se.curanexus.events.config.RabbitMQConfig;

/**
 * Unified event publisher that sends events both locally (Spring Events)
 * and to RabbitMQ for cross-service communication.
 */
@Service
public class EventPublisher implements DomainEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final ApplicationEventPublisher localEventPublisher;
    private final ObjectMapper objectMapper;

    public EventPublisher(RabbitTemplate rabbitTemplate,
                          ApplicationEventPublisher localEventPublisher) {
        this.rabbitTemplate = rabbitTemplate;
        this.localEventPublisher = localEventPublisher;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Publish a domain event both locally and to RabbitMQ.
     */
    @Override
    public void publish(DomainEvent event) {
        // Publish locally for in-process listeners
        localEventPublisher.publishEvent(event);

        // Publish to RabbitMQ for cross-service communication
        try {
            String payload = serializeEvent(event);
            EventMessage message = EventMessage.from(event, payload);

            // Create correlation data for publisher confirms tracking
            CorrelationData correlationData = new CorrelationData(event.getEventId().toString());

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EVENTS_EXCHANGE,
                    message.getRoutingKey(),
                    message,
                    correlationData
            );

            log.info("Published event: {} for {} {} via RabbitMQ (correlationId={})",
                    event.getEventType(),
                    event.getAggregateType(),
                    event.getAggregateId(),
                    correlationData.getId());

        } catch (Exception e) {
            log.error("Failed to publish event to RabbitMQ: {} for {} {}",
                    event.getEventType(),
                    event.getAggregateType(),
                    event.getAggregateId(),
                    e);
        }
    }

    private String serializeEvent(DomainEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize event, using basic format", e);
            return String.format("{\"eventId\":\"%s\",\"aggregateId\":\"%s\",\"aggregateType\":\"%s\",\"eventType\":\"%s\"}",
                    event.getEventId(), event.getAggregateId(), event.getAggregateType(), event.getEventType());
        }
    }
}
