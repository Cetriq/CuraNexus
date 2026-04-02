package se.curanexus.task.api.dto;

import jakarta.validation.constraints.NotNull;
import se.curanexus.task.domain.WatchType;

import java.util.UUID;

public record CreateWatchRequest(
        @NotNull UUID userId,
        @NotNull WatchType watchType,
        @NotNull UUID targetId,
        boolean notifyOnChange,
        String note
) {}
