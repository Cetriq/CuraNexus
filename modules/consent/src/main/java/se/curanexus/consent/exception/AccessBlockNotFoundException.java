package se.curanexus.consent.exception;

import java.util.UUID;

public class AccessBlockNotFoundException extends RuntimeException {

    public AccessBlockNotFoundException(UUID id) {
        super("Access block not found with id: " + id);
    }

    public AccessBlockNotFoundException(String message) {
        super(message);
    }
}
