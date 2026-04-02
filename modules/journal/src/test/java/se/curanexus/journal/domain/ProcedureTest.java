package se.curanexus.journal.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProcedureTest {

    @Test
    void shouldCreateProcedureWithPlannedStatus() {
        UUID encounterId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();

        Procedure procedure = new Procedure(encounterId, patientId, "AA123");

        assertEquals(encounterId, procedure.getEncounterId());
        assertEquals(patientId, procedure.getPatientId());
        assertEquals("AA123", procedure.getCode());
        assertEquals("KVÅ", procedure.getCodeSystem());
        assertEquals(ProcedureStatus.PLANNED, procedure.getStatus());
        assertNotNull(procedure.getCreatedAt());
    }

    @Test
    void shouldStartPlannedProcedure() {
        Procedure procedure = createTestProcedure();
        UUID performedById = UUID.randomUUID();

        procedure.start(performedById, "Dr. Surgeon");

        assertEquals(ProcedureStatus.IN_PROGRESS, procedure.getStatus());
        assertEquals(performedById, procedure.getPerformedById());
        assertEquals("Dr. Surgeon", procedure.getPerformedByName());
        assertNotNull(procedure.getPerformedAt());
        assertNotNull(procedure.getUpdatedAt());
    }

    @Test
    void shouldNotStartInProgressProcedure() {
        Procedure procedure = createTestProcedure();
        procedure.start(UUID.randomUUID(), "Dr. Surgeon");

        assertThrows(IllegalStateException.class, () ->
                procedure.start(UUID.randomUUID(), "Dr. Other"));
    }

    @Test
    void shouldCompleteInProgressProcedure() {
        Procedure procedure = createTestProcedure();
        procedure.start(UUID.randomUUID(), "Dr. Surgeon");

        procedure.complete("Successful");

        assertEquals(ProcedureStatus.COMPLETED, procedure.getStatus());
        assertEquals("Successful", procedure.getOutcome());
    }

    @Test
    void shouldNotCompletePlannedProcedure() {
        Procedure procedure = createTestProcedure();

        assertThrows(IllegalStateException.class, () ->
                procedure.complete("Successful"));
    }

    @Test
    void shouldCancelPlannedProcedure() {
        Procedure procedure = createTestProcedure();

        procedure.cancel();

        assertEquals(ProcedureStatus.CANCELLED, procedure.getStatus());
    }

    @Test
    void shouldCancelInProgressProcedure() {
        Procedure procedure = createTestProcedure();
        procedure.start(UUID.randomUUID(), "Dr. Surgeon");

        procedure.cancel();

        assertEquals(ProcedureStatus.CANCELLED, procedure.getStatus());
    }

    @Test
    void shouldNotCancelCompletedProcedure() {
        Procedure procedure = createTestProcedure();
        procedure.start(UUID.randomUUID(), "Dr. Surgeon");
        procedure.complete("Done");

        assertThrows(IllegalStateException.class, procedure::cancel);
    }

    private Procedure createTestProcedure() {
        return new Procedure(UUID.randomUUID(), UUID.randomUUID(), "AA123");
    }
}
