package se.curanexus.coding.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Represents a code system (kodverk) like ICD-10, KVÅ, or ATC.
 */
@Entity
@Table(name = "code_systems")
public class CodeSystem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, unique = true, length = 20)
    private CodeSystemType type;

    @Column(name = "version", nullable = false, length = 20)
    private String version;

    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    @Column(name = "valid_to")
    private LocalDate validTo;

    @Column(name = "source_url", length = 500)
    private String sourceUrl;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    protected CodeSystem() {
    }

    public CodeSystem(CodeSystemType type, String version, LocalDate validFrom) {
        this.type = type;
        this.version = version;
        this.validFrom = validFrom;
    }

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public CodeSystemType getType() {
        return type;
    }

    public void setType(CodeSystemType type) {
        this.type = type;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isValidAt(LocalDate date) {
        if (!active) return false;
        if (date.isBefore(validFrom)) return false;
        if (validTo != null && date.isAfter(validTo)) return false;
        return true;
    }
}
