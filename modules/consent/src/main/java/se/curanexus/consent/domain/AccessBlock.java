package se.curanexus.consent.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Patient-initiated access block to restrict who can view their data.
 * Implements "spärr" functionality according to Swedish healthcare regulations.
 */
@Entity
@Table(name = "access_blocks")
public class AccessBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Enumerated(EnumType.STRING)
    @Column(name = "block_type", nullable = false, length = 30)
    private AccessBlockType blockType;

    /** Blocked unit ID (if UNIT type) */
    @Column(name = "blocked_unit_id")
    private UUID blockedUnitId;

    @Column(name = "blocked_unit_name", length = 200)
    private String blockedUnitName;

    /** Blocked practitioner ID (if PRACTITIONER type) */
    @Column(name = "blocked_practitioner_id")
    private UUID blockedPractitionerId;

    @Column(name = "blocked_practitioner_name", length = 200)
    private String blockedPractitionerName;

    /** Data category to block (if DATA_CATEGORY type) */
    @Column(name = "blocked_data_category", length = 100)
    private String blockedDataCategory;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    /** Valid from date */
    @Column(name = "valid_from")
    private LocalDate validFrom;

    /** Valid until date (null = indefinite) */
    @Column(name = "valid_until")
    private LocalDate validUntil;

    /** Who requested the block */
    @Column(name = "requested_by")
    private UUID requestedBy;

    @Column(name = "requested_by_name", length = 200)
    private String requestedByName;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt;

    /** Deactivation info */
    @Column(name = "deactivated_at")
    private Instant deactivatedAt;

    @Column(name = "deactivated_by")
    private UUID deactivatedBy;

    @Column(name = "deactivation_reason", length = 500)
    private String deactivationReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected AccessBlock() {
    }

    public AccessBlock(UUID patientId, AccessBlockType blockType) {
        this.patientId = patientId;
        this.blockType = blockType;
        this.requestedAt = Instant.now();
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

    public void deactivate(UUID deactivatedBy, String reason) {
        this.active = false;
        this.deactivatedAt = Instant.now();
        this.deactivatedBy = deactivatedBy;
        this.deactivationReason = reason;
    }

    public boolean isCurrentlyActive() {
        if (!active) {
            return false;
        }
        LocalDate today = LocalDate.now();
        if (validFrom != null && today.isBefore(validFrom)) {
            return false;
        }
        if (validUntil != null && today.isAfter(validUntil)) {
            return false;
        }
        return true;
    }

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }

    public AccessBlockType getBlockType() {
        return blockType;
    }

    public void setBlockType(AccessBlockType blockType) {
        this.blockType = blockType;
    }

    public UUID getBlockedUnitId() {
        return blockedUnitId;
    }

    public void setBlockedUnitId(UUID blockedUnitId) {
        this.blockedUnitId = blockedUnitId;
    }

    public String getBlockedUnitName() {
        return blockedUnitName;
    }

    public void setBlockedUnitName(String blockedUnitName) {
        this.blockedUnitName = blockedUnitName;
    }

    public UUID getBlockedPractitionerId() {
        return blockedPractitionerId;
    }

    public void setBlockedPractitionerId(UUID blockedPractitionerId) {
        this.blockedPractitionerId = blockedPractitionerId;
    }

    public String getBlockedPractitionerName() {
        return blockedPractitionerName;
    }

    public void setBlockedPractitionerName(String blockedPractitionerName) {
        this.blockedPractitionerName = blockedPractitionerName;
    }

    public String getBlockedDataCategory() {
        return blockedDataCategory;
    }

    public void setBlockedDataCategory(String blockedDataCategory) {
        this.blockedDataCategory = blockedDataCategory;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDate validFrom) {
        this.validFrom = validFrom;
    }

    public LocalDate getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(LocalDate validUntil) {
        this.validUntil = validUntil;
    }

    public UUID getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(UUID requestedBy) {
        this.requestedBy = requestedBy;
    }

    public String getRequestedByName() {
        return requestedByName;
    }

    public void setRequestedByName(String requestedByName) {
        this.requestedByName = requestedByName;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    public Instant getDeactivatedAt() {
        return deactivatedAt;
    }

    public UUID getDeactivatedBy() {
        return deactivatedBy;
    }

    public String getDeactivationReason() {
        return deactivationReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
