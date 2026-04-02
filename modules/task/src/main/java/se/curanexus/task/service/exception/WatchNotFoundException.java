package se.curanexus.task.service.exception;

import java.util.UUID;

public class WatchNotFoundException extends RuntimeException {

    private final UUID watchId;

    public WatchNotFoundException(UUID watchId) {
        super("Watch not found: " + watchId);
        this.watchId = watchId;
    }

    public UUID getWatchId() {
        return watchId;
    }
}
