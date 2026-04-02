package se.curanexus.authorization.service;

import java.util.UUID;

public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String message) {
        super(message);
    }

    public AccessDeniedException(UUID userId, String resource, String action) {
        super("User " + userId + " does not have permission to " + action + " " + resource);
    }

    public AccessDeniedException(UUID userId, UUID patientId) {
        super("User " + userId + " does not have an active care relation with patient " + patientId);
    }
}
