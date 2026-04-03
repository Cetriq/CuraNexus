package se.curanexus.notification.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import se.curanexus.events.journal.NoteCreatedEvent;
import se.curanexus.events.journal.NoteSignedEvent;

/**
 * Listener for journal-related domain events.
 */
@Component
public class JournalEventListener {

    private static final Logger log = LoggerFactory.getLogger(JournalEventListener.class);

    @EventListener
    @Async
    public void onNoteCreated(NoteCreatedEvent event) {
        log.info("Clinical note created: {} (type: {}) for encounter {}",
                event.getNoteId(), event.getNoteType(), event.getEncounterId());

        // Future implementations:
        // 1. Create signature reminder task
        // 2. Notify supervising physician if needed
    }

    @EventListener
    @Async
    public void onNoteSigned(NoteSignedEvent event) {
        log.info("Clinical note {} signed by {} at {}",
                event.getNoteId(), event.getSignedByName(), event.getSignedAt());

        // Future implementations:
        // 1. Complete related signature task
        // 2. Update encounter documentation status
        // 3. Trigger coding workflow
    }
}
