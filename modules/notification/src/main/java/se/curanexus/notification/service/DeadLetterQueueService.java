package se.curanexus.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import se.curanexus.events.EventMessage;
import se.curanexus.events.config.RabbitMQConfig;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for managing Dead Letter Queue (DLQ) messages.
 * Provides monitoring and reprocessing capabilities.
 */
@Service
public class DeadLetterQueueService {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterQueueService.class);

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public DeadLetterQueueService(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Get statistics about the DLQ.
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        try {
            // Get queue info using RabbitTemplate
            var queueInfo = rabbitTemplate.execute(channel -> {
                try {
                    return channel.queueDeclarePassive(RabbitMQConfig.DLQ_QUEUE);
                } catch (Exception e) {
                    return null;
                }
            });

            if (queueInfo != null) {
                stats.put("queueName", RabbitMQConfig.DLQ_QUEUE);
                stats.put("messageCount", queueInfo.getMessageCount());
                stats.put("consumerCount", queueInfo.getConsumerCount());
                stats.put("status", "active");
            } else {
                stats.put("queueName", RabbitMQConfig.DLQ_QUEUE);
                stats.put("messageCount", 0);
                stats.put("consumerCount", 0);
                stats.put("status", "not_found");
            }
        } catch (Exception e) {
            log.error("Failed to get DLQ statistics", e);
            stats.put("queueName", RabbitMQConfig.DLQ_QUEUE);
            stats.put("status", "error");
            stats.put("error", e.getMessage());
        }

        stats.put("timestamp", Instant.now().toString());
        return stats;
    }

    /**
     * Peek at messages in the DLQ without removing them.
     * Returns up to maxMessages messages.
     */
    public List<Map<String, Object>> peekMessages(int maxMessages) {
        List<Map<String, Object>> messages = new ArrayList<>();

        try {
            for (int i = 0; i < maxMessages; i++) {
                Message message = rabbitTemplate.receive(RabbitMQConfig.DLQ_QUEUE, 100);
                if (message == null) {
                    break;
                }

                Map<String, Object> messageInfo = parseMessage(message);
                messages.add(messageInfo);

                // Re-queue the message (we're just peeking)
                rabbitTemplate.send(RabbitMQConfig.DLQ_EXCHANGE, RabbitMQConfig.DLQ_ROUTING_KEY, message);
            }
        } catch (Exception e) {
            log.error("Failed to peek DLQ messages", e);
        }

        return messages;
    }

    /**
     * Reprocess a single message from DLQ by moving it back to the main queue.
     * Returns true if a message was reprocessed.
     */
    public boolean reprocessOne() {
        try {
            Message message = rabbitTemplate.receive(RabbitMQConfig.DLQ_QUEUE, 1000);
            if (message == null) {
                log.info("No messages in DLQ to reprocess");
                return false;
            }

            // Parse the message to get routing key
            String body = new String(message.getBody(), StandardCharsets.UTF_8);
            EventMessage eventMessage = objectMapper.readValue(body, EventMessage.class);

            // Send back to main exchange
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EVENTS_EXCHANGE,
                    eventMessage.getRoutingKey(),
                    eventMessage
            );

            log.info("Reprocessed DLQ message: {} for aggregate {}",
                    eventMessage.getEventType(), eventMessage.getAggregateId());
            return true;

        } catch (Exception e) {
            log.error("Failed to reprocess DLQ message", e);
            return false;
        }
    }

    /**
     * Reprocess all messages in the DLQ.
     * Returns the number of messages reprocessed.
     */
    public int reprocessAll() {
        int count = 0;
        int maxIterations = 1000; // Safety limit

        while (count < maxIterations && reprocessOne()) {
            count++;
        }

        log.info("Reprocessed {} messages from DLQ", count);
        return count;
    }

    /**
     * Purge all messages from the DLQ.
     * Returns the number of messages purged.
     */
    public int purge() {
        try {
            Integer purged = rabbitTemplate.execute(channel -> {
                try {
                    return channel.queuePurge(RabbitMQConfig.DLQ_QUEUE).getMessageCount();
                } catch (Exception e) {
                    return 0;
                }
            });

            int count = purged != null ? purged : 0;
            log.info("Purged {} messages from DLQ", count);
            return count;

        } catch (Exception e) {
            log.error("Failed to purge DLQ", e);
            return 0;
        }
    }

    private Map<String, Object> parseMessage(Message message) {
        Map<String, Object> info = new HashMap<>();
        MessageProperties props = message.getMessageProperties();

        info.put("messageId", props.getMessageId());
        info.put("timestamp", props.getTimestamp());
        info.put("receivedRoutingKey", props.getReceivedRoutingKey());

        // Get death info (x-death header)
        List<Map<String, ?>> xDeath = props.getXDeathHeader();
        if (xDeath != null && !xDeath.isEmpty()) {
            Map<String, ?> firstDeath = xDeath.get(0);
            info.put("originalQueue", firstDeath.get("queue"));
            info.put("reason", firstDeath.get("reason"));
            info.put("deathCount", firstDeath.get("count"));
            info.put("originalExchange", firstDeath.get("exchange"));
            info.put("originalRoutingKeys", firstDeath.get("routing-keys"));
        }

        // Parse body
        try {
            String body = new String(message.getBody(), StandardCharsets.UTF_8);
            EventMessage eventMessage = objectMapper.readValue(body, EventMessage.class);
            info.put("eventId", eventMessage.getEventId());
            info.put("eventType", eventMessage.getEventType());
            info.put("aggregateType", eventMessage.getAggregateType());
            info.put("aggregateId", eventMessage.getAggregateId());
            info.put("occurredAt", eventMessage.getOccurredAt());
        } catch (Exception e) {
            info.put("bodyParseError", e.getMessage());
            info.put("rawBody", new String(message.getBody(), StandardCharsets.UTF_8));
        }

        return info;
    }
}
