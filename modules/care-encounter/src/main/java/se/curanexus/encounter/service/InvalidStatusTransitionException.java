package se.curanexus.encounter.service;

import se.curanexus.encounter.domain.EncounterStatus;

public class InvalidStatusTransitionException extends RuntimeException {

    public InvalidStatusTransitionException(EncounterStatus from, EncounterStatus to) {
        super("Invalid status transition from " + from + " to " + to);
    }
}
