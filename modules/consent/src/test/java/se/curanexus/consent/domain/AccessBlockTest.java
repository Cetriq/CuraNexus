package se.curanexus.consent.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AccessBlockTest {

    private AccessBlock accessBlock;
    private UUID patientId;

    @BeforeEach
    void setUp() {
        patientId = UUID.randomUUID();
        accessBlock = new AccessBlock(patientId, AccessBlockType.UNIT);
    }

    @Test
    void constructor_ShouldSetPatientIdAndBlockType() {
        assertEquals(patientId, accessBlock.getPatientId());
        assertEquals(AccessBlockType.UNIT, accessBlock.getBlockType());
        assertTrue(accessBlock.isActive());
        assertNotNull(accessBlock.getRequestedAt());
    }

    @Test
    void deactivate_ShouldSetInactiveAndReason() {
        UUID deactivatedBy = UUID.randomUUID();
        accessBlock.deactivate(deactivatedBy, "Patient request");

        assertFalse(accessBlock.isActive());
        assertNotNull(accessBlock.getDeactivatedAt());
        assertEquals(deactivatedBy, accessBlock.getDeactivatedBy());
        assertEquals("Patient request", accessBlock.getDeactivationReason());
    }

    @Test
    void isCurrentlyActive_ShouldReturnFalse_WhenInactive() {
        accessBlock.deactivate(UUID.randomUUID(), "Test");

        assertFalse(accessBlock.isCurrentlyActive());
    }

    @Test
    void isCurrentlyActive_ShouldReturnTrue_WhenActiveAndNoDateRestrictions() {
        assertTrue(accessBlock.isCurrentlyActive());
    }

    @Test
    void isCurrentlyActive_ShouldReturnFalse_WhenBeforeValidFrom() {
        accessBlock.setValidFrom(LocalDate.now().plusDays(1));

        assertFalse(accessBlock.isCurrentlyActive());
    }

    @Test
    void isCurrentlyActive_ShouldReturnFalse_WhenAfterValidUntil() {
        accessBlock.setValidUntil(LocalDate.now().minusDays(1));

        assertFalse(accessBlock.isCurrentlyActive());
    }

    @Test
    void isCurrentlyActive_ShouldReturnTrue_WhenWithinDateRange() {
        accessBlock.setValidFrom(LocalDate.now().minusDays(1));
        accessBlock.setValidUntil(LocalDate.now().plusDays(1));

        assertTrue(accessBlock.isCurrentlyActive());
    }

    @Test
    void settersAndGetters_ForUnitBlock_ShouldWork() {
        UUID unitId = UUID.randomUUID();
        UUID requestedBy = UUID.randomUUID();

        accessBlock.setBlockedUnitId(unitId);
        accessBlock.setBlockedUnitName("Test Hospital");
        accessBlock.setReason("Privacy concerns");
        accessBlock.setValidFrom(LocalDate.now());
        accessBlock.setValidUntil(LocalDate.now().plusYears(1));
        accessBlock.setRequestedBy(requestedBy);
        accessBlock.setRequestedByName("Patient Name");

        assertEquals(unitId, accessBlock.getBlockedUnitId());
        assertEquals("Test Hospital", accessBlock.getBlockedUnitName());
        assertEquals("Privacy concerns", accessBlock.getReason());
        assertEquals(LocalDate.now(), accessBlock.getValidFrom());
        assertEquals(LocalDate.now().plusYears(1), accessBlock.getValidUntil());
        assertEquals(requestedBy, accessBlock.getRequestedBy());
        assertEquals("Patient Name", accessBlock.getRequestedByName());
    }

    @Test
    void settersAndGetters_ForPractitionerBlock_ShouldWork() {
        AccessBlock practitionerBlock = new AccessBlock(patientId, AccessBlockType.PRACTITIONER);
        UUID practitionerId = UUID.randomUUID();

        practitionerBlock.setBlockedPractitionerId(practitionerId);
        practitionerBlock.setBlockedPractitionerName("Dr. Smith");

        assertEquals(practitionerId, practitionerBlock.getBlockedPractitionerId());
        assertEquals("Dr. Smith", practitionerBlock.getBlockedPractitionerName());
    }

    @Test
    void settersAndGetters_ForDataCategoryBlock_ShouldWork() {
        AccessBlock categoryBlock = new AccessBlock(patientId, AccessBlockType.DATA_CATEGORY);

        categoryBlock.setBlockedDataCategory("MENTAL_HEALTH");

        assertEquals("MENTAL_HEALTH", categoryBlock.getBlockedDataCategory());
    }

    @Test
    void blockTypeEnum_ShouldHaveExpectedValues() {
        assertEquals(5, AccessBlockType.values().length);
        assertNotNull(AccessBlockType.UNIT);
        assertNotNull(AccessBlockType.PRACTITIONER);
        assertNotNull(AccessBlockType.DATA_CATEGORY);
        assertNotNull(AccessBlockType.EXTERNAL_UNITS);
        assertNotNull(AccessBlockType.EMERGENCY_OVERRIDE);
    }
}
