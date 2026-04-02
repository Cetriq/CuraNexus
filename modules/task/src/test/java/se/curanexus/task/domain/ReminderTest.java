package se.curanexus.task.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ReminderTest {

    @Test
    void shouldCreateReminderWithPendingStatus() {
        UUID userId = UUID.randomUUID();
        LocalDateTime remindAt = LocalDateTime.now().plusHours(1);

        Reminder reminder = new Reminder(userId, "Follow up with patient", remindAt);

        assertEquals(userId, reminder.getUserId());
        assertEquals("Follow up with patient", reminder.getMessage());
        assertEquals(remindAt, reminder.getRemindAt());
        assertEquals(ReminderStatus.PENDING, reminder.getStatus());
        assertNotNull(reminder.getCreatedAt());
    }

    @Test
    void shouldTriggerPendingReminder() {
        Reminder reminder = createTestReminder();

        reminder.trigger();

        assertEquals(ReminderStatus.TRIGGERED, reminder.getStatus());
    }

    @Test
    void shouldAcknowledgeTriggeredReminder() {
        Reminder reminder = createTestReminder();
        reminder.trigger();

        reminder.acknowledge();

        assertEquals(ReminderStatus.ACKNOWLEDGED, reminder.getStatus());
        assertNotNull(reminder.getAcknowledgedAt());
    }

    @Test
    void shouldNotAcknowledgePendingReminder() {
        Reminder reminder = createTestReminder();

        assertThrows(IllegalStateException.class, reminder::acknowledge);
    }

    @Test
    void shouldSnoozeTriggeredReminder() {
        Reminder reminder = createTestReminder();
        reminder.trigger();
        LocalDateTime snoozeUntil = LocalDateTime.now().plusMinutes(30);

        reminder.snooze(snoozeUntil);

        assertEquals(ReminderStatus.SNOOZED, reminder.getStatus());
        assertEquals(snoozeUntil, reminder.getSnoozedUntil());
        assertEquals(snoozeUntil, reminder.getRemindAt());
    }

    @Test
    void shouldNotSnoozePendingReminder() {
        Reminder reminder = createTestReminder();

        assertThrows(IllegalStateException.class, () ->
                reminder.snooze(LocalDateTime.now().plusMinutes(30)));
    }

    @Test
    void shouldCancelReminder() {
        Reminder reminder = createTestReminder();

        reminder.cancel();

        assertEquals(ReminderStatus.CANCELLED, reminder.getStatus());
    }

    @Test
    void shouldNotCancelAcknowledgedReminder() {
        Reminder reminder = createTestReminder();
        reminder.trigger();
        reminder.acknowledge();

        assertThrows(IllegalStateException.class, reminder::cancel);
    }

    @Test
    void shouldBeDueWhenTimeHasPassed() {
        Reminder reminder = new Reminder(
                UUID.randomUUID(),
                "Test",
                LocalDateTime.now().minusMinutes(5)
        );

        assertTrue(reminder.isDue());
    }

    @Test
    void shouldNotBeDueWhenTimeHasNotPassed() {
        Reminder reminder = createTestReminder();

        assertFalse(reminder.isDue());
    }

    private Reminder createTestReminder() {
        return new Reminder(
                UUID.randomUUID(),
                "Test reminder",
                LocalDateTime.now().plusHours(1)
        );
    }
}
