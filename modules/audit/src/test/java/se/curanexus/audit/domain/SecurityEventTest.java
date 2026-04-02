package se.curanexus.audit.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("SecurityEvent Entity Tests")
class SecurityEventTest {

    @Test
    @DisplayName("Should create security event with required fields")
    void shouldCreateSecurityEventWithRequiredFields() {
        UUID userId = UUID.randomUUID();

        SecurityEvent event = new SecurityEvent(userId, SecurityEventType.LOGIN, true);

        assertThat(event.getUserId()).isEqualTo(userId);
        assertThat(event.getEventType()).isEqualTo(SecurityEventType.LOGIN);
        assertThat(event.isSuccess()).isTrue();
        assertThat(event.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Should allow null user ID for failed login attempts")
    void shouldAllowNullUserIdForFailedLoginAttempts() {
        SecurityEvent event = new SecurityEvent(null, SecurityEventType.LOGIN_FAILED, false);

        assertThat(event.getUserId()).isNull();
        assertThat(event.getEventType()).isEqualTo(SecurityEventType.LOGIN_FAILED);
        assertThat(event.isSuccess()).isFalse();
    }

    @Test
    @DisplayName("Should throw exception when event type is null")
    void shouldThrowExceptionWhenEventTypeIsNull() {
        UUID userId = UUID.randomUUID();

        assertThatThrownBy(() -> new SecurityEvent(userId, null, true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Event type is required");
    }

    @Test
    @DisplayName("Should set all optional fields")
    void shouldSetAllOptionalFields() {
        UUID userId = UUID.randomUUID();

        SecurityEvent event = new SecurityEvent(userId, SecurityEventType.PERMISSION_DENIED, false);
        event.setUsername("hacker123");
        event.setIpAddress("1.2.3.4");
        event.setUserAgent("EvilBot/1.0");
        event.setDetails("{\"resource\": \"admin-panel\", \"action\": \"access\"}");

        assertThat(event.getUsername()).isEqualTo("hacker123");
        assertThat(event.getIpAddress()).isEqualTo("1.2.3.4");
        assertThat(event.getUserAgent()).isEqualTo("EvilBot/1.0");
        assertThat(event.getDetails()).isEqualTo("{\"resource\": \"admin-panel\", \"action\": \"access\"}");
    }

    @Test
    @DisplayName("Should have all security event types defined")
    void shouldHaveAllSecurityEventTypesDefined() {
        assertThat(SecurityEventType.values()).contains(
                SecurityEventType.LOGIN,
                SecurityEventType.LOGOUT,
                SecurityEventType.LOGIN_FAILED,
                SecurityEventType.PASSWORD_CHANGE,
                SecurityEventType.PASSWORD_RESET,
                SecurityEventType.MFA_ENABLED,
                SecurityEventType.MFA_DISABLED,
                SecurityEventType.PERMISSION_DENIED,
                SecurityEventType.SESSION_EXPIRED,
                SecurityEventType.SUSPICIOUS_ACTIVITY,
                SecurityEventType.ACCOUNT_LOCKED,
                SecurityEventType.EMERGENCY_ACCESS
        );
    }

    @Test
    @DisplayName("Should track failed login attempt correctly")
    void shouldTrackFailedLoginAttemptCorrectly() {
        SecurityEvent event = new SecurityEvent(null, SecurityEventType.LOGIN_FAILED, false);
        event.setUsername("unknown_user");
        event.setIpAddress("192.168.100.50");
        event.setDetails("{\"reason\": \"invalid_credentials\", \"attempts\": 3}");

        assertThat(event.getEventType()).isEqualTo(SecurityEventType.LOGIN_FAILED);
        assertThat(event.isSuccess()).isFalse();
        assertThat(event.getUsername()).isEqualTo("unknown_user");
    }
}
