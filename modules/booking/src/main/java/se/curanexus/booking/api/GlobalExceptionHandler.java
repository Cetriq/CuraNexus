package se.curanexus.booking.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import se.curanexus.booking.service.*;

import java.net.URI;
import java.time.Instant;

/**
 * Global exception handler för bokningsmodulen.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AppointmentNotFoundException.class)
    public ProblemDetail handleAppointmentNotFound(AppointmentNotFoundException ex) {
        log.warn("Appointment not found: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setType(URI.create("https://curanexus.se/errors/appointment-not-found"));
        problem.setTitle("Bokning hittades inte");
        problem.setProperty("timestamp", Instant.now());
        if (ex.getAppointmentId() != null) {
            problem.setProperty("appointmentId", ex.getAppointmentId());
        }
        return problem;
    }

    @ExceptionHandler(BookingConflictException.class)
    public ProblemDetail handleBookingConflict(BookingConflictException ex) {
        log.warn("Booking conflict: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, ex.getMessage());
        problem.setType(URI.create("https://curanexus.se/errors/booking-conflict"));
        problem.setTitle("Bokningskonflikt");
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(ScheduleNotFoundException.class)
    public ProblemDetail handleScheduleNotFound(ScheduleNotFoundException ex) {
        log.warn("Schedule not found: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setType(URI.create("https://curanexus.se/errors/schedule-not-found"));
        problem.setTitle("Schema hittades inte");
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("scheduleId", ex.getScheduleId());
        return problem;
    }

    @ExceptionHandler(TimeSlotNotFoundException.class)
    public ProblemDetail handleTimeSlotNotFound(TimeSlotNotFoundException ex) {
        log.warn("Time slot not found: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setType(URI.create("https://curanexus.se/errors/timeslot-not-found"));
        problem.setTitle("Tidslucka hittades inte");
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("slotId", ex.getSlotId());
        return problem;
    }

    @ExceptionHandler(WaitlistEntryNotFoundException.class)
    public ProblemDetail handleWaitlistEntryNotFound(WaitlistEntryNotFoundException ex) {
        log.warn("Waitlist entry not found: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setType(URI.create("https://curanexus.se/errors/waitlist-entry-not-found"));
        problem.setTitle("Väntelistpost hittades inte");
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("entryId", ex.getEntryId());
        return problem;
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalState(IllegalStateException ex) {
        log.warn("Illegal state: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setType(URI.create("https://curanexus.se/errors/illegal-state"));
        problem.setTitle("Ogiltig operation");
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setType(URI.create("https://curanexus.se/errors/illegal-argument"));
        problem.setTitle("Ogiltigt argument");
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException ex) {
        log.warn("Validation error: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Valideringsfel i request");
        problem.setType(URI.create("https://curanexus.se/errors/validation-error"));
        problem.setTitle("Valideringsfel");
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("errors", ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .toList());
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "Ett oväntat fel uppstod");
        problem.setType(URI.create("https://curanexus.se/errors/internal-error"));
        problem.setTitle("Internt fel");
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}
