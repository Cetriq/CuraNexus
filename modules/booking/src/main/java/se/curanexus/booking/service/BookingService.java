package se.curanexus.booking.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.curanexus.booking.api.dto.*;
import se.curanexus.booking.domain.*;
import se.curanexus.booking.repository.AppointmentRepository;
import se.curanexus.booking.repository.TimeSlotRepository;
import se.curanexus.events.DomainEventPublisher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Bokningsservice - hanterar alla bokningsoperationer.
 */
@Service
@Transactional
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private final AppointmentRepository appointmentRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final DomainEventPublisher eventPublisher;

    public BookingService(AppointmentRepository appointmentRepository,
                          TimeSlotRepository timeSlotRepository,
                          DomainEventPublisher eventPublisher) {
        this.appointmentRepository = appointmentRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Skapa en ny bokning.
     */
    public AppointmentDto createAppointment(CreateAppointmentRequest request, UUID bookedById) {
        log.info("Creating appointment for patient {} at {}", request.patientId(), request.startTime());

        // Validate time slot availability if booking by slot
        TimeSlot slot = null;
        if (request.timeSlotId() != null) {
            slot = timeSlotRepository.findById(request.timeSlotId())
                    .orElseThrow(() -> new IllegalArgumentException("Time slot not found: " + request.timeSlotId()));

            if (!slot.isAvailable()) {
                throw new BookingConflictException("Time slot is not available");
            }
        } else {
            // Check for conflicts when booking without slot
            List<Appointment> conflicts = appointmentRepository.findConflictingAppointments(
                    request.practitionerId(),
                    request.startTime(),
                    request.endTime()
            );
            if (!conflicts.isEmpty()) {
                throw new BookingConflictException("Time conflicts with existing appointment");
            }
        }

        // Create appointment
        Appointment appointment = new Appointment(
                request.patientId(),
                request.startTime(),
                request.endTime(),
                request.appointmentType() != null ? request.appointmentType() : AppointmentType.IN_PERSON,
                bookedById
        );

        appointment.setPractitionerId(request.practitionerId());
        appointment.setPractitionerHsaId(request.practitionerHsaId());
        appointment.setUnitId(request.unitId());
        appointment.setUnitHsaId(request.unitHsaId());
        appointment.setServiceType(request.serviceType());
        appointment.setReasonText(request.reasonText());
        appointment.setReasonCode(request.reasonCode());
        appointment.setPatientInstructions(request.patientInstructions());
        appointment.setInternalNotes(request.internalNotes());

        if (slot != null) {
            appointment.setTimeSlot(slot);
            slot.book(appointment);
        }

        Appointment saved = appointmentRepository.save(appointment);
        log.info("Created appointment {} with reference {}", saved.getId(), saved.getBookingReference());

        // Publish event
        eventPublisher.publish(new AppointmentCreatedEvent(saved));

        return AppointmentDto.from(saved);
    }

    /**
     * Hämta bokning via ID.
     */
    @Transactional(readOnly = true)
    public AppointmentDto getAppointment(UUID appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .map(AppointmentDto::from)
                .orElseThrow(() -> new AppointmentNotFoundException(appointmentId));
    }

    /**
     * Hämta bokning via bokningsreferens.
     */
    @Transactional(readOnly = true)
    public AppointmentDto getAppointmentByReference(String bookingReference) {
        return appointmentRepository.findByBookingReference(bookingReference)
                .map(AppointmentDto::from)
                .orElseThrow(() -> new AppointmentNotFoundException("Booking reference not found: " + bookingReference));
    }

    /**
     * Hämta patientens bokningar.
     */
    @Transactional(readOnly = true)
    public List<AppointmentDto> getPatientAppointments(UUID patientId) {
        return appointmentRepository.findByPatientIdOrderByStartTimeDesc(patientId)
                .stream()
                .map(AppointmentDto::from)
                .toList();
    }

    /**
     * Hämta patientens kommande bokningar.
     */
    @Transactional(readOnly = true)
    public List<AppointmentDto> getUpcomingAppointments(UUID patientId) {
        return appointmentRepository.findUpcomingByPatientId(patientId, LocalDateTime.now())
                .stream()
                .map(AppointmentDto::from)
                .toList();
    }

    /**
     * Hämta vårdgivarens bokningar för en dag.
     */
    @Transactional(readOnly = true)
    public List<AppointmentDto> getPractitionerAppointments(UUID practitionerId, LocalDateTime date) {
        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        return appointmentRepository.findByPractitionerIdAndDateRange(practitionerId, startOfDay, endOfDay)
                .stream()
                .map(AppointmentDto::from)
                .toList();
    }

    /**
     * Avboka en tid.
     */
    public AppointmentDto cancelAppointment(UUID appointmentId, CancelAppointmentRequest request, UUID cancelledById) {
        log.info("Cancelling appointment {}", appointmentId);

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException(appointmentId));

        if (!appointment.isCancellable()) {
            throw new IllegalStateException("Appointment cannot be cancelled in current state: " + appointment.getStatus());
        }

        appointment.cancel(cancelledById, request.reason(), request.byPatient());
        Appointment saved = appointmentRepository.save(appointment);

        // Publish event
        eventPublisher.publish(new AppointmentCancelledEvent(saved, request.reason()));

        log.info("Cancelled appointment {}", appointmentId);
        return AppointmentDto.from(saved);
    }

    /**
     * Omboka till ny tid.
     */
    public AppointmentDto rescheduleAppointment(UUID appointmentId, RescheduleRequest request, UUID rescheduledById) {
        log.info("Rescheduling appointment {} to {}", appointmentId, request.newStartTime());

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException(appointmentId));

        TimeSlot newSlot = null;
        if (request.newTimeSlotId() != null) {
            newSlot = timeSlotRepository.findById(request.newTimeSlotId())
                    .orElseThrow(() -> new IllegalArgumentException("New time slot not found"));

            if (!newSlot.isAvailable()) {
                throw new BookingConflictException("New time slot is not available");
            }
        }

        LocalDateTime oldStartTime = appointment.getStartTime();
        appointment.reschedule(request.newStartTime(), request.newEndTime(), newSlot);
        Appointment saved = appointmentRepository.save(appointment);

        // Publish event
        eventPublisher.publish(new AppointmentRescheduledEvent(saved, oldStartTime));

        log.info("Rescheduled appointment {} from {} to {}", appointmentId, oldStartTime, request.newStartTime());
        return AppointmentDto.from(saved);
    }

    /**
     * Checka in patient.
     */
    public AppointmentDto checkIn(UUID appointmentId) {
        log.info("Checking in appointment {}", appointmentId);

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException(appointmentId));

        appointment.checkIn();
        Appointment saved = appointmentRepository.save(appointment);

        eventPublisher.publish(new AppointmentCheckedInEvent(saved));

        return AppointmentDto.from(saved);
    }

    /**
     * Markera besök som påbörjat.
     */
    public AppointmentDto startVisit(UUID appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException(appointmentId));

        appointment.startVisit();
        return AppointmentDto.from(appointmentRepository.save(appointment));
    }

    /**
     * Markera besök som avslutat.
     */
    public AppointmentDto completeVisit(UUID appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException(appointmentId));

        appointment.complete();
        Appointment saved = appointmentRepository.save(appointment);

        eventPublisher.publish(new AppointmentCompletedEvent(saved));

        return AppointmentDto.from(saved);
    }

    /**
     * Markera som utebliven.
     */
    public AppointmentDto markNoShow(UUID appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException(appointmentId));

        appointment.markNoShow();
        Appointment saved = appointmentRepository.save(appointment);

        eventPublisher.publish(new AppointmentNoShowEvent(saved));

        return AppointmentDto.from(saved);
    }

    /**
     * Koppla bokning till vårdkontakt.
     */
    public AppointmentDto linkToEncounter(UUID appointmentId, UUID encounterId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException(appointmentId));

        appointment.linkToEncounter(encounterId);
        return AppointmentDto.from(appointmentRepository.save(appointment));
    }

    /**
     * Sök bokningar.
     */
    @Transactional(readOnly = true)
    public Page<AppointmentDto> searchAppointments(AppointmentSearchRequest request, Pageable pageable) {
        return appointmentRepository.search(
                request.patientId(),
                request.practitionerId(),
                request.unitId(),
                request.status(),
                request.appointmentType(),
                request.fromDate(),
                request.toDate(),
                pageable
        ).map(AppointmentDto::from);
    }

    // Event classes (inner classes for simplicity, can be moved to separate files)

    public static class AppointmentCreatedEvent extends se.curanexus.events.DomainEvent {
        private final Appointment appointment;
        public AppointmentCreatedEvent(Appointment appointment) {
            super(appointment);
            this.appointment = appointment;
        }
        public Appointment getAppointment() { return appointment; }
        @Override public String getAggregateType() { return "APPOINTMENT"; }
        @Override public UUID getAggregateId() { return appointment.getId(); }
        @Override public String getEventType() { return "CREATED"; }
    }

    public static class AppointmentCancelledEvent extends se.curanexus.events.DomainEvent {
        private final Appointment appointment;
        private final String reason;
        public AppointmentCancelledEvent(Appointment appointment, String reason) {
            super(appointment);
            this.appointment = appointment;
            this.reason = reason;
        }
        public Appointment getAppointment() { return appointment; }
        public String getReason() { return reason; }
        @Override public String getAggregateType() { return "APPOINTMENT"; }
        @Override public UUID getAggregateId() { return appointment.getId(); }
        @Override public String getEventType() { return "CANCELLED"; }
    }

    public static class AppointmentRescheduledEvent extends se.curanexus.events.DomainEvent {
        private final Appointment appointment;
        private final LocalDateTime oldStartTime;
        public AppointmentRescheduledEvent(Appointment appointment, LocalDateTime oldStartTime) {
            super(appointment);
            this.appointment = appointment;
            this.oldStartTime = oldStartTime;
        }
        public Appointment getAppointment() { return appointment; }
        public LocalDateTime getOldStartTime() { return oldStartTime; }
        @Override public String getAggregateType() { return "APPOINTMENT"; }
        @Override public UUID getAggregateId() { return appointment.getId(); }
        @Override public String getEventType() { return "RESCHEDULED"; }
    }

    public static class AppointmentCheckedInEvent extends se.curanexus.events.DomainEvent {
        private final Appointment appointment;
        public AppointmentCheckedInEvent(Appointment appointment) {
            super(appointment);
            this.appointment = appointment;
        }
        public Appointment getAppointment() { return appointment; }
        @Override public String getAggregateType() { return "APPOINTMENT"; }
        @Override public UUID getAggregateId() { return appointment.getId(); }
        @Override public String getEventType() { return "CHECKED_IN"; }
    }

    public static class AppointmentCompletedEvent extends se.curanexus.events.DomainEvent {
        private final Appointment appointment;
        public AppointmentCompletedEvent(Appointment appointment) {
            super(appointment);
            this.appointment = appointment;
        }
        public Appointment getAppointment() { return appointment; }
        @Override public String getAggregateType() { return "APPOINTMENT"; }
        @Override public UUID getAggregateId() { return appointment.getId(); }
        @Override public String getEventType() { return "COMPLETED"; }
    }

    public static class AppointmentNoShowEvent extends se.curanexus.events.DomainEvent {
        private final Appointment appointment;
        public AppointmentNoShowEvent(Appointment appointment) {
            super(appointment);
            this.appointment = appointment;
        }
        public Appointment getAppointment() { return appointment; }
        @Override public String getAggregateType() { return "APPOINTMENT"; }
        @Override public UUID getAggregateId() { return appointment.getId(); }
        @Override public String getEventType() { return "NO_SHOW"; }
    }
}
