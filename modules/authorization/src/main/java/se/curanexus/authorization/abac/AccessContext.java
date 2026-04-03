package se.curanexus.authorization.abac;

import se.curanexus.authorization.domain.ActionType;
import se.curanexus.authorization.domain.ResourceType;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Access context for ABAC (Attribute-Based Access Control).
 * Contains all contextual attributes for an access decision.
 *
 * According to Swedish healthcare requirements (Princip 4: Zero Trust Access),
 * access requires care relation context and all access must be logged.
 */
public record AccessContext(
        // Subject attributes (who is requesting access)
        UUID userId,
        String username,
        String userType,
        String department,
        String unit,

        // Resource attributes (what is being accessed)
        ResourceType resourceType,
        UUID resourceId,
        UUID patientId,

        // Action attributes (what operation)
        ActionType action,

        // Environment attributes (context)
        UUID encounterId,
        String encounterClass,
        LocalDateTime requestTime,
        String clientIp,
        String clientApplication,

        // Additional attributes
        Map<String, Object> additionalAttributes
) {
    public AccessContext {
        if (additionalAttributes == null) {
            additionalAttributes = new HashMap<>();
        }
        if (requestTime == null) {
            requestTime = LocalDateTime.now();
        }
    }

    /**
     * Builder for creating AccessContext instances.
     */
    public static Builder builder() {
        return new Builder();
    }

    public Optional<Object> getAttribute(String key) {
        return Optional.ofNullable(additionalAttributes.get(key));
    }

    public boolean hasEncounterContext() {
        return encounterId != null;
    }

    public boolean hasPatientContext() {
        return patientId != null;
    }

    /**
     * Check if this is an emergency access request (nödåtkomst).
     */
    public boolean isEmergencyAccess() {
        Object emergency = additionalAttributes.get("emergencyAccess");
        return emergency != null && Boolean.TRUE.equals(emergency);
    }

    /**
     * Get the reason for access (required for audit trail).
     */
    public Optional<String> getAccessReason() {
        return Optional.ofNullable((String) additionalAttributes.get("accessReason"));
    }

    public static class Builder {
        private UUID userId;
        private String username;
        private String userType;
        private String department;
        private String unit;
        private ResourceType resourceType;
        private UUID resourceId;
        private UUID patientId;
        private ActionType action;
        private UUID encounterId;
        private String encounterClass;
        private LocalDateTime requestTime;
        private String clientIp;
        private String clientApplication;
        private final Map<String, Object> additionalAttributes = new HashMap<>();

        public Builder userId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder userType(String userType) {
            this.userType = userType;
            return this;
        }

        public Builder department(String department) {
            this.department = department;
            return this;
        }

        public Builder unit(String unit) {
            this.unit = unit;
            return this;
        }

        public Builder resourceType(ResourceType resourceType) {
            this.resourceType = resourceType;
            return this;
        }

        public Builder resourceId(UUID resourceId) {
            this.resourceId = resourceId;
            return this;
        }

        public Builder patientId(UUID patientId) {
            this.patientId = patientId;
            return this;
        }

        public Builder action(ActionType action) {
            this.action = action;
            return this;
        }

        public Builder encounterId(UUID encounterId) {
            this.encounterId = encounterId;
            return this;
        }

        public Builder encounterClass(String encounterClass) {
            this.encounterClass = encounterClass;
            return this;
        }

        public Builder requestTime(LocalDateTime requestTime) {
            this.requestTime = requestTime;
            return this;
        }

        public Builder clientIp(String clientIp) {
            this.clientIp = clientIp;
            return this;
        }

        public Builder clientApplication(String clientApplication) {
            this.clientApplication = clientApplication;
            return this;
        }

        public Builder emergencyAccess(boolean emergency) {
            this.additionalAttributes.put("emergencyAccess", emergency);
            return this;
        }

        public Builder accessReason(String reason) {
            this.additionalAttributes.put("accessReason", reason);
            return this;
        }

        public Builder attribute(String key, Object value) {
            this.additionalAttributes.put(key, value);
            return this;
        }

        public AccessContext build() {
            return new AccessContext(
                    userId, username, userType, department, unit,
                    resourceType, resourceId, patientId,
                    action,
                    encounterId, encounterClass, requestTime, clientIp, clientApplication,
                    additionalAttributes
            );
        }
    }
}
