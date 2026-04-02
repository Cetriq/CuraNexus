package se.curanexus.encounter.service;

import java.util.UUID;

public class EncounterNotFoundException extends RuntimeException {

    public EncounterNotFoundException(UUID encounterId) {
        super("Encounter not found with id: " + encounterId);
    }
}
