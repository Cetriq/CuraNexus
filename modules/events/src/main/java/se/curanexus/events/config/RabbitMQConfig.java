package se.curanexus.events.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for domain events.
 * Uses a topic exchange for flexible routing based on event types.
 * Includes Dead Letter Queue (DLQ) for failed message handling.
 *
 * Message persistence features:
 * - All exchanges are durable (survive broker restart)
 * - All queues are durable (survive broker restart)
 * - Messages are persistent (delivery mode = 2)
 * - Publisher confirms enabled (acknowledgment from broker)
 */
@Configuration
public class RabbitMQConfig {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQConfig.class);

    public static final String EVENTS_EXCHANGE = "curanexus.events";
    public static final String EVENTS_QUEUE = "curanexus.events.queue";

    // Module-specific queues for targeted event consumption
    public static final String TASK_MODULE_QUEUE = "curanexus.events.task-module";

    // Dead Letter Queue configuration
    public static final String DLQ_EXCHANGE = "curanexus.events.dlx";
    public static final String DLQ_QUEUE = "curanexus.events.dlq";
    public static final String DLQ_ROUTING_KEY = "dead-letter";

    // Routing keys
    public static final String ROUTING_KEY_ENCOUNTER = "event.encounter.#";
    public static final String ROUTING_KEY_TASK = "event.task.#";
    public static final String ROUTING_KEY_JOURNAL = "event.journal.#";
    public static final String ROUTING_KEY_ALL = "event.#";

    @Bean
    public TopicExchange eventsExchange() {
        // Durable = survives broker restart, autoDelete = false
        return ExchangeBuilder.topicExchange(EVENTS_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        // Durable = survives broker restart
        return ExchangeBuilder.directExchange(DLQ_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ_QUEUE).build();
    }

    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(DLQ_ROUTING_KEY);
    }

    @Bean
    public Queue eventsQueue() {
        return QueueBuilder.durable(EVENTS_QUEUE)
                .withArgument("x-dead-letter-exchange", DLQ_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding eventsBinding(Queue eventsQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(eventsQueue).to(eventsExchange).with(ROUTING_KEY_ALL);
    }

    @Bean
    public Queue taskModuleQueue() {
        return QueueBuilder.durable(TASK_MODULE_QUEUE)
                .withArgument("x-dead-letter-exchange", DLQ_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding taskModuleBinding(Queue taskModuleQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(taskModuleQueue).to(eventsExchange).with(ROUTING_KEY_ENCOUNTER);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        // Enable publisher confirms for reliable message delivery
        if (connectionFactory instanceof CachingConnectionFactory cachingConnectionFactory) {
            cachingConnectionFactory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);
            cachingConnectionFactory.setPublisherReturns(true);
        }

        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter);
        rabbitTemplate.setExchange(EVENTS_EXCHANGE);

        // Ensure messages are persistent (delivery mode = 2)
        rabbitTemplate.setBeforePublishPostProcessors(message -> {
            message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            return message;
        });

        // Enable mandatory flag to get returns for unroutable messages
        rabbitTemplate.setMandatory(true);

        // Set up confirm callback for publisher confirms
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.debug("Message confirmed by broker: {}",
                        correlationData != null ? correlationData.getId() : "unknown");
            } else {
                log.error("Message NOT confirmed by broker: {}, cause: {}",
                        correlationData != null ? correlationData.getId() : "unknown", cause);
            }
        });

        // Set up returns callback for unroutable messages
        rabbitTemplate.setReturnsCallback(returned -> {
            log.error("Message returned: exchange={}, routingKey={}, replyCode={}, replyText={}",
                    returned.getExchange(),
                    returned.getRoutingKey(),
                    returned.getReplyCode(),
                    returned.getReplyText());
        });

        return rabbitTemplate;
    }
}
