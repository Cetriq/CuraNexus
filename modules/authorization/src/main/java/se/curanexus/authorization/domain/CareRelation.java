package se.curanexus.authorization.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "care_relations", indexes = {
    @Index(name = "idx_care_relation_user", columnList = "user_id"),
    @Index(name = "idx_care_relation_patient", columnList = "patient_id"),
    @Index(name = "idx_care_relation_encounter", columnList = "encounter_id")
})
public class CareRelation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Column(name = "encounter_id")
    private UUID encounterId;

    @Enumerated(EnumType.STRING)
    @Column(name = "relation_type", nullable = false, length = 30)
    private CareRelationType relationType;

    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;

    @Column(name = "valid_until")
    private LocalDateTime validUntil;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "active")
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    @Column(name = "ended_by_id")
    private UUID endedById;

    protected CareRelation() {
    }

    public CareRelation(UUID userId, UUID patientId, CareRelationType relationType) {
        this.userId = userId;
        this.patientId = patientId;
        this.relationType = relationType;
        this.validFrom = LocalDateTime.now();
        this.createdAt = Instant.now();
    }

    public boolean isCurrentlyActive() {
        if (!active) return false;
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(validFrom)) return false;
        if (validUntil != null && now.isAfter(validUntil)) return false;
        return true;
    }

    public void end(UUID endedById) {
        this.active = false;
        this.endedAt = Instant.now();
        this.endedById = endedById;
    }

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public UUID getEncounterId() {
        return encounterId;
    }

    public void setEncounterId(UUID encounterId) {
        this.encounterId = encounterId;
    }

    public CareRelationType getRelationType() {
        return relationType;
    }

    public LocalDateTime getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDateTime validFrom) {
        this.validFrom = validFrom;
    }

    public LocalDateTime getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(LocalDateTime validUntil) {
        this.validUntil = validUntil;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getEndedAt() {
        return endedAt;
    }

    public UUID getEndedById() {
        return endedById;
    }
}
