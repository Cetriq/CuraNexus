package se.curanexus.medication.service;

import java.util.UUID;

public class MedicationAdministrationNotFoundException extends RuntimeException {

    public MedicationAdministrationNotFoundException(UUID administrationId) {
        super("Administrering hittades inte: " + administrationId);
    }

    public MedicationAdministrationNotFoundException(String message) {
        super(message);
    }
}
