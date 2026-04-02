package se.curanexus.authorization.service;

import java.util.UUID;

public class CareRelationNotFoundException extends RuntimeException {

    public CareRelationNotFoundException(UUID relationId) {
        super("Care relation not found: " + relationId);
    }
}
