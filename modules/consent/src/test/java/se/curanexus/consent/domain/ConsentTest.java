package se.curanexus.consent.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ConsentTest {

    private Consent consent;
    private UUID patientId;

    @BeforeEach
    void setUp() {
        patientId = UUID.randomUUID();
        consent = new Consent(patientId, ConsentType.TREATMENT);
    }

    @Test
    void constructor_ShouldSetPatientIdAndType() {
        assertEquals(patientId, consent.getPatientId());
        assertEquals(ConsentType.TREATMENT, consent.getType());
        assertEquals(ConsentStatus.PENDING, consent.getStatus());
    }

    @Test
    void activate_ShouldSetStatusToActive_WhenPending() {
        consent.activate();

        assertEquals(ConsentStatus.ACTIVE, consent.getStatus());
        assertNotNull(consent.getGivenAt());
    }

    @Test
    void activate_ShouldNotChange_WhenNotPending() {
        consent.activate();
        consent.withdraw("Test");
        consent.activate(); // Should not change

        assertEquals(ConsentStatus.WITHDRAWN, consent.getStatus());
    }

    @Test
    void withdraw_ShouldSetStatusToWithdrawn_WhenActive() {
        consent.activate();
        consent.withdraw("Patient request");

        assertEquals(ConsentStatus.WITHDRAWN, consent.getStatus());
        assertNotNull(consent.getWithdrawnAt());
        assertEquals("Patient request", consent.getWithdrawalReason());
    }

    @Test
    void withdraw_ShouldNotChange_WhenNotActive() {
        consent.withdraw("Test");

        assertEquals(ConsentStatus.PENDING, consent.getStatus());
        assertNull(consent.getWithdrawnAt());
    }

    @Test
    void reject_ShouldSetStatusToRejected_WhenPending() {
        consent.reject();

        assertEquals(ConsentStatus.REJECTED, consent.getStatus());
    }

    @Test
    void reject_ShouldNotChange_WhenNotPending() {
        consent.activate();
        consent.reject();

        assertEquals(ConsentStatus.ACTIVE, consent.getStatus());
    }

    @Test
    void expire_ShouldSetStatusToExpired_WhenActive() {
        consent.activate();
        consent.expire();

        assertEquals(ConsentStatus.EXPIRED, consent.getStatus());
    }

    @Test
    void isValid_ShouldReturnFalse_WhenNotActive() {
        assertFalse(consent.isValid());
    }

    @Test
    void isValid_ShouldReturnTrue_WhenActiveAndNoDateRestrictions() {
        consent.activate();

        assertTrue(consent.isValid());
    }

    @Test
    void isValid_ShouldReturnFalse_WhenBeforeValidFrom() {
        consent.activate();
        consent.setValidFrom(LocalDate.now().plusDays(1));

        assertFalse(consent.isValid());
    }

    @Test
    void isValid_ShouldReturnFalse_WhenAfterValidUntil() {
        consent.activate();
        consent.setValidUntil(LocalDate.now().minusDays(1));

        assertFalse(consent.isValid());
    }

    @Test
    void isValid_ShouldReturnTrue_WhenWithinDateRange() {
        consent.activate();
        consent.setValidFrom(LocalDate.now().minusDays(1));
        consent.setValidUntil(LocalDate.now().plusDays(1));

        assertTrue(consent.isValid());
    }

    @Test
    void settersAndGetters_ShouldWork() {
        UUID managingUnitId = UUID.randomUUID();
        UUID givenBy = UUID.randomUUID();
        UUID recordedBy = UUID.randomUUID();

        consent.setDescription("Test description");
        consent.setScope("Test scope");
        consent.setManagingUnitId(managingUnitId);
        consent.setManagingUnitName("Test Hospital");
        consent.setGivenBy(givenBy);
        consent.setGivenByName("Patient Name");
        consent.setRepresentativeRelation("Parent");
        consent.setCollectionMethod("VERBAL");
        consent.setValidFrom(LocalDate.now());
        consent.setValidUntil(LocalDate.now().plusYears(1));
        consent.setRecordedBy(recordedBy);
        consent.setRecordedByName("Dr. Smith");
        consent.setDocumentReference("doc-123");

        assertEquals("Test description", consent.getDescription());
        assertEquals("Test scope", consent.getScope());
        assertEquals(managingUnitId, consent.getManagingUnitId());
        assertEquals("Test Hospital", consent.getManagingUnitName());
        assertEquals(givenBy, consent.getGivenBy());
        assertEquals("Patient Name", consent.getGivenByName());
        assertEquals("Parent", consent.getRepresentativeRelation());
        assertEquals("VERBAL", consent.getCollectionMethod());
        assertEquals(LocalDate.now(), consent.getValidFrom());
        assertEquals(LocalDate.now().plusYears(1), consent.getValidUntil());
        assertEquals(recordedBy, consent.getRecordedBy());
        assertEquals("Dr. Smith", consent.getRecordedByName());
        assertEquals("doc-123", consent.getDocumentReference());
    }
}
