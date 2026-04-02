package se.curanexus.task.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DelegationTest {

    @Test
    void shouldCreateDelegationWithActiveStatus() {
        UUID fromUserId = UUID.randomUUID();
        UUID toUserId = UUID.randomUUID();
        LocalDateTime validFrom = LocalDateTime.now();
        LocalDateTime validUntil = LocalDateTime.now().plusDays(7);

        Delegation delegation = new Delegation(fromUserId, toUserId, validFrom, validUntil);

        assertEquals(fromUserId, delegation.getFromUserId());
        assertEquals(toUserId, delegation.getToUserId());
        assertEquals(validFrom, delegation.getValidFrom());
        assertEquals(validUntil, delegation.getValidUntil());
        assertEquals(DelegationStatus.ACTIVE, delegation.getStatus());
        assertNotNull(delegation.getCreatedAt());
    }

    @Test
    void shouldRevokeDelegation() {
        Delegation delegation = createTestDelegation();
        UUID revokedById = UUID.randomUUID();

        delegation.revoke(revokedById);

        assertEquals(DelegationStatus.REVOKED, delegation.getStatus());
        assertEquals(revokedById, delegation.getRevokedById());
        assertNotNull(delegation.getRevokedAt());
    }

    @Test
    void shouldNotRevokeAlreadyRevokedDelegation() {
        Delegation delegation = createTestDelegation();
        delegation.revoke(UUID.randomUUID());

        assertThrows(IllegalStateException.class, () ->
                delegation.revoke(UUID.randomUUID()));
    }

    @Test
    void shouldExpireDelegation() {
        Delegation delegation = new Delegation(
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1)
        );

        delegation.checkExpiration();

        assertEquals(DelegationStatus.EXPIRED, delegation.getStatus());
    }

    @Test
    void shouldNotExpireActiveDelegation() {
        Delegation delegation = createTestDelegation();

        delegation.checkExpiration();

        assertEquals(DelegationStatus.ACTIVE, delegation.getStatus());
    }

    @Test
    void shouldBeCurrentlyActive() {
        Delegation delegation = new Delegation(
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusHours(1)
        );

        assertTrue(delegation.isCurrentlyActive());
    }

    @Test
    void shouldNotBeCurrentlyActiveWhenNotStarted() {
        Delegation delegation = new Delegation(
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2)
        );

        assertFalse(delegation.isCurrentlyActive());
    }

    @Test
    void shouldNotBeCurrentlyActiveWhenExpired() {
        Delegation delegation = new Delegation(
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDateTime.now().minusHours(2),
                LocalDateTime.now().minusHours(1)
        );

        assertFalse(delegation.isCurrentlyActive());
    }

    private Delegation createTestDelegation() {
        return new Delegation(
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(7)
        );
    }
}
