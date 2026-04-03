package se.curanexus.booking.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Appointment domänentitet")
class AppointmentTest {

    private UUID patientId;
    private UUID bookedById;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @BeforeEach
    void setUp() {
        patientId = UUID.randomUUID();
        bookedById = UUID.randomUUID();
        startTime = LocalDateTime.now().plusDays(1);
        endTime = startTime.plusMinutes(30);
    }

    @Nested
    @DisplayName("Skapande")
    class Creation {

        @Test
        @DisplayName("Ska skapa bokning med korrekta värden")
        void shouldCreateWithCorrectValues() {
            Appointment appointment = new Appointment(
                    patientId, startTime, endTime, AppointmentType.IN_PERSON, bookedById
            );

            assertEquals(patientId, appointment.getPatientId());
            assertEquals(startTime, appointment.getStartTime());
            assertEquals(endTime, appointment.getEndTime());
            assertEquals(AppointmentType.IN_PERSON, appointment.getAppointmentType());
            assertEquals(bookedById, appointment.getBookedById());
            assertEquals(AppointmentStatus.BOOKED, appointment.getStatus());
            assertNotNull(appointment.getBookingReference());
        }

        @Test
        @DisplayName("Ska generera unik bokningsreferens")
        void shouldGenerateUniqueBookingReference() {
            Appointment appointment1 = new Appointment(
                    patientId, startTime, endTime, AppointmentType.IN_PERSON, bookedById
            );
            Appointment appointment2 = new Appointment(
                    patientId, startTime, endTime, AppointmentType.IN_PERSON, bookedById
            );

            assertNotEquals(appointment1.getBookingReference(), appointment2.getBookingReference());
        }

        @Test
        @DisplayName("Bokningsreferens ska ha korrekt format")
        void bookingReferenceShouldHaveCorrectFormat() {
            Appointment appointment = new Appointment(
                    patientId, startTime, endTime, AppointmentType.IN_PERSON, bookedById
            );

            String ref = appointment.getBookingReference();
            assertTrue(ref.startsWith("CNB"), "Ska börja med CNB");
            assertTrue(ref.contains("-"), "Ska innehålla bindestreck");
            assertEquals(16, ref.length(), "Ska ha korrekt längd (CNB + 4 chars + - + YYYYMMDD)");
        }
    }

    @Nested
    @DisplayName("Statusövergångar")
    class StatusTransitions {

        private Appointment appointment;

        @BeforeEach
        void setUp() {
            appointment = new Appointment(
                    patientId, startTime, endTime, AppointmentType.IN_PERSON, bookedById
            );
        }

        @Test
        @DisplayName("Ska kunna checka in")
        void shouldCheckIn() {
            appointment.checkIn();

            assertEquals(AppointmentStatus.CHECKED_IN, appointment.getStatus());
            assertNotNull(appointment.getCheckedInAt());
        }

        @Test
        @DisplayName("Ska kunna starta besök efter incheckning")
        void shouldStartVisitAfterCheckIn() {
            appointment.checkIn();
            appointment.startVisit();

            assertEquals(AppointmentStatus.IN_PROGRESS, appointment.getStatus());
        }

        @Test
        @DisplayName("Ska kunna starta besök utan incheckning (direkt från bokad)")
        void shouldStartVisitFromBooked() {
            appointment.startVisit();
            assertEquals(AppointmentStatus.IN_PROGRESS, appointment.getStatus());
        }

        @Test
        @DisplayName("Ska kunna avsluta besök")
        void shouldCompleteVisit() {
            appointment.checkIn();
            appointment.startVisit();
            appointment.complete();

            assertEquals(AppointmentStatus.COMPLETED, appointment.getStatus());
        }

        @Test
        @DisplayName("Ska kunna avboka")
        void shouldCancel() {
            UUID cancelledById = UUID.randomUUID();
            appointment.cancel(cancelledById, "Patient kan inte komma", true);

            assertEquals(AppointmentStatus.CANCELLED, appointment.getStatus());
            assertEquals(cancelledById, appointment.getCancelledById());
            assertEquals("Patient kan inte komma", appointment.getCancellationReason());
            assertTrue(appointment.isCancelledByPatient());
        }

        @Test
        @DisplayName("Ska inte kunna avboka avslutat besök")
        void shouldNotCancelCompletedVisit() {
            appointment.checkIn();
            appointment.startVisit();
            appointment.complete();

            assertFalse(appointment.isCancellable());
        }

        @Test
        @DisplayName("Ska kunna markera som utebliven")
        void shouldMarkNoShow() {
            appointment.markNoShow();

            assertEquals(AppointmentStatus.NO_SHOW, appointment.getStatus());
        }
    }

    @Nested
    @DisplayName("Ombokning")
    class Rescheduling {

        private Appointment appointment;

        @BeforeEach
        void setUp() {
            appointment = new Appointment(
                    patientId, startTime, endTime, AppointmentType.IN_PERSON, bookedById
            );
        }

        @Test
        @DisplayName("Ska kunna omboka till ny tid")
        void shouldReschedule() {
            LocalDateTime newStart = startTime.plusDays(7);
            LocalDateTime newEnd = newStart.plusMinutes(30);

            appointment.reschedule(newStart, newEnd, null);

            assertEquals(newStart, appointment.getStartTime());
            assertEquals(newEnd, appointment.getEndTime());
            assertEquals(AppointmentStatus.BOOKED, appointment.getStatus());
        }

        @Test
        @DisplayName("Ska inte kunna omboka avslutat besök")
        void shouldNotRescheduleCompletedVisit() {
            appointment.checkIn();
            appointment.startVisit();
            appointment.complete();

            LocalDateTime newStart = startTime.plusDays(7);
            LocalDateTime newEnd = newStart.plusMinutes(30);

            assertThrows(IllegalStateException.class, () ->
                    appointment.reschedule(newStart, newEnd, null));
        }
    }

    @Nested
    @DisplayName("Vårdkontakt-koppling")
    class EncounterLinking {

        @Test
        @DisplayName("Ska kunna koppla till vårdkontakt")
        void shouldLinkToEncounter() {
            Appointment appointment = new Appointment(
                    patientId, startTime, endTime, AppointmentType.IN_PERSON, bookedById
            );
            UUID encounterId = UUID.randomUUID();

            appointment.linkToEncounter(encounterId);

            assertEquals(encounterId, appointment.getEncounterId());
        }
    }
}
