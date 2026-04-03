package se.curanexus.authorization.abac;

import jakarta.persistence.*;
import se.curanexus.authorization.domain.ActionType;
import se.curanexus.authorization.domain.ResourceType;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents an access policy for ABAC evaluation.
 * Policies define rules for when access should be granted or denied.
 *
 * Policy types:
 * - PERMIT: Grant access when conditions are met
 * - DENY: Deny access when conditions are met (takes precedence)
 * - REQUIRE_CONTEXT: Require specific context (e.g., care relation, encounter)
 */
@Entity
@Table(name = "access_policies", indexes = {
        @Index(name = "idx_policy_resource_action", columnList = "resource_type, action_type"),
        @Index(name = "idx_policy_active", columnList = "active")
})
public class AccessPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "policy_type", nullable = false, length = 30)
    private PolicyType policyType;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", length = 30)
    private ResourceType resourceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", length = 30)
    private ActionType actionType;

    /**
     * SpEL expression for policy condition.
     * Available variables: context, user, resource, environment
     */
    @Column(name = "condition_expression", length = 1000)
    private String conditionExpression;

    /**
     * Required role code (if any).
     */
    @Column(name = "required_role", length = 50)
    private String requiredRole;

    /**
     * Required permission code (if any).
     */
    @Column(name = "required_permission", length = 50)
    private String requiredPermission;

    /**
     * Whether care relation is required.
     */
    @Column(name = "require_care_relation")
    private boolean requireCareRelation;

    /**
     * Whether encounter context is required.
     */
    @Column(name = "require_encounter_context")
    private boolean requireEncounterContext;

    /**
     * Allowed user types (comma-separated).
     */
    @Column(name = "allowed_user_types", length = 200)
    private String allowedUserTypes;

    /**
     * Allowed departments (comma-separated).
     */
    @Column(name = "allowed_departments", length = 500)
    private String allowedDepartments;

    /**
     * Priority for policy evaluation (higher = evaluated first).
     */
    @Column(name = "priority")
    private int priority = 0;

    @Column(name = "active")
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    protected AccessPolicy() {
    }

    public AccessPolicy(String name, PolicyType policyType) {
        this.name = name;
        this.policyType = policyType;
        this.createdAt = Instant.now();
    }

    /**
     * Check if this policy applies to the given context.
     */
    public boolean appliesTo(AccessContext context) {
        // Check resource type match
        if (resourceType != null && !resourceType.equals(context.resourceType())) {
            return false;
        }

        // Check action type match
        if (actionType != null && !actionType.equals(context.action())) {
            return false;
        }

        return true;
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
        this.updatedAt = Instant.now();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = Instant.now();
    }

    public PolicyType getPolicyType() {
        return policyType;
    }

    public void setPolicyType(PolicyType policyType) {
        this.policyType = policyType;
        this.updatedAt = Instant.now();
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
        this.updatedAt = Instant.now();
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
        this.updatedAt = Instant.now();
    }

    public String getConditionExpression() {
        return conditionExpression;
    }

    public void setConditionExpression(String conditionExpression) {
        this.conditionExpression = conditionExpression;
        this.updatedAt = Instant.now();
    }

    public String getRequiredRole() {
        return requiredRole;
    }

    public void setRequiredRole(String requiredRole) {
        this.requiredRole = requiredRole;
        this.updatedAt = Instant.now();
    }

    public String getRequiredPermission() {
        return requiredPermission;
    }

    public void setRequiredPermission(String requiredPermission) {
        this.requiredPermission = requiredPermission;
        this.updatedAt = Instant.now();
    }

    public boolean isRequireCareRelation() {
        return requireCareRelation;
    }

    public void setRequireCareRelation(boolean requireCareRelation) {
        this.requireCareRelation = requireCareRelation;
        this.updatedAt = Instant.now();
    }

    public boolean isRequireEncounterContext() {
        return requireEncounterContext;
    }

    public void setRequireEncounterContext(boolean requireEncounterContext) {
        this.requireEncounterContext = requireEncounterContext;
        this.updatedAt = Instant.now();
    }

    public String getAllowedUserTypes() {
        return allowedUserTypes;
    }

    public void setAllowedUserTypes(String allowedUserTypes) {
        this.allowedUserTypes = allowedUserTypes;
        this.updatedAt = Instant.now();
    }

    public String getAllowedDepartments() {
        return allowedDepartments;
    }

    public void setAllowedDepartments(String allowedDepartments) {
        this.allowedDepartments = allowedDepartments;
        this.updatedAt = Instant.now();
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
        this.updatedAt = Instant.now();
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
        this.updatedAt = Instant.now();
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
