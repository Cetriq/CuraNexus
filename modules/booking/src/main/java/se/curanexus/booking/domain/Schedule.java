package se.curanexus.booking.domain;

import jakarta.persistence.*;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Schema för en vårdgivare eller resurs.
 * Definierar tillgängliga tider och genererar tidsluckor.
 */
@Entity
@Table(name = "schedules", indexes = {
        @Index(name = "idx_schedule_practitioner", columnList = "practitioner_id"),
        @Index(name = "idx_schedule_unit", columnList = "unit_id"),
        @Index(name = "idx_schedule_active", columnList = "active")
})
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "practitioner_id")
    private UUID practitionerId;

    @Column(name = "practitioner_hsa_id")
    private String practitionerHsaId;

    @Column(name = "unit_id")
    private UUID unitId;

    @Column(name = "unit_hsa_id")
    private String unitHsaId;

    @Column(name = "service_type")
    private String serviceType;

    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    @Column(name = "valid_to")
    private LocalDate validTo;

    @Column(name = "default_slot_duration_minutes", nullable = false)
    private int defaultSlotDurationMinutes = 30;

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ScheduleRule> rules = new ArrayList<>();

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TimeSlot> timeSlots = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    protected Schedule() {
    }

    public Schedule(String name, UUID practitionerId, LocalDate validFrom) {
        this.name = name;
        this.practitionerId = practitionerId;
        this.validFrom = validFrom;
        this.active = true;
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

    public void addRule(ScheduleRule rule) {
        rules.add(rule);
        rule.setSchedule(this);
    }

    public void removeRule(ScheduleRule rule) {
        rules.remove(rule);
        rule.setSchedule(null);
    }

    public boolean isValidForDate(LocalDate date) {
        if (!active) return false;
        if (date.isBefore(validFrom)) return false;
        if (validTo != null && date.isAfter(validTo)) return false;
        return true;
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    public void extendValidityTo(LocalDate newEndDate) {
        this.validTo = newEndDate;
    }

    // Getters and setters

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDate validFrom) {
        this.validFrom = validFrom;
    }

    public LocalDate getValidTo() {
        return validTo;
    }

    public void setValidTo(LocalDate validTo) {
        this.validTo = validTo;
    }

    public int getDefaultSlotDurationMinutes() {
        return defaultSlotDurationMinutes;
    }

    public void setDefaultSlotDurationMinutes(int defaultSlotDurationMinutes) {
        this.defaultSlotDurationMinutes = defaultSlotDurationMinutes;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ScheduleRule> getRules() {
        return rules;
    }

    public List<TimeSlot> getTimeSlots() {
        return timeSlots;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
