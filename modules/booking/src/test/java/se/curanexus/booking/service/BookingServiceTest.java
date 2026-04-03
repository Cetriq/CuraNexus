package se.curanexus.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.curanexus.booking.api.dto.*;
import se.curanexus.booking.domain.*;
import se.curanexus.booking.repository.AppointmentRepository;
import se.curanexus.booking.repository.TimeSlotRepository;
import se.curanexus.events.DomainEvent;
import se.curanexus.events.DomainEventPublisher;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingService")
class BookingServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private TimeSlotRepository timeSlotRepository;

    @Mock
    private DomainEventPublisher eventPublisher;

    @InjectMocks
    private BookingService bookingService;

    private UUID patientId;
    private UUID practitionerId;
    private UUID unitId;
    private UUID bookedById;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @BeforeEach
    void setUp() {
        patientId = UUID.randomUUID();
        practitionerId = UUID.randomUUID();
        unitId = UUID.randomUUID();
        bookedById = UUID.randomUUID();
        startTime = LocalDateTime.now().plusDays(1);
        endTime = startTime.plusMinutes(30);
    }

    @Nested
    @DisplayName("Skapa bokning")
    class CreateAppointment {

        @Test
        @DisplayName("Ska skapa bokning utan tidslucka")
        void shouldCreateAppointmentWithoutTimeSlot() {
            CreateAppointmentRequest request = new CreateAppointmentRequest(
                    patientId, practitionerId, "HSA-1234", unitId, "HSA-UNIT-1",
                    startTime, endTime, AppointmentType.IN_PERSON, null,
                    "Läkarbesök", "Huvudvärk", "R51", "Ta med ID", null
            );

            when(appointmentRepository.findConflictingAppointments(any(), any(), any()))
                    .thenReturn(Collections.emptyList());
            when(appointmentRepository.save(any(Appointment.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            AppointmentDto result = bookingService.createAppointment(request, bookedById);

            assertNotNull(result);
            assertEquals(patientId, result.patientId());
            assertEquals(practitionerId, result.practitionerId());
            assertEquals(AppointmentStatus.BOOKED, result.status());

            verify(appointmentRepository).findConflictingAppointments(practitionerId, startTime, endTime);
            verify(appointmentRepository).save(any(Appointment.class));
            verify(eventPublisher).publish(any(BookingService.AppointmentCreatedEvent.class));
        }

        @Test
        @DisplayName("Ska skapa bokning med tidslucka")
        void shouldCreateAppointmentWithTimeSlot() {
            UUID slotId = UUID.randomUUID();
            TimeSlot timeSlot = new TimeSlot(null, startTime, endTime);

            CreateAppointmentRequest request = new CreateAppointmentRequest(
                    patientId, practitionerId, "HSA-1234", unitId, "HSA-UNIT-1",
                    startTime, endTime, AppointmentType.IN_PERSON, slotId,
                    "Läkarbesök", "Huvudvärk", "R51", "Ta med ID", null
            );

            when(timeSlotRepository.findById(slotId)).thenReturn(Optional.of(timeSlot));
            when(appointmentRepository.save(any(Appointment.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            AppointmentDto result = bookingService.createAppointment(request, bookedById);

            assertNotNull(result);
            verify(timeSlotRepository).findById(slotId);
            verify(eventPublisher).publish(any(BookingService.AppointmentCreatedEvent.class));
        }

        @Test
        @DisplayName("Ska kasta exception vid tidkonflikt")
        void shouldThrowOnTimeConflict() {
            CreateAppointmentRequest request = new CreateAppointmentRequest(
                    patientId, practitionerId, "HSA-1234", unitId, "HSA-UNIT-1",
                    startTime, endTime, AppointmentType.IN_PERSON, null,
                    "Läkarbesök", "Huvudvärk", "R51", "Ta med ID", null
            );

            Appointment existingAppointment = new Appointment(
                    UUID.randomUUID(), startTime, endTime, AppointmentType.IN_PERSON, bookedById
            );

            when(appointmentRepository.findConflictingAppointments(any(), any(), any()))
                    .thenReturn(List.of(existingAppointment));

            assertThrows(BookingConflictException.class, () ->
                    bookingService.createAppointment(request, bookedById));

            verify(appointmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Ska kasta exception om tidslucka inte finns")
        void shouldThrowIfTimeSlotNotFound() {
            UUID slotId = UUID.randomUUID();

            CreateAppointmentRequest request = new CreateAppointmentRequest(
                    patientId, practitionerId, "HSA-1234", unitId, "HSA-UNIT-1",
                    startTime, endTime, AppointmentType.IN_PERSON, slotId,
                    "Läkarbesök", "Huvudvärk", "R51", "Ta med ID", null
            );

            when(timeSlotRepository.findById(slotId)).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () ->
                    bookingService.createAppointment(request, bookedById));
        }
    }

    @Nested
    @DisplayName("Avboka")
    class CancelAppointment {

        @Test
        @DisplayName("Ska avboka befintlig bokning")
        void shouldCancelAppointment() {
            UUID appointmentId = UUID.randomUUID();
            Appointment appointment = new Appointment(
                    patientId, startTime, endTime, AppointmentType.IN_PERSON, bookedById
            );

            CancelAppointmentRequest request = new CancelAppointmentRequest("Sjuk", true);

            when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
            when(appointmentRepository.save(any(Appointment.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            AppointmentDto result = bookingService.cancelAppointment(appointmentId, request, bookedById);

            assertEquals(AppointmentStatus.CANCELLED, result.status());
            verify(eventPublisher).publish(any(BookingService.AppointmentCancelledEvent.class));
        }

        @Test
        @DisplayName("Ska kasta exception om bokning inte finns")
        void shouldThrowIfAppointmentNotFound() {
            UUID appointmentId = UUID.randomUUID();
            CancelAppointmentRequest request = new CancelAppointmentRequest("Sjuk", true);

            when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.empty());

            assertThrows(AppointmentNotFoundException.class, () ->
                    bookingService.cancelAppointment(appointmentId, request, bookedById));
        }
    }

    @Nested
    @DisplayName("Omboka")
    class RescheduleAppointment {

        @Test
        @DisplayName("Ska omboka till ny tid")
        void shouldRescheduleAppointment() {
            UUID appointmentId = UUID.randomUUID();
            Appointment appointment = new Appointment(
                    patientId, startTime, endTime, AppointmentType.IN_PERSON, bookedById
            );

            LocalDateTime newStart = startTime.plusDays(7);
            LocalDateTime newEnd = newStart.plusMinutes(30);
            RescheduleRequest request = new RescheduleRequest(newStart, newEnd, null);

            when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
            when(appointmentRepository.save(any(Appointment.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            AppointmentDto result = bookingService.rescheduleAppointment(appointmentId, request, bookedById);

            assertEquals(newStart, result.startTime());
            assertEquals(newEnd, result.endTime());
            verify(eventPublisher).publish(any(BookingService.AppointmentRescheduledEvent.class));
        }
    }

    @Nested
    @DisplayName("Besöksflöde")
    class VisitFlow {

        private UUID appointmentId;
        private Appointment appointment;

        @BeforeEach
        void setUp() {
            appointmentId = UUID.randomUUID();
            appointment = new Appointment(
                    patientId, startTime, endTime, AppointmentType.IN_PERSON, bookedById
            );
        }

        @Test
        @DisplayName("Ska checka in patient")
        void shouldCheckInPatient() {
            when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
            when(appointmentRepository.save(any(Appointment.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            AppointmentDto result = bookingService.checkIn(appointmentId);

            assertEquals(AppointmentStatus.CHECKED_IN, result.status());
            assertNotNull(result.checkedInAt());
            verify(eventPublisher).publish(any(BookingService.AppointmentCheckedInEvent.class));
        }

        @Test
        @DisplayName("Ska markera besök som avslutat")
        void shouldCompleteVisit() {
            appointment.checkIn();
            appointment.startVisit();

            when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
            when(appointmentRepository.save(any(Appointment.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            AppointmentDto result = bookingService.completeVisit(appointmentId);

            assertEquals(AppointmentStatus.COMPLETED, result.status());
            verify(eventPublisher).publish(any(BookingService.AppointmentCompletedEvent.class));
        }

        @Test
        @DisplayName("Ska markera som utebliven")
        void shouldMarkNoShow() {
            when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
            when(appointmentRepository.save(any(Appointment.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            AppointmentDto result = bookingService.markNoShow(appointmentId);

            assertEquals(AppointmentStatus.NO_SHOW, result.status());
            verify(eventPublisher).publish(any(BookingService.AppointmentNoShowEvent.class));
        }
    }

    @Nested
    @DisplayName("Hämta bokningar")
    class GetAppointments {

        @Test
        @DisplayName("Ska hämta bokning via ID")
        void shouldGetAppointmentById() {
            UUID appointmentId = UUID.randomUUID();
            Appointment appointment = new Appointment(
                    patientId, startTime, endTime, AppointmentType.IN_PERSON, bookedById
            );

            when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));

            AppointmentDto result = bookingService.getAppointment(appointmentId);

            assertNotNull(result);
            assertEquals(patientId, result.patientId());
        }

        @Test
        @DisplayName("Ska hämta bokning via referens")
        void shouldGetAppointmentByReference() {
            String ref = "CNB1234-20260403";
            Appointment appointment = new Appointment(
                    patientId, startTime, endTime, AppointmentType.IN_PERSON, bookedById
            );

            when(appointmentRepository.findByBookingReference(ref)).thenReturn(Optional.of(appointment));

            AppointmentDto result = bookingService.getAppointmentByReference(ref);

            assertNotNull(result);
        }

        @Test
        @DisplayName("Ska hämta patientens bokningar")
        void shouldGetPatientAppointments() {
            Appointment appointment = new Appointment(
                    patientId, startTime, endTime, AppointmentType.IN_PERSON, bookedById
            );

            when(appointmentRepository.findByPatientIdOrderByStartTimeDesc(patientId))
                    .thenReturn(List.of(appointment));

            List<AppointmentDto> results = bookingService.getPatientAppointments(patientId);

            assertEquals(1, results.size());
            assertEquals(patientId, results.get(0).patientId());
        }
    }
}
