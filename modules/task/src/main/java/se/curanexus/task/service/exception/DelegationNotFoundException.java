package se.curanexus.task.service.exception;

import java.util.UUID;

public class DelegationNotFoundException extends RuntimeException {

    private final UUID delegationId;

    public DelegationNotFoundException(UUID delegationId) {
        super("Delegation not found: " + delegationId);
        this.delegationId = delegationId;
    }

    public UUID getDelegationId() {
        return delegationId;
    }
}
