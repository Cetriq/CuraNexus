package se.curanexus.journal.service.exception;

import java.util.UUID;

public class ProcedureNotFoundException extends RuntimeException {

    private final UUID procedureId;

    public ProcedureNotFoundException(UUID procedureId) {
        super("Procedure not found: " + procedureId);
        this.procedureId = procedureId;
    }

    public UUID getProcedureId() {
        return procedureId;
    }
}
