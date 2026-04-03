package se.curanexus.notification.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import se.curanexus.events.encounter.EncounterCreatedEvent;
import se.curanexus.events.encounter.EncounterStatusChangedEvent;

/**
 * Listener for encounter-related domain events.
 * Handles cross-cutting concerns like notifications and follow-up actions.
 */
@Component
public class EncounterEventListener {

    private static final Logger log = LoggerFactory.getLogger(EncounterEventListener.class);

    @EventListener
    @Async
    public void onEncounterCreated(EncounterCreatedEvent event) {
        log.info("Encounter created: {} for patient {}",
                event.getEncounterId(), event.getPatientId());

        // Future: Create initial tasks, send notifications, etc.
    }

    @EventListener
    @Async
    public void onEncounterStatusChanged(EncounterStatusChangedEvent event) {
        log.info("Encounter {} status changed: {} -> {}",
                event.getEncounterId(), event.getOldStatus(), event.getNewStatus());

        if (event.wasCompleted()) {
            handleEncounterCompleted(event);
        } else if (event.wasCancelled()) {
            handleEncounterCancelled(event);
        }
    }

    private void handleEncounterCompleted(EncounterStatusChangedEvent event) {
        log.info("Encounter {} completed - triggering follow-up actions", event.getEncounterId());

        // Future implementations:
        // 1. Create documentation review task
        // 2. Send completion notification
        // 3. Trigger billing/coding workflow
    }

    private void handleEncounterCancelled(EncounterStatusChangedEvent event) {
        log.info("Encounter {} cancelled - cleaning up", event.getEncounterId());

        // Future implementations:
        // 1. Cancel related tasks
        // 2. Send cancellation notification
        // 3. Update scheduling system
    }
}
