package se.curanexus.notification.api.dto;

import se.curanexus.notification.domain.StoredEvent;

import java.time.Instant;
import java.util.UUID;

public record StoredEventDto(
        UUID id,
        UUID eventId,
        UUID aggregateId,
        String aggregateType,
        String eventType,
        String payload,
        Instant occurredAt,
        Instant storedAt,
        boolean processed,
        Instant processedAt
) {
    public static StoredEventDto from(StoredEvent event) {
        return new StoredEventDto(
                event.getId(),
                event.getEventId(),
                event.getAggregateId(),
                event.getAggregateType(),
                event.getEventType(),
                event.getPayload(),
                event.getOccurredAt(),
                event.getStoredAt(),
                event.isProcessed(),
                event.getProcessedAt()
        );
    }
}
