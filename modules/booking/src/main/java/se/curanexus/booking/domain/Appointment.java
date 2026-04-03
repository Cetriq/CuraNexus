package se.curanexus.booking.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Bokad tid för patient hos vårdgivare.
 * Kopplar samman patient, vårdpersonal, tid och eventuell vårdkontakt.
 */
@Entity
@Table(name = "appointments", indexes = {
        @Index(name = "idx_appointment_patient", columnList = "patient_id"),
        @Index(name = "idx_appointment_practitioner", columnList = "practitioner_id"),
        @Index(name = "idx_appointment_start_time", columnList = "start_time"),
        @Index(name = "idx_appointment_status", columnList = "status"),
        @Index(name = "idx_appointment_slot", columnList = "time_slot_id")
})
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Column(name = "practitioner_id")
    private UUID practitionerId;

    @Column(name = "practitioner_hsa_id")
    private String practitionerHsaId;

    @Column(name = "unit_id")
    private UUID unitId;

    @Column(name = "unit_hsa_id")
    private String unitHsaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_slot_id")
    private TimeSlot timeSlot;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status = AppointmentStatus.BOOKED;

    @Enumerated(EnumType.STRING)
    @Column(name = "appointment_type", nullable = false)
    private AppointmentType appointmentType = AppointmentType.IN_PERSON;

    @Column(name = "service_type")
    private String serviceType;

    @Column(name = "reason_text", length = 500)
    private String reasonText;

    @Column(name = "reason_code")
    private String reasonCode;

    @Column(name = "encounter_id")
    private UUID encounterId;

    @Column(name = "booking_reference", unique = true)
    private String bookingReference;

    @Column(name = "patient_instructions", length = 1000)
    private String patientInstructions;

    @Column(name = "internal_notes", length = 1000)
    private String internalNotes;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @Column(name = "reminder_sent")
    private boolean reminderSent = false;

    @Column(name = "reminder_sent_at")
    private Instant reminderSentAt;

    @Column(name = "booked_by_id")
    private UUID bookedById;

    @Column(name = "booked_at", nullable = false)
    private Instant bookedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "cancelled_by_id")
    private UUID cancelledById;

    @Column(name = "cancelled_by_patient")
    private Boolean cancelledByPatient;

    @Column(name = "checked_in_at")
    private Instant checkedInAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    protected Appointment() {
    }

    public Appointment(UUID patientId, LocalDateTime startTime, LocalDateTime endTime,
                       AppointmentType appointmentType, UUID bookedById) {
        this.patientId = patientId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.appointmentType = appointmentType;
        this.bookedById = bookedById;
        this.bookedAt = Instant.now();
        this.status = AppointmentStatus.BOOKED;
        this.bookingReference = generateBookingReference();
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (bookedAt == null) {
            bookedAt = Instant.now();
        }
        if (bookingReference == null) {
            bookingReference = generateBookingReference();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    private String generateBookingReference() {
        // Format: CNBXXXX-YYYYMMDD (CN = CuraNexus Booking, random 4 chars, date)
        String random = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        String date = java.time.LocalDate.now().toString().replace("-", "");
        return "CNB" + random + "-" + date;
    }

    // Business methods

    public void cancel(UUID cancelledById, String reason, boolean byPatient) {
        if (status == AppointmentStatus.COMPLETED || status == AppointmentStatus.IN_PROGRESS) {
            throw new IllegalStateException("Cannot cancel appointment that is completed or in progress");
        }
        this.status = AppointmentStatus.CANCELLED;
        this.cancelledById = cancelledById;
        this.cancelledAt = Instant.now();
        this.cancellationReason = reason;
        this.cancelledByPatient = byPatient;

        // Release the time slot if one was booked
        if (timeSlot != null) {
            timeSlot.release();
        }
    }

    public void checkIn() {
        if (status != AppointmentStatus.BOOKED) {
            throw new IllegalStateException("Can only check in to booked appointments");
        }
        this.status = AppointmentStatus.CHECKED_IN;
        this.checkedInAt = Instant.now();
    }

    public void startVisit() {
        if (status != AppointmentStatus.CHECKED_IN && status != AppointmentStatus.BOOKED) {
            throw new IllegalStateException("Cannot start visit from status: " + status);
        }
        this.status = AppointmentStatus.IN_PROGRESS;
    }

    public void complete() {
        if (status != AppointmentStatus.IN_PROGRESS && status != AppointmentStatus.CHECKED_IN) {
            throw new IllegalStateException("Cannot complete appointment from status: " + status);
        }
        this.status = AppointmentStatus.COMPLETED;
    }

    public void markNoShow() {
        if (status != AppointmentStatus.BOOKED) {
            throw new IllegalStateException("Can only mark no-show for booked appointments");
        }
        this.status = AppointmentStatus.NO_SHOW;
    }

    public void reschedule(LocalDateTime newStartTime, LocalDateTime newEndTime, TimeSlot newSlot) {
        if (status == AppointmentStatus.COMPLETED || status == AppointmentStatus.IN_PROGRESS) {
            throw new IllegalStateException("Cannot reschedule completed or in-progress appointments");
        }

        // Release old slot
        if (this.timeSlot != null) {
            this.timeSlot.release();
        }

        this.startTime = newStartTime;
        this.endTime = newEndTime;
        this.timeSlot = newSlot;
        this.status = AppointmentStatus.BOOKED;

        // Book new slot
        if (newSlot != null) {
            newSlot.book(this);
        }
    }

    public void markReminderSent() {
        this.reminderSent = true;
        this.reminderSentAt = Instant.now();
    }

    public void linkToEncounter(UUID encounterId) {
        this.encounterId = encounterId;
    }

    public boolean isCancellable() {
        return status == AppointmentStatus.BOOKED;
    }

    public boolean isUpcoming() {
        return status == AppointmentStatus.BOOKED && startTime.isAfter(LocalDateTime.now());
    }

    public boolean isPast() {
        return endTime.isBefore(LocalDateTime.now());
    }

    // Getters and setters

    public UUID getId() {
        return id;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public UUID getPractitionerId() {
        return practitionerId;
    }

    public void setPractitionerId(UUID practitionerId) {
        this.practitionerId = practitionerId;
    }

    public String getPractitionerHsaId() {
        return practitionerHsaId;
    }

    public void setPractitionerHsaId(String practitionerHsaId) {
        this.practitionerHsaId = practitionerHsaId;
    }

    public UUID getUnitId() {
        return unitId;
    }

    public void setUnitId(UUID unitId) {
        this.unitId = unitId;
    }

    public String getUnitHsaId() {
        return unitHsaId;
    }

    public void setUnitHsaId(String unitHsaId) {
        this.unitHsaId = unitHsaId;
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(TimeSlot timeSlot) {
        this.timeSlot = timeSlot;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public AppointmentType getAppointmentType() {
        return appointmentType;
    }

    public void setAppointmentType(AppointmentType appointmentType) {
        this.appointmentType = appointmentType;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getReasonText() {
        return reasonText;
    }

    public void setReasonText(String reasonText) {
        this.reasonText = reasonText;
    }

    public String getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(String reasonCode) {
        this.reasonCode = reasonCode;
    }

    public UUID getEncounterId() {
        return encounterId;
    }

    public String getBookingReference() {
        return bookingReference;
    }

    public String getPatientInstructions() {
        return patientInstructions;
    }

    public void setPatientInstructions(String patientInstructions) {
        this.patientInstructions = patientInstructions;
    }

    public String getInternalNotes() {
        return internalNotes;
    }

    public void setInternalNotes(String internalNotes) {
        this.internalNotes = internalNotes;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public boolean isReminderSent() {
        return reminderSent;
    }

    public Instant getReminderSentAt() {
        return reminderSentAt;
    }

    public UUID getBookedById() {
        return bookedById;
    }

    public Instant getBookedAt() {
        return bookedAt;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public UUID getCancelledById() {
        return cancelledById;
    }

    public Instant getCheckedInAt() {
        return checkedInAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Boolean isCancelledByPatient() {
        return cancelledByPatient;
    }
}
