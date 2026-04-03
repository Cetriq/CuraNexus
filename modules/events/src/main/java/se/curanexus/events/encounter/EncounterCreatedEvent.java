package se.curanexus.events.encounter;

import se.curanexus.events.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Event published when a new care encounter is created.
 */
public class EncounterCreatedEvent extends DomainEvent {

    private final UUID encounterId;
    private final UUID patientId;
    private final String encounterClass;
    private final UUID responsibleUnitId;
    private final UUID responsiblePractitionerId;
    private final Instant plannedStartTime;

    public EncounterCreatedEvent(
            Object source,
            UUID encounterId,
            UUID patientId,
            String encounterClass,
            UUID responsibleUnitId,
            UUID responsiblePractitionerId,
            Instant plannedStartTime) {
        super(source);
        this.encounterId = encounterId;
        this.patientId = patientId;
        this.encounterClass = encounterClass;
        this.responsibleUnitId = responsibleUnitId;
        this.responsiblePractitionerId = responsiblePractitionerId;
        this.plannedStartTime = plannedStartTime;
    }

    @Override
    public UUID getAggregateId() {
        return encounterId;
    }

    @Override
    public String getAggregateType() {
        return "ENCOUNTER";
    }

    @Override
    public String getEventType() {
        return "CREATED";
    }

    public UUID getEncounterId() {
        return encounterId;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public String getEncounterClass() {
        return encounterClass;
    }

    public UUID getResponsibleUnitId() {
        return responsibleUnitId;
    }

    public UUID getResponsiblePractitionerId() {
        return responsiblePractitionerId;
    }

    public Instant getPlannedStartTime() {
        return plannedStartTime;
    }
}
