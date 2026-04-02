package se.curanexus.audit.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("AuditEvent Entity Tests")
class AuditEventTest {

    @Test
    @DisplayName("Should create audit event with required fields")
    void shouldCreateAuditEventWithRequiredFields() {
        UUID userId = UUID.randomUUID();

        AuditEvent event = new AuditEvent(AuditEventType.CREATE, userId, ResourceType.PATIENT);

        assertThat(event.getEventType()).isEqualTo(AuditEventType.CREATE);
        assertThat(event.getUserId()).isEqualTo(userId);
        assertThat(event.getResourceType()).isEqualTo(ResourceType.PATIENT);
        assertThat(event.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Should throw exception when event type is null")
    void shouldThrowExceptionWhenEventTypeIsNull() {
        UUID userId = UUID.randomUUID();

        assertThatThrownBy(() -> new AuditEvent(null, userId, ResourceType.PATIENT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Event type is required");
    }

    @Test
    @DisplayName("Should throw exception when user ID is null")
    void shouldThrowExceptionWhenUserIdIsNull() {
        assertThatThrownBy(() -> new AuditEvent(AuditEventType.CREATE, null, ResourceType.PATIENT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User ID is required");
    }

    @Test
    @DisplayName("Should throw exception when resource type is null")
    void shouldThrowExceptionWhenResourceTypeIsNull() {
        UUID userId = UUID.randomUUID();

        assertThatThrownBy(() -> new AuditEvent(AuditEventType.CREATE, userId, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Resource type is required");
    }

    @Test
    @DisplayName("Should set all optional fields")
    void shouldSetAllOptionalFields() {
        UUID userId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        UUID careRelationId = UUID.randomUUID();

        AuditEvent event = new AuditEvent(AuditEventType.READ, userId, ResourceType.JOURNAL_ENTRY);
        event.setResourceId(resourceId);
        event.setPatientId(patientId);
        event.setAction("VIEW_JOURNAL");
        event.setUsername("dr.smith");
        event.setDetails("{\"page\": 1}");
        event.setIpAddress("192.168.1.1");
        event.setUserAgent("Mozilla/5.0");
        event.setCareRelationId(careRelationId);
        event.setReason("Emergency access");

        assertThat(event.getResourceId()).isEqualTo(resourceId);
        assertThat(event.getPatientId()).isEqualTo(patientId);
        assertThat(event.getAction()).isEqualTo("VIEW_JOURNAL");
        assertThat(event.getUsername()).isEqualTo("dr.smith");
        assertThat(event.getDetails()).isEqualTo("{\"page\": 1}");
        assertThat(event.getIpAddress()).isEqualTo("192.168.1.1");
        assertThat(event.getUserAgent()).isEqualTo("Mozilla/5.0");
        assertThat(event.getCareRelationId()).isEqualTo(careRelationId);
        assertThat(event.getReason()).isEqualTo("Emergency access");
    }

    @Test
    @DisplayName("Should have all event types defined")
    void shouldHaveAllEventTypesDefined() {
        assertThat(AuditEventType.values()).contains(
                AuditEventType.CREATE,
                AuditEventType.READ,
                AuditEventType.UPDATE,
                AuditEventType.DELETE,
                AuditEventType.EXPORT,
                AuditEventType.PRINT,
                AuditEventType.SEARCH,
                AuditEventType.LOGIN,
                AuditEventType.LOGOUT,
                AuditEventType.ACCESS_DENIED,
                AuditEventType.EMERGENCY_ACCESS
        );
    }

    @Test
    @DisplayName("Should have all resource types defined")
    void shouldHaveAllResourceTypesDefined() {
        assertThat(ResourceType.values()).contains(
                ResourceType.PATIENT,
                ResourceType.CARE_ENCOUNTER,
                ResourceType.JOURNAL_ENTRY,
                ResourceType.TASK,
                ResourceType.USER,
                ResourceType.ROLE,
                ResourceType.PERMISSION,
                ResourceType.CARE_RELATION,
                ResourceType.DOCUMENT,
                ResourceType.LAB_RESULT,
                ResourceType.PRESCRIPTION,
                ResourceType.REFERRAL
        );
    }
}
