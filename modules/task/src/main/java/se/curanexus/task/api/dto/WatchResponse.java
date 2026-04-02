package se.curanexus.task.api.dto;

import se.curanexus.task.domain.Watch;
import se.curanexus.task.domain.WatchType;

import java.time.Instant;
import java.util.UUID;

public record WatchResponse(
        UUID id,
        UUID userId,
        WatchType watchType,
        UUID targetId,
        boolean notifyOnChange,
        String note,
        boolean active,
        Instant createdAt,
        Instant lastNotifiedAt
) {
    public static WatchResponse from(Watch watch) {
        return new WatchResponse(
                watch.getId(),
                watch.getUserId(),
                watch.getWatchType(),
                watch.getTargetId(),
                watch.isNotifyOnChange(),
                watch.getNote(),
                watch.isActive(),
                watch.getCreatedAt(),
                watch.getLastNotifiedAt()
        );
    }
}
