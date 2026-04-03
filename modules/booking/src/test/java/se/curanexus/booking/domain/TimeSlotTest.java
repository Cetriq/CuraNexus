package se.curanexus.booking.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TimeSlot domänentitet")
class TimeSlotTest {

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Schedule schedule;

    @BeforeEach
    void setUp() {
        startTime = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0);
        endTime = startTime.plusMinutes(30);
        schedule = new Schedule();
        schedule.setPractitionerId(UUID.randomUUID());
        schedule.setName("Test Schedule");
    }

    @Nested
    @DisplayName("Skapande")
    class Creation {

        @Test
        @DisplayName("Ska skapa tidslucka med korrekta värden")
        void shouldCreateWithCorrectValues() {
            TimeSlot slot = new TimeSlot(schedule, startTime, endTime);

            assertEquals(startTime, slot.getStartTime());
            assertEquals(endTime, slot.getEndTime());
            assertEquals(TimeSlot.SlotStatus.AVAILABLE, slot.getStatus());
            assertEquals(0, slot.getCurrentBookings());
            assertEquals(0, slot.getMaxOverbook());
            assertFalse(slot.isOverbookable());
        }

        @Test
        @DisplayName("Ska vara tillgänglig vid skapande")
        void shouldBeAvailableOnCreation() {
            TimeSlot slot = new TimeSlot(schedule, startTime, endTime);

            assertTrue(slot.isAvailable());
        }
    }

    @Nested
    @DisplayName("Bokning")
    class Booking {

        private TimeSlot slot;

        @BeforeEach
        void setUp() {
            slot = new TimeSlot(schedule, startTime, endTime);
        }

        @Test
        @DisplayName("Ska kunna boka tillgänglig tid")
        void shouldBookAvailableSlot() {
            Appointment appointment = createAppointment();

            slot.book(appointment);

            assertEquals(TimeSlot.SlotStatus.BOOKED, slot.getStatus());
            assertEquals(1, slot.getCurrentBookings());
            assertFalse(slot.isAvailable());
        }

        @Test
        @DisplayName("Ska inte kunna boka blockerad tid")
        void shouldNotBookBlockedSlot() {
            slot.block("Möte");

            assertThrows(IllegalStateException.class, () -> slot.book(createAppointment()));
        }

        @Test
        @DisplayName("Ska kunna avboka och frigöra tid")
        void shouldReleaseSlot() {
            Appointment appointment = createAppointment();
            slot.book(appointment);

            slot.release();

            assertEquals(TimeSlot.SlotStatus.AVAILABLE, slot.getStatus());
            assertEquals(0, slot.getCurrentBookings());
            assertTrue(slot.isAvailable());
        }

        private Appointment createAppointment() {
            return new Appointment(
                    UUID.randomUUID(),
                    startTime,
                    endTime,
                    AppointmentType.IN_PERSON,
                    UUID.randomUUID()
            );
        }
    }

    @Nested
    @DisplayName("Överbokning")
    class Overbooking {

        private TimeSlot slot;

        @BeforeEach
        void setUp() {
            slot = new TimeSlot(schedule, startTime, endTime);
            slot.setOverbookable(true);
            slot.setMaxOverbook(3);
        }

        @Test
        @DisplayName("Ska tillåta flera bokningar med överbokning")
        void shouldAllowMultipleBookingsWithOverbooking() {
            Appointment app1 = createAppointment();
            Appointment app2 = createAppointment();

            slot.book(app1);
            assertTrue(slot.isAvailable(), "Ska fortfarande vara tillgänglig");

            slot.book(app2);
            assertEquals(2, slot.getCurrentBookings());
            assertTrue(slot.isAvailable(), "Ska fortfarande vara tillgänglig med 2/3 bokningar");
        }

        @Test
        @DisplayName("Ska markeras som bokad vid maxantal")
        void shouldBeMarkedAsBookedAtMaxCapacity() {
            for (int i = 0; i < 3; i++) {
                slot.book(createAppointment());
            }

            assertEquals(3, slot.getCurrentBookings());
            assertFalse(slot.isAvailable());
            assertEquals(TimeSlot.SlotStatus.BOOKED, slot.getStatus());
        }

        private Appointment createAppointment() {
            return new Appointment(
                    UUID.randomUUID(),
                    startTime,
                    endTime,
                    AppointmentType.IN_PERSON,
                    UUID.randomUUID()
            );
        }
    }

    @Nested
    @DisplayName("Blockering")
    class Blocking {

        private TimeSlot slot;

        @BeforeEach
        void setUp() {
            slot = new TimeSlot(schedule, startTime, endTime);
        }

        @Test
        @DisplayName("Ska kunna blockera tid")
        void shouldBlockSlot() {
            slot.block("Möte");

            assertEquals(TimeSlot.SlotStatus.BLOCKED, slot.getStatus());
            assertFalse(slot.isAvailable());
        }

        @Test
        @DisplayName("Ska kunna avblockera tid")
        void shouldUnblockSlot() {
            slot.block("Möte");
            slot.unblock();

            assertEquals(TimeSlot.SlotStatus.AVAILABLE, slot.getStatus());
            assertTrue(slot.isAvailable());
        }

        @Test
        @DisplayName("Ska inte kunna blockera redan bokad tid")
        void shouldNotBlockBookedSlot() {
            Appointment appointment = new Appointment(
                    UUID.randomUUID(),
                    startTime,
                    endTime,
                    AppointmentType.IN_PERSON,
                    UUID.randomUUID()
            );
            slot.book(appointment);

            assertThrows(IllegalStateException.class, () -> slot.block("Möte"));
        }
    }
}
