package se.curanexus.booking.service;

import java.util.UUID;

/**
 * Exception som kastas när en bokning inte hittas.
 */
public class AppointmentNotFoundException extends RuntimeException {

    private final UUID appointmentId;
    private final String bookingReference;

    public AppointmentNotFoundException(UUID appointmentId) {
        super("Appointment not found: " + appointmentId);
        this.appointmentId = appointmentId;
        this.bookingReference = null;
    }

    public AppointmentNotFoundException(String message) {
        super(message);
        this.appointmentId = null;
        this.bookingReference = null;
    }

    public UUID getAppointmentId() {
        return appointmentId;
    }

    public String getBookingReference() {
        return bookingReference;
    }
}
