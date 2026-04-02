package se.curanexus.authorization.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CareRelationTest {

    @Test
    void shouldCreateCareRelationWithRequiredFields() {
        UUID userId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();

        CareRelation relation = new CareRelation(userId, patientId, CareRelationType.PRIMARY_CARE);

        assertEquals(userId, relation.getUserId());
        assertEquals(patientId, relation.getPatientId());
        assertEquals(CareRelationType.PRIMARY_CARE, relation.getRelationType());
        assertTrue(relation.isActive());
        assertNotNull(relation.getValidFrom());
        assertNotNull(relation.getCreatedAt());
        assertNull(relation.getEncounterId());
        assertNull(relation.getValidUntil());
    }

    @Test
    void shouldBeCurrentlyActiveWhenValidAndActive() {
        UUID userId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        CareRelation relation = new CareRelation(userId, patientId, CareRelationType.PRIMARY_CARE);
        relation.setValidFrom(LocalDateTime.now().minusDays(1));

        assertTrue(relation.isCurrentlyActive());
    }

    @Test
    void shouldNotBeCurrentlyActiveWhenDeactivated() {
        UUID userId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        CareRelation relation = new CareRelation(userId, patientId, CareRelationType.PRIMARY_CARE);
        relation.setValidFrom(LocalDateTime.now().minusDays(1));

        relation.end(UUID.randomUUID());

        assertFalse(relation.isCurrentlyActive());
        assertFalse(relation.isActive());
        assertNotNull(relation.getEndedAt());
        assertNotNull(relation.getEndedById());
    }

    @Test
    void shouldNotBeCurrentlyActiveBeforeValidFrom() {
        UUID userId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        CareRelation relation = new CareRelation(userId, patientId, CareRelationType.PRIMARY_CARE);
        relation.setValidFrom(LocalDateTime.now().plusDays(1));

        assertFalse(relation.isCurrentlyActive());
    }

    @Test
    void shouldNotBeCurrentlyActiveAfterValidUntil() {
        UUID userId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        CareRelation relation = new CareRelation(userId, patientId, CareRelationType.PRIMARY_CARE);
        relation.setValidFrom(LocalDateTime.now().minusDays(2));
        relation.setValidUntil(LocalDateTime.now().minusDays(1));

        assertFalse(relation.isCurrentlyActive());
    }

    @Test
    void shouldSetEncounterId() {
        UUID userId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        UUID encounterId = UUID.randomUUID();
        CareRelation relation = new CareRelation(userId, patientId, CareRelationType.SPECIALIST);

        relation.setEncounterId(encounterId);

        assertEquals(encounterId, relation.getEncounterId());
    }

    @Test
    void shouldSetReason() {
        UUID userId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        CareRelation relation = new CareRelation(userId, patientId, CareRelationType.EMERGENCY);

        relation.setReason("Emergency admission for chest pain");

        assertEquals("Emergency admission for chest pain", relation.getReason());
    }

    @Test
    void shouldSupportAllRelationTypes() {
        UUID userId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();

        for (CareRelationType type : CareRelationType.values()) {
            CareRelation relation = new CareRelation(userId, patientId, type);
            assertEquals(type, relation.getRelationType());
        }
    }
}
