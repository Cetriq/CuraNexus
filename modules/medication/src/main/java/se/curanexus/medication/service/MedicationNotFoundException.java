package se.curanexus.medication.service;

import java.util.UUID;

public class MedicationNotFoundException extends RuntimeException {

    public MedicationNotFoundException(UUID medicationId) {
        super("Läkemedel hittades inte: " + medicationId);
    }

    public MedicationNotFoundException(String message) {
        super(message);
    }
}
