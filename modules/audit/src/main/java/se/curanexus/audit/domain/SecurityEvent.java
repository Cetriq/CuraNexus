package se.curanexus.audit.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "security_events", indexes = {
    @Index(name = "idx_security_event_user", columnList = "user_id"),
    @Index(name = "idx_security_event_type", columnList = "event_type"),
    @Index(name = "idx_security_event_timestamp", columnList = "timestamp"),
    @Index(name = "idx_security_event_success", columnList = "success")
})
public class SecurityEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "username", length = 100)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    private SecurityEventType eventType;

    @Column(name = "success", nullable = false)
    private boolean success;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    protected SecurityEvent() {
    }

    public SecurityEvent(SecurityEventType eventType, boolean success) {
        if (eventType == null) {
            throw new IllegalArgumentException("Event type is required");
        }
        this.eventType = eventType;
        this.success = success;
        this.timestamp = Instant.now();
    }

    public SecurityEvent(UUID userId, SecurityEventType eventType, boolean success) {
        if (eventType == null) {
            throw new IllegalArgumentException("Event type is required");
        }
        this.userId = userId;
        this.eventType = eventType;
        this.success = success;
        this.timestamp = Instant.now();
    }

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public SecurityEventType getEventType() {
        return eventType;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
