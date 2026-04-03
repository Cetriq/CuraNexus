package se.curanexus.medication.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Prescription domänentitet")
class PrescriptionTest {

    private UUID patientId;
    private UUID prescriberId;

    @BeforeEach
    void setUp() {
        patientId = UUID.randomUUID();
        prescriberId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("Skapande")
    class Creation {

        @Test
        @DisplayName("Ska skapa ordination med korrekta värden")
        void shouldCreateWithCorrectValues() {
            Prescription prescription = new Prescription(patientId, prescriberId);

            assertEquals(patientId, prescription.getPatientId());
            assertEquals(prescriberId, prescription.getPrescriberId());
            assertEquals(PrescriptionStatus.DRAFT, prescription.getStatus());
            assertNotNull(prescription.getCreatedAt());
        }

        @Test
        @DisplayName("Ska kunna sätta läkemedelstext")
        void shouldSetMedicationText() {
            Prescription prescription = new Prescription(patientId, prescriberId);
            prescription.setMedicationText("Paracetamol 500 mg");
            prescription.setAtcCode("N02BE01");

            assertEquals("Paracetamol 500 mg", prescription.getMedicationText());
            assertEquals("N02BE01", prescription.getAtcCode());
            assertEquals("Paracetamol 500 mg", prescription.getMedicationName());
        }
    }

    @Nested
    @DisplayName("Statusövergångar")
    class StatusTransitions {

        private Prescription prescription;

        @BeforeEach
        void setUp() {
            prescription = new Prescription(patientId, prescriberId);
            prescription.setMedicationText("Test medication");
        }

        @Test
        @DisplayName("Ska kunna aktivera ordination från DRAFT")
        void shouldActivateFromDraft() {
            prescription.activate();

            assertEquals(PrescriptionStatus.ACTIVE, prescription.getStatus());
            assertNotNull(prescription.getActivatedAt());
        }

        @Test
        @DisplayName("Ska kunna pausa aktiv ordination")
        void shouldPutOnHold() {
            prescription.activate();
            prescription.putOnHold("Tillfälligt uppehåll pga biverkning");

            assertEquals(PrescriptionStatus.ON_HOLD, prescription.getStatus());
        }

        @Test
        @DisplayName("Ska kunna återaktivera pausad ordination")
        void shouldReactivateFromOnHold() {
            prescription.activate();
            prescription.putOnHold("Tillfälligt uppehåll");
            prescription.activate();

            assertEquals(PrescriptionStatus.ACTIVE, prescription.getStatus());
        }

        @Test
        @DisplayName("Ska kunna avsluta ordination")
        void shouldComplete() {
            prescription.activate();
            prescription.complete();

            assertEquals(PrescriptionStatus.COMPLETED, prescription.getStatus());
            assertNotNull(prescription.getDiscontinuedAt());
        }

        @Test
        @DisplayName("Ska kunna avbryta ordination")
        void shouldCancel() {
            prescription.activate();
            prescription.cancel("Bytt till annat preparat");

            assertEquals(PrescriptionStatus.CANCELLED, prescription.getStatus());
            assertEquals("Bytt till annat preparat", prescription.getDiscontinuationReason());
            assertNotNull(prescription.getDiscontinuedAt());
        }

        @Test
        @DisplayName("Ska inte kunna pausa ej aktiv ordination")
        void shouldNotPutOnHoldIfNotActive() {
            assertThrows(IllegalStateException.class, () ->
                    prescription.putOnHold("Försök pausa DRAFT"));
        }

        @Test
        @DisplayName("Ska inte kunna avbryta redan avslutad ordination")
        void shouldNotCancelCompleted() {
            prescription.activate();
            prescription.complete();

            assertThrows(IllegalStateException.class, () ->
                    prescription.cancel("Försök avbryta avslutad"));
        }

        @Test
        @DisplayName("Ska kunna markera som felaktigt inlagd")
        void shouldMarkAsEnteredInError() {
            prescription.markAsEnteredInError("Fel patient");

            assertEquals(PrescriptionStatus.ENTERED_IN_ERROR, prescription.getStatus());
            assertEquals("Fel patient", prescription.getDiscontinuationReason());
        }
    }

    @Nested
    @DisplayName("Giltighetskontroll")
    class ValidityCheck {

        @Test
        @DisplayName("Ska vara aktiv på datum inom intervall")
        void shouldBeActiveOnDateWithinRange() {
            Prescription prescription = new Prescription(patientId, prescriberId);
            prescription.setStartDate(LocalDate.now().minusDays(5));
            prescription.setEndDate(LocalDate.now().plusDays(5));
            prescription.activate();

            assertTrue(prescription.isActiveOnDate(LocalDate.now()));
        }

        @Test
        @DisplayName("Ska inte vara aktiv före startdatum")
        void shouldNotBeActiveBeforeStartDate() {
            Prescription prescription = new Prescription(patientId, prescriberId);
            prescription.setStartDate(LocalDate.now().plusDays(5));
            prescription.activate();

            assertFalse(prescription.isActiveOnDate(LocalDate.now()));
        }

        @Test
        @DisplayName("Ska inte vara aktiv efter slutdatum")
        void shouldNotBeActiveAfterEndDate() {
            Prescription prescription = new Prescription(patientId, prescriberId);
            prescription.setStartDate(LocalDate.now().minusDays(10));
            prescription.setEndDate(LocalDate.now().minusDays(1));
            prescription.activate();

            assertFalse(prescription.isActiveOnDate(LocalDate.now()));
        }

        @Test
        @DisplayName("Ska inte vara aktiv om status ej är ACTIVE")
        void shouldNotBeActiveIfStatusNotActive() {
            Prescription prescription = new Prescription(patientId, prescriberId);
            prescription.setStartDate(LocalDate.now().minusDays(5));

            assertFalse(prescription.isActiveOnDate(LocalDate.now()));
        }

        @Test
        @DisplayName("Ska beräkna effektivt slutdatum från duration")
        void shouldCalculateEffectiveEndDate() {
            Prescription prescription = new Prescription(patientId, prescriberId);
            LocalDate startDate = LocalDate.now();
            prescription.setStartDate(startDate);
            prescription.setDurationDays(14);

            assertEquals(startDate.plusDays(14), prescription.getEffectiveEndDate());
        }

        @Test
        @DisplayName("Ska använda explicit slutdatum om angivet")
        void shouldUseExplicitEndDate() {
            Prescription prescription = new Prescription(patientId, prescriberId);
            LocalDate endDate = LocalDate.now().plusMonths(3);
            prescription.setStartDate(LocalDate.now());
            prescription.setEndDate(endDate);
            prescription.setDurationDays(14); // Bör ignoreras

            assertEquals(endDate, prescription.getEffectiveEndDate());
        }
    }

    @Nested
    @DisplayName("Modifierbarhet")
    class Modifiability {

        @Test
        @DisplayName("DRAFT ska vara modifierbar")
        void draftShouldBeModifiable() {
            Prescription prescription = new Prescription(patientId, prescriberId);
            assertTrue(prescription.isModifiable());
        }

        @Test
        @DisplayName("ON_HOLD ska vara modifierbar")
        void onHoldShouldBeModifiable() {
            Prescription prescription = new Prescription(patientId, prescriberId);
            prescription.activate();
            prescription.putOnHold("Test");

            assertTrue(prescription.isModifiable());
        }

        @Test
        @DisplayName("ACTIVE ska inte vara modifierbar")
        void activeShouldNotBeModifiable() {
            Prescription prescription = new Prescription(patientId, prescriberId);
            prescription.activate();

            assertFalse(prescription.isModifiable());
        }

        @Test
        @DisplayName("COMPLETED ska inte vara modifierbar")
        void completedShouldNotBeModifiable() {
            Prescription prescription = new Prescription(patientId, prescriberId);
            prescription.activate();
            prescription.complete();

            assertFalse(prescription.isModifiable());
        }
    }
}
