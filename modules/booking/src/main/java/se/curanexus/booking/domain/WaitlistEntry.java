package se.curanexus.booking.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * En plats på väntelistan för patienter som vill ha tid men inte hittat passande.
 * Kan notifieras när tid blir tillgänglig.
 */
@Entity
@Table(name = "waitlist_entries", indexes = {
        @Index(name = "idx_waitlist_patient", columnList = "patient_id"),
        @Index(name = "idx_waitlist_practitioner", columnList = "practitioner_id"),
        @Index(name = "idx_waitlist_status", columnList = "status"),
        @Index(name = "idx_waitlist_priority", columnList = "priority")
})
public class WaitlistEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Column(name = "practitioner_id")
    private UUID practitionerId;

    @Column(name = "unit_id")
    private UUID unitId;

    @Column(name = "service_type")
    private String serviceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "appointment_type")
    private AppointmentType appointmentType;

    @Column(name = "preferred_date_from")
    private LocalDate preferredDateFrom;

    @Column(name = "preferred_date_to")
    private LocalDate preferredDateTo;

    @Column(name = "reason_text", length = 500)
    private String reasonText;

    @Column(name = "reason_code")
    private String reasonCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WaitlistStatus status = WaitlistStatus.WAITING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WaitlistPriority priority = WaitlistPriority.ROUTINE;

    @Column(name = "position_number")
    private Integer positionNumber;

    @Column(name = "created_by_id", nullable = false)
    private UUID createdById;

    @Column(name = "notified_at")
    private Instant notifiedAt;

    @Column(name = "booked_appointment_id")
    private UUID bookedAppointmentId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Version
    private Long version;

    protected WaitlistEntry() {
    }

    public WaitlistEntry(UUID patientId, UUID createdById) {
        this.patientId = patientId;
        this.createdById = createdById;
        this.status = WaitlistStatus.WAITING;
        this.priority = WaitlistPriority.ROUTINE;
    }

    public WaitlistEntry(UUID patientId, String serviceType) {
        this.patientId = patientId;
        this.serviceType = serviceType;
        this.status = WaitlistStatus.WAITING;
        this.priority = WaitlistPriority.ROUTINE;
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

    public void markNotified() {
        this.notifiedAt = Instant.now();
        this.status = WaitlistStatus.NOTIFIED;
    }

    public void markBooked(UUID appointmentId) {
        this.bookedAppointmentId = appointmentId;
        this.status = WaitlistStatus.BOOKED;
    }

    public void cancel() {
        this.status = WaitlistStatus.CANCELLED;
    }

    public void expire() {
        this.status = WaitlistStatus.EXPIRED;
    }

    public boolean isActive() {
        return status == WaitlistStatus.WAITING || status == WaitlistStatus.NOTIFIED;
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

    public UUID getUnitId() {
        return unitId;
    }

    public void setUnitId(UUID unitId) {
        this.unitId = unitId;
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

    public LocalDate getPreferredDateFrom() {
        return preferredDateFrom;
    }

    public void setPreferredDateFrom(LocalDate preferredDateFrom) {
        this.preferredDateFrom = preferredDateFrom;
    }

    public LocalDate getPreferredDateTo() {
        return preferredDateTo;
    }

    public void setPreferredDateTo(LocalDate preferredDateTo) {
        this.preferredDateTo = preferredDateTo;
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

    public WaitlistStatus getStatus() {
        return status;
    }

    public void setStatus(WaitlistStatus status) {
        this.status = status;
    }

    public void setNotifiedAt(Instant notifiedAt) {
        this.notifiedAt = notifiedAt;
    }

    public void setBookedAppointmentId(UUID appointmentId) {
        this.bookedAppointmentId = appointmentId;
    }

    public WaitlistPriority getPriority() {
        return priority;
    }

    public void setPriority(WaitlistPriority priority) {
        this.priority = priority;
    }

    public Integer getPositionNumber() {
        return positionNumber;
    }

    public void setPositionNumber(Integer positionNumber) {
        this.positionNumber = positionNumber;
    }

    public UUID getCreatedById() {
        return createdById;
    }

    public Instant getNotifiedAt() {
        return notifiedAt;
    }

    public UUID getBookedAppointmentId() {
        return bookedAppointmentId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public enum WaitlistStatus {
        WAITING,
        NOTIFIED,
        BOOKED,
        CANCELLED,
        EXPIRED
    }

    public enum WaitlistPriority {
        URGENT,
        SOON,
        ROUTINE
    }
}
