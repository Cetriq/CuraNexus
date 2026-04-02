package se.curanexus.authorization.service;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(UUID userId) {
        super("User not found: " + userId);
    }

    public UserNotFoundException(String identifier) {
        super("User not found: " + identifier);
    }
}
