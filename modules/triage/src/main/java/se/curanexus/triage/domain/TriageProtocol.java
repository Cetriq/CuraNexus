package se.curanexus.triage.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "triage_protocols", indexes = {
    @Index(name = "idx_protocol_code", columnList = "code"),
    @Index(name = "idx_protocol_category", columnList = "category"),
    @Index(name = "idx_protocol_active", columnList = "active")
})
public class TriageProtocol {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "code", nullable = false, unique = true, length = 30)
    private String code;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "version", length = 20)
    private String version;

    @Column(name = "active", nullable = false)
    private boolean active;

    @OneToMany(mappedBy = "protocol", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stepOrder ASC")
    private List<ProtocolStep> steps = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "protocol_red_flags", joinColumns = @JoinColumn(name = "protocol_id"))
    @Column(name = "red_flag", length = 500)
    private List<String> redFlags = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected TriageProtocol() {
    }

    public TriageProtocol(String code, String name) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Protocol code is required");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Protocol name is required");
        }

        this.code = code;
        this.name = name;
        this.active = true;
        this.version = "1.0";
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public void addStep(ProtocolStep step) {
        steps.add(step);
        step.setProtocol(this);
    }

    public void addRedFlag(String redFlag) {
        redFlags.add(redFlag);
    }

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<ProtocolStep> getSteps() {
        return steps;
    }

    public List<String> getRedFlags() {
        return redFlags;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
