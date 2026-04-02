package se.curanexus.patient.service;

import java.util.UUID;

public class PatientNotFoundException extends RuntimeException {

    public PatientNotFoundException(UUID patientId) {
        super("Patient not found with id: " + patientId);
    }

    public PatientNotFoundException(String personalIdentityNumber) {
        super("Patient not found with personal identity number: " + personalIdentityNumber);
    }
}
