package se.curanexus.notification.config;

import org.mockito.Mockito;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Test configuration that provides mock RabbitMQ beans.
 */
@TestConfiguration
public class TestRabbitMQConfig {

    @Bean
    @Primary
    public ConnectionFactory connectionFactory() {
        return Mockito.mock(ConnectionFactory.class);
    }

    @Bean
    @Primary
    public RabbitTemplate rabbitTemplate() {
        return Mockito.mock(RabbitTemplate.class);
    }

    @Bean
    @Primary
    public TopicExchange eventsExchange() {
        return new TopicExchange("test.events");
    }

    @Bean
    @Primary
    public DirectExchange deadLetterExchange() {
        return new DirectExchange("test.events.dlx");
    }

    @Bean
    @Primary
    public Queue eventsQueue() {
        return QueueBuilder.durable("test.events.queue").build();
    }

    @Bean
    @Primary
    public Queue deadLetterQueue() {
        return QueueBuilder.durable("test.events.dlq").build();
    }

    @Bean
    @Primary
    public Binding eventsBinding(Queue eventsQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(eventsQueue).to(eventsExchange).with("event.#");
    }

    @Bean
    @Primary
    public Binding deadLetterBinding(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with("dead-letter");
    }

    @Bean
    @Primary
    public MessageConverter jsonMessageConverter() {
        return Mockito.mock(MessageConverter.class);
    }
}
