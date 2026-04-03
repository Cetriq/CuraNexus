package se.curanexus.notification.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import se.curanexus.events.task.TaskCompletedEvent;
import se.curanexus.events.task.TaskCreatedEvent;

/**
 * Listener for task-related domain events.
 */
@Component
public class TaskEventListener {

    private static final Logger log = LoggerFactory.getLogger(TaskEventListener.class);

    @EventListener
    @Async
    public void onTaskCreated(TaskCreatedEvent event) {
        log.info("Task created: {} (category: {}, priority: {})",
                event.getTaskId(), event.getCategory(), event.getPriority());

        // Future: Send assignment notification if assignee is set
        if (event.getAssigneeId() != null) {
            log.debug("Task {} assigned to user {}", event.getTaskId(), event.getAssigneeId());
            // sendAssignmentNotification(event);
        }
    }

    @EventListener
    @Async
    public void onTaskCompleted(TaskCompletedEvent event) {
        log.info("Task {} completed by user {}",
                event.getTaskId(), event.getCompletedById());

        // Future implementations:
        // 1. Update encounter progress
        // 2. Trigger next workflow step
        // 3. Send completion notification
    }
}
