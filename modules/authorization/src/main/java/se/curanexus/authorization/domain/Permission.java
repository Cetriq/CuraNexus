package se.curanexus.authorization.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "permissions", indexes = {
    @Index(name = "idx_permission_code", columnList = "code", unique = true),
    @Index(name = "idx_permission_resource", columnList = "resource")
})
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "code", nullable = false, unique = true, length = 100)
    private String code;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource", nullable = false, length = 30)
    private ResourceType resource;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 20)
    private ActionType action;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Permission() {
    }

    public Permission(String code, String name, ResourceType resource, ActionType action) {
        this.code = code;
        this.name = name;
        this.resource = resource;
        this.action = action;
        this.createdAt = Instant.now();
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ResourceType getResource() {
        return resource;
    }

    public ActionType getAction() {
        return action;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
