package se.curanexus.lab.service;

import java.util.UUID;

public class LabOrderNotFoundException extends RuntimeException {

    public LabOrderNotFoundException(UUID id) {
        super("Labbeställning hittades ej: " + id);
    }

    public LabOrderNotFoundException(String message) {
        super(message);
    }
}
