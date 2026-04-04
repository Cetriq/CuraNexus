package se.curanexus.consent.exception;

import java.util.UUID;

public class ConsentNotFoundException extends RuntimeException {

    public ConsentNotFoundException(UUID id) {
        super("Consent not found with id: " + id);
    }

    public ConsentNotFoundException(String message) {
        super(message);
    }
}
