package se.curanexus.medication.service;

import java.util.UUID;

public class DrugAllergyNotFoundException extends RuntimeException {

    public DrugAllergyNotFoundException(UUID allergyId) {
        super("Allergi hittades inte: " + allergyId);
    }

    public DrugAllergyNotFoundException(String message) {
        super(message);
    }
}
