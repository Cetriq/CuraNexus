package se.curanexus.medication.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MedicationAdministration domänentitet")
class MedicationAdministrationTest {

    private UUID patientId;
    private UUID performerId;
    private Prescription prescription;

    @BeforeEach
    void setUp() {
        patientId = UUID.randomUUID();
        performerId = UUID.randomUUID();
        prescription = new Prescription(patientId, UUID.randomUUID());
        prescription.setMedicationText("Test medication");
    }

    @Nested
    @DisplayName("Skapande")
    class Creation {

        @Test
        @DisplayName("Ska skapa administrering med korrekta värden")
        void shouldCreateWithCorrectValues() {
            MedicationAdministration admin = new MedicationAdministration(patientId, prescription);

            assertEquals(patientId, admin.getPatientId());
            assertEquals(prescription, admin.getPrescription());
            assertEquals(AdministrationStatus.PLANNED, admin.getStatus());
            assertNotNull(admin.getCreatedAt());
        }
    }

    @Nested
    @DisplayName("Statusövergångar")
    class StatusTransitions {

        private MedicationAdministration administration;

        @BeforeEach
        void setUp() {
            administration = new MedicationAdministration(patientId, prescription);
        }

        @Test
        @DisplayName("Ska kunna starta administrering")
        void shouldStartAdministration() {
            administration.startAdministration(performerId, "Anna Sköterska");

            assertEquals(AdministrationStatus.IN_PROGRESS, administration.getStatus());
            assertEquals(performerId, administration.getPerformerId());
            assertEquals("Anna Sköterska", administration.getPerformerName());
            assertNotNull(administration.getAdministeredAt());
        }

        @Test
        @DisplayName("Ska kunna slutföra administrering direkt")
        void shouldCompleteDirect() {
            administration.complete(
                    performerId,
                    "Anna Sköterska",
                    new BigDecimal("500"),
                    "mg"
            );

            assertEquals(AdministrationStatus.COMPLETED, administration.getStatus());
            assertEquals(new BigDecimal("500"), administration.getDoseQuantity());
            assertEquals("mg", administration.getDoseUnit());
        }

        @Test
        @DisplayName("Ska kunna slutföra pågående administrering")
        void shouldCompleteInProgress() {
            administration.startAdministration(performerId, "Anna Sköterska");
            administration.complete(
                    performerId,
                    "Anna Sköterska",
                    new BigDecimal("100"),
                    "ml"
            );

            assertEquals(AdministrationStatus.COMPLETED, administration.getStatus());
        }

        @Test
        @DisplayName("Ska kunna markera som ej given")
        void shouldMarkNotGiven() {
            administration.markNotGiven("Patient vägrade ta medicin");

            assertEquals(AdministrationStatus.NOT_DONE, administration.getStatus());
            assertEquals("Patient vägrade ta medicin", administration.getNotGivenReason());
        }

        @Test
        @DisplayName("Ska kunna avbryta pågående administrering")
        void shouldStopInProgress() {
            administration.startAdministration(performerId, "Anna Sköterska");
            administration.stop("Allergisk reaktion observerad");

            assertEquals(AdministrationStatus.STOPPED, administration.getStatus());
            assertTrue(administration.getNotes().contains("Avbruten"));
        }

        @Test
        @DisplayName("Ska inte kunna starta redan slutförd administrering")
        void shouldNotStartCompleted() {
            administration.complete(performerId, "Anna", new BigDecimal("1"), "tablett");

            assertThrows(IllegalStateException.class, () ->
                    administration.startAdministration(performerId, "Anna"));
        }

        @Test
        @DisplayName("Ska inte kunna avbryta ej pågående administrering")
        void shouldNotStopIfNotInProgress() {
            assertThrows(IllegalStateException.class, () ->
                    administration.stop("Test"));
        }

        @Test
        @DisplayName("Ska kunna markera som felaktigt registrerad")
        void shouldMarkAsEnteredInError() {
            administration.complete(performerId, "Anna", new BigDecimal("1"), "tablett");
            administration.markAsEnteredInError("Registrerad på fel patient");

            assertEquals(AdministrationStatus.ENTERED_IN_ERROR, administration.getStatus());
        }
    }

    @Nested
    @DisplayName("Försening")
    class Overdue {

        @Test
        @DisplayName("Ska vara försenad om schemalagd tid passerat")
        void shouldBeOverdueIfScheduledTimePassed() {
            MedicationAdministration admin = new MedicationAdministration(patientId, prescription);
            admin.setScheduledAt(LocalDateTime.now().minusHours(1));

            assertTrue(admin.isOverdue());
        }

        @Test
        @DisplayName("Ska inte vara försenad inom grace period")
        void shouldNotBeOverdueWithinGracePeriod() {
            MedicationAdministration admin = new MedicationAdministration(patientId, prescription);
            admin.setScheduledAt(LocalDateTime.now().minusMinutes(15)); // 30 min grace

            assertFalse(admin.isOverdue());
        }

        @Test
        @DisplayName("Ska inte vara försenad om ej schemalagd")
        void shouldNotBeOverdueIfNotScheduled() {
            MedicationAdministration admin = new MedicationAdministration(patientId, prescription);

            assertFalse(admin.isOverdue());
        }

        @Test
        @DisplayName("Ska inte vara försenad om redan utförd")
        void shouldNotBeOverdueIfCompleted() {
            MedicationAdministration admin = new MedicationAdministration(patientId, prescription);
            admin.setScheduledAt(LocalDateTime.now().minusHours(1));
            admin.complete(performerId, "Anna", new BigDecimal("1"), "tablett");

            assertFalse(admin.isOverdue());
        }
    }

    @Nested
    @DisplayName("Infusionsdata")
    class InfusionData {

        @Test
        @DisplayName("Ska kunna sätta infusionshastighet")
        void shouldSetInfusionRate() {
            MedicationAdministration admin = new MedicationAdministration(patientId, prescription);
            admin.setRateQuantity(new BigDecimal("50"));
            admin.setRateUnit("ml/h");
            admin.setMethod("Slow IV infusion");

            assertEquals(new BigDecimal("50"), admin.getRateQuantity());
            assertEquals("ml/h", admin.getRateUnit());
            assertEquals("Slow IV infusion", admin.getMethod());
        }
    }
}
