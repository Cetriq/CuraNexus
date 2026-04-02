package se.curanexus.patient.service;

public class PatientAlreadyExistsException extends RuntimeException {

    public PatientAlreadyExistsException(String personalIdentityNumber) {
        super("Patient already exists with personal identity number: " + personalIdentityNumber);
    }
}
