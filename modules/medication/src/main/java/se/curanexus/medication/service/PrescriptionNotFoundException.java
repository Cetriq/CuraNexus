package se.curanexus.medication.service;

import java.util.UUID;

public class PrescriptionNotFoundException extends RuntimeException {

    public PrescriptionNotFoundException(UUID prescriptionId) {
        super("Ordination hittades inte: " + prescriptionId);
    }

    public PrescriptionNotFoundException(String message) {
        super(message);
    }
}
