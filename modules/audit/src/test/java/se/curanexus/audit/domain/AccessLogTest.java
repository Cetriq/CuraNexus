package se.curanexus.audit.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("AccessLog Entity Tests")
class AccessLogTest {

    @Test
    @DisplayName("Should create access log with required fields")
    void shouldCreateAccessLogWithRequiredFields() {
        UUID userId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();

        AccessLog log = new AccessLog(userId, patientId, ResourceType.PATIENT, AccessType.VIEW);

        assertThat(log.getUserId()).isEqualTo(userId);
        assertThat(log.getPatientId()).isEqualTo(patientId);
        assertThat(log.getResourceType()).isEqualTo(ResourceType.PATIENT);
        assertThat(log.getAccessType()).isEqualTo(AccessType.VIEW);
        assertThat(log.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Should throw exception when user ID is null")
    void shouldThrowExceptionWhenUserIdIsNull() {
        UUID patientId = UUID.randomUUID();

        assertThatThrownBy(() -> new AccessLog(null, patientId, ResourceType.PATIENT, AccessType.VIEW))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User ID is required");
    }

    @Test
    @DisplayName("Should throw exception when patient ID is null")
    void shouldThrowExceptionWhenPatientIdIsNull() {
        UUID userId = UUID.randomUUID();

        assertThatThrownBy(() -> new AccessLog(userId, null, ResourceType.PATIENT, AccessType.VIEW))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Patient ID is required");
    }

    @Test
    @DisplayName("Should throw exception when resource type is null")
    void shouldThrowExceptionWhenResourceTypeIsNull() {
        UUID userId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();

        assertThatThrownBy(() -> new AccessLog(userId, patientId, null, AccessType.VIEW))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Resource type is required");
    }

    @Test
    @DisplayName("Should throw exception when access type is null")
    void shouldThrowExceptionWhenAccessTypeIsNull() {
        UUID userId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();

        assertThatThrownBy(() -> new AccessLog(userId, patientId, ResourceType.PATIENT, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Access type is required");
    }

    @Test
    @DisplayName("Should set all optional fields")
    void shouldSetAllOptionalFields() {
        UUID userId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();
        UUID careRelationId = UUID.randomUUID();

        AccessLog log = new AccessLog(userId, patientId, ResourceType.JOURNAL_ENTRY, AccessType.EDIT);
        log.setUsername("nurse.anna");
        log.setResourceId(resourceId);
        log.setCareRelationId(careRelationId);
        log.setCareRelationType("PRIMARY_NURSE");
        log.setReason("Patient care");
        log.setIpAddress("10.0.0.1");

        assertThat(log.getUsername()).isEqualTo("nurse.anna");
        assertThat(log.getResourceId()).isEqualTo(resourceId);
        assertThat(log.getCareRelationId()).isEqualTo(careRelationId);
        assertThat(log.getCareRelationType()).isEqualTo("PRIMARY_NURSE");
        assertThat(log.getReason()).isEqualTo("Patient care");
        assertThat(log.getIpAddress()).isEqualTo("10.0.0.1");
    }

    @Test
    @DisplayName("Should have all access types defined")
    void shouldHaveAllAccessTypesDefined() {
        assertThat(AccessType.values()).contains(
                AccessType.VIEW,
                AccessType.SEARCH,
                AccessType.EDIT,
                AccessType.CREATE,
                AccessType.DELETE,
                AccessType.EXPORT,
                AccessType.PRINT,
                AccessType.EMERGENCY
        );
    }
}
