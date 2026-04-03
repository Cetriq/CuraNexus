package se.curanexus.booking.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * En bokningsbar tidslucka i ett schema.
 * Representerar en specifik tid som kan bokas av en patient.
 */
@Entity
@Table(name = "time_slots", indexes = {
        @Index(name = "idx_slot_schedule", columnList = "schedule_id"),
        @Index(name = "idx_slot_start_time", columnList = "start_time"),
        @Index(name = "idx_slot_status", columnList = "status"),
        @Index(name = "idx_slot_practitioner", columnList = "practitioner_id")
})
public class TimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @Column(name = "practitioner_id")
    private UUID practitionerId;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SlotStatus status = SlotStatus.AVAILABLE;

    @OneToOne(mappedBy = "timeSlot", fetch = FetchType.LAZY)
    private Appointment appointment;

    @Column(name = "service_type")
    private String serviceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "appointment_type")
    private AppointmentType appointmentType;

    @Column(name = "overbookable")
    private boolean overbookable = false;

    @Column(name = "max_overbook")
    private int maxOverbook = 0;

    @Column(name = "current_bookings")
    private int currentBookings = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    protected TimeSlot() {
    }

    public TimeSlot(Schedule schedule, LocalDateTime startTime, LocalDateTime endTime) {
        this.schedule = schedule;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationMinutes = (int) java.time.Duration.between(startTime, endTime).toMinutes();
        this.status = SlotStatus.AVAILABLE;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // Business methods

    public boolean isAvailable() {
        if (status == SlotStatus.BLOCKED) {
            return false;
        }
        if (status == SlotStatus.AVAILABLE) {
            return true;
        }
        // If booked but overbookable
        return overbookable && currentBookings < maxOverbook;
    }

    public void book(Appointment appointment) {
        if (!isAvailable()) {
            throw new IllegalStateException("Time slot is not available for booking");
        }
        this.appointment = appointment;
        this.currentBookings++;
        if (!overbookable || currentBookings >= maxOverbook) {
            this.status = SlotStatus.BOOKED;
        }
    }

    public void release() {
        this.appointment = null;
        this.currentBookings = Math.max(0, this.currentBookings - 1);
        if (currentBookings == 0) {
            this.status = SlotStatus.AVAILABLE;
        }
    }

    public void block(String reason) {
        if (status == SlotStatus.BOOKED) {
            throw new IllegalStateException("Cannot block a booked slot");
        }
        this.status = SlotStatus.BLOCKED;
    }

    public void unblock() {
        if (status == SlotStatus.BLOCKED) {
            this.status = SlotStatus.AVAILABLE;
        }
    }

    public boolean isPast() {
        return endTime.isBefore(LocalDateTime.now());
    }

    public boolean isToday() {
        return startTime.toLocalDate().equals(java.time.LocalDate.now());
    }

    // Getters and setters

    public UUID getId() {
        return id;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public UUID getPractitionerId() {
        return practitionerId;
    }

    public void setPractitionerId(UUID practitionerId) {
        this.practitionerId = practitionerId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public SlotStatus getStatus() {
        return status;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public AppointmentType getAppointmentType() {
        return appointmentType;
    }

    public void setAppointmentType(AppointmentType appointmentType) {
        this.appointmentType = appointmentType;
    }

    public boolean isOverbookable() {
        return overbookable;
    }

    public void setOverbookable(boolean overbookable) {
        this.overbookable = overbookable;
    }

    public int getMaxOverbook() {
        return maxOverbook;
    }

    public void setMaxOverbook(int maxOverbook) {
        this.maxOverbook = maxOverbook;
    }

    public int getCurrentBookings() {
        return currentBookings;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public enum SlotStatus {
        AVAILABLE,
        BOOKED,
        BLOCKED
    }
}
