package se.curanexus.audit.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ChangeLog Entity Tests")
class ChangeLogTest {

    @Test
    @DisplayName("Should create change log with required fields")
    void shouldCreateChangeLogWithRequiredFields() {
        UUID userId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();

        ChangeLog log = new ChangeLog(userId, ResourceType.PATIENT, resourceId, ChangeType.UPDATE);

        assertThat(log.getUserId()).isEqualTo(userId);
        assertThat(log.getResourceType()).isEqualTo(ResourceType.PATIENT);
        assertThat(log.getResourceId()).isEqualTo(resourceId);
        assertThat(log.getChangeType()).isEqualTo(ChangeType.UPDATE);
        assertThat(log.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Should throw exception when user ID is null")
    void shouldThrowExceptionWhenUserIdIsNull() {
        UUID resourceId = UUID.randomUUID();

        assertThatThrownBy(() -> new ChangeLog(null, ResourceType.PATIENT, resourceId, ChangeType.UPDATE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User ID is required");
    }

    @Test
    @DisplayName("Should throw exception when resource type is null")
    void shouldThrowExceptionWhenResourceTypeIsNull() {
        UUID userId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();

        assertThatThrownBy(() -> new ChangeLog(userId, null, resourceId, ChangeType.UPDATE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Resource type is required");
    }

    @Test
    @DisplayName("Should throw exception when resource ID is null")
    void shouldThrowExceptionWhenResourceIdIsNull() {
        UUID userId = UUID.randomUUID();

        assertThatThrownBy(() -> new ChangeLog(userId, ResourceType.PATIENT, null, ChangeType.UPDATE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Resource ID is required");
    }

    @Test
    @DisplayName("Should throw exception when change type is null")
    void shouldThrowExceptionWhenChangeTypeIsNull() {
        UUID userId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();

        assertThatThrownBy(() -> new ChangeLog(userId, ResourceType.PATIENT, resourceId, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Change type is required");
    }

    @Test
    @DisplayName("Should set all optional fields for field change")
    void shouldSetAllOptionalFieldsForFieldChange() {
        UUID userId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();

        ChangeLog log = new ChangeLog(userId, ResourceType.PATIENT, resourceId, ChangeType.FIELD_CHANGE);
        log.setUsername("admin");
        log.setPatientId(patientId);
        log.setFieldName("phoneNumber");
        log.setOldValue("070-1234567");
        log.setNewValue("070-9876543");

        assertThat(log.getUsername()).isEqualTo("admin");
        assertThat(log.getPatientId()).isEqualTo(patientId);
        assertThat(log.getFieldName()).isEqualTo("phoneNumber");
        assertThat(log.getOldValue()).isEqualTo("070-1234567");
        assertThat(log.getNewValue()).isEqualTo("070-9876543");
    }

    @Test
    @DisplayName("Should have all change types defined")
    void shouldHaveAllChangeTypesDefined() {
        assertThat(ChangeType.values()).contains(
                ChangeType.CREATE,
                ChangeType.UPDATE,
                ChangeType.DELETE,
                ChangeType.FIELD_CHANGE,
                ChangeType.STATUS_CHANGE,
                ChangeType.RESTORE
        );
    }
}
