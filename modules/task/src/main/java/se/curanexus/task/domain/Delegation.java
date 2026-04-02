package se.curanexus.task.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "delegations", indexes = {
    @Index(name = "idx_delegation_from", columnList = "from_user_id"),
    @Index(name = "idx_delegation_to", columnList = "to_user_id"),
    @Index(name = "idx_delegation_status", columnList = "status")
})
public class Delegation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "from_user_id", nullable = false)
    private UUID fromUserId;

    @Column(name = "to_user_id", nullable = false)
    private UUID toUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DelegationStatus status = DelegationStatus.ACTIVE;

    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;

    @Column(name = "valid_until", nullable = false)
    private LocalDateTime validUntil;

    @Column(name = "scope", length = 200)
    private String scope;

    @Column(name = "note", length = 500)
    private String note;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "revoked_by_id")
    private UUID revokedById;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Delegation() {
    }

    public Delegation(UUID fromUserId, UUID toUserId, LocalDateTime validFrom, LocalDateTime validUntil) {
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.createdAt = Instant.now();
    }

    public void revoke(UUID revokedById) {
        if (this.status != DelegationStatus.ACTIVE) {
            throw new IllegalStateException("Only active delegations can be revoked");
        }
        this.status = DelegationStatus.REVOKED;
        this.revokedAt = Instant.now();
        this.revokedById = revokedById;
    }

    public void checkExpiration() {
        if (this.status == DelegationStatus.ACTIVE && LocalDateTime.now().isAfter(validUntil)) {
            this.status = DelegationStatus.EXPIRED;
        }
    }

    public boolean isCurrentlyActive() {
        LocalDateTime now = LocalDateTime.now();
        return status == DelegationStatus.ACTIVE &&
               now.isAfter(validFrom) &&
               now.isBefore(validUntil);
    }

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public UUID getFromUserId() {
        return fromUserId;
    }

    public UUID getToUserId() {
        return toUserId;
    }

    public DelegationStatus getStatus() {
        return status;
    }

    public LocalDateTime getValidFrom() {
        return validFrom;
    }

    public LocalDateTime getValidUntil() {
        return validUntil;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public UUID getRevokedById() {
        return revokedById;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
