package se.curanexus.notification.health;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import se.curanexus.events.config.RabbitMQConfig;

/**
 * Health indicator for RabbitMQ queues.
 * Reports the status of the events queue and DLQ.
 */
@Component
public class RabbitMQQueuesHealthIndicator implements HealthIndicator {

    private static final int DLQ_WARNING_THRESHOLD = 10;
    private static final int DLQ_CRITICAL_THRESHOLD = 100;

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQQueuesHealthIndicator(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public Health health() {
        try {
            // Check events queue
            var eventsQueueInfo = rabbitTemplate.execute(channel ->
                    channel.queueDeclarePassive(RabbitMQConfig.EVENTS_QUEUE)
            );

            // Check DLQ
            var dlqInfo = rabbitTemplate.execute(channel ->
                    channel.queueDeclarePassive(RabbitMQConfig.DLQ_QUEUE)
            );

            if (eventsQueueInfo == null || dlqInfo == null) {
                return Health.down()
                        .withDetail("error", "Could not access queue information")
                        .build();
            }

            int eventsMessages = eventsQueueInfo.getMessageCount();
            int eventsConsumers = eventsQueueInfo.getConsumerCount();
            int dlqMessages = dlqInfo.getMessageCount();

            Health.Builder builder;

            // Determine health status based on DLQ message count
            if (dlqMessages >= DLQ_CRITICAL_THRESHOLD) {
                builder = Health.down()
                        .withDetail("status", "CRITICAL: Too many messages in DLQ");
            } else if (dlqMessages >= DLQ_WARNING_THRESHOLD) {
                builder = Health.status("WARNING")
                        .withDetail("status", "WARNING: Messages accumulating in DLQ");
            } else if (eventsConsumers == 0) {
                builder = Health.down()
                        .withDetail("status", "No consumers on events queue");
            } else {
                builder = Health.up()
                        .withDetail("status", "All queues healthy");
            }

            return builder
                    .withDetail("eventsQueue", RabbitMQConfig.EVENTS_QUEUE)
                    .withDetail("eventsQueueMessages", eventsMessages)
                    .withDetail("eventsQueueConsumers", eventsConsumers)
                    .withDetail("dlqQueue", RabbitMQConfig.DLQ_QUEUE)
                    .withDetail("dlqMessages", dlqMessages)
                    .withDetail("dlqWarningThreshold", DLQ_WARNING_THRESHOLD)
                    .withDetail("dlqCriticalThreshold", DLQ_CRITICAL_THRESHOLD)
                    .build();

        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", "Failed to check RabbitMQ queues: " + e.getMessage())
                    .build();
        }
    }
}
