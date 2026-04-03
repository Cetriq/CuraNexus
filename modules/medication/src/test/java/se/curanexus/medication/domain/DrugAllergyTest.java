package se.curanexus.medication.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import se.curanexus.medication.domain.DrugAllergy.AllergySeverity;
import se.curanexus.medication.domain.DrugAllergy.ReactionType;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DrugAllergy domänentitet")
class DrugAllergyTest {

    private UUID patientId;

    @BeforeEach
    void setUp() {
        patientId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("Skapande")
    class Creation {

        @Test
        @DisplayName("Ska skapa allergi med korrekta värden")
        void shouldCreateWithCorrectValues() {
            DrugAllergy allergy = new DrugAllergy(
                    patientId,
                    "Penicillin",
                    ReactionType.ALLERGY
            );

            assertEquals(patientId, allergy.getPatientId());
            assertEquals("Penicillin", allergy.getSubstanceName());
            assertEquals(ReactionType.ALLERGY, allergy.getReactionType());
            assertTrue(allergy.isActive());
            assertFalse(allergy.isVerified());
            assertNotNull(allergy.getCreatedAt());
        }

        @Test
        @DisplayName("Ska kunna sätta allvarlighetsgrad")
        void shouldSetSeverity() {
            DrugAllergy allergy = new DrugAllergy(patientId, "Penicillin", ReactionType.ALLERGY);
            allergy.setSeverity(AllergySeverity.SEVERE);

            assertEquals(AllergySeverity.SEVERE, allergy.getSeverity());
        }

        @Test
        @DisplayName("Ska kunna sätta ATC-kod")
        void shouldSetAtcCode() {
            DrugAllergy allergy = new DrugAllergy(patientId, "Amoxicillin", ReactionType.ALLERGY);
            allergy.setAtcCode("J01CA04");

            assertEquals("J01CA04", allergy.getAtcCode());
        }
    }

    @Nested
    @DisplayName("Verifiering")
    class Verification {

        @Test
        @DisplayName("Ska kunna verifiera allergi")
        void shouldVerify() {
            DrugAllergy allergy = new DrugAllergy(patientId, "Penicillin", ReactionType.ALLERGY);
            UUID verifiedById = UUID.randomUUID();

            allergy.verify(verifiedById);

            assertTrue(allergy.isVerified());
            assertEquals(verifiedById, allergy.getVerifiedById());
            assertNotNull(allergy.getVerifiedAt());
        }
    }

    @Nested
    @DisplayName("Inaktivering")
    class Deactivation {

        @Test
        @DisplayName("Ska kunna inaktivera allergi")
        void shouldDeactivate() {
            DrugAllergy allergy = new DrugAllergy(patientId, "Penicillin", ReactionType.ALLERGY);

            allergy.deactivate("Negativ provokation");

            assertFalse(allergy.isActive());
            assertTrue(allergy.getNotes().contains("Inaktiverad"));
        }
    }

    @Nested
    @DisplayName("Reaktionstyper")
    class ReactionTypes {

        @Test
        @DisplayName("Ska kunna skapa allergi")
        void shouldCreateAllergy() {
            DrugAllergy allergy = new DrugAllergy(patientId, "Penicillin", ReactionType.ALLERGY);
            assertEquals(ReactionType.ALLERGY, allergy.getReactionType());
        }

        @Test
        @DisplayName("Ska kunna skapa intolerans")
        void shouldCreateIntolerance() {
            DrugAllergy allergy = new DrugAllergy(patientId, "Kodein", ReactionType.INTOLERANCE);
            allergy.setReactionDescription("Illamående och kräkningar");

            assertEquals(ReactionType.INTOLERANCE, allergy.getReactionType());
            assertEquals("Illamående och kräkningar", allergy.getReactionDescription());
        }

        @Test
        @DisplayName("Ska kunna skapa biverkning")
        void shouldCreateSideEffect() {
            DrugAllergy allergy = new DrugAllergy(patientId, "Metformin", ReactionType.SIDE_EFFECT);
            allergy.setReactionDescription("Mag-tarmbesvär");

            assertEquals(ReactionType.SIDE_EFFECT, allergy.getReactionType());
        }
    }

    @Nested
    @DisplayName("Allvarlighetsgrader")
    class SeverityLevels {

        @Test
        @DisplayName("Ska kunna sätta mild reaktion")
        void shouldSetMildSeverity() {
            DrugAllergy allergy = new DrugAllergy(patientId, "Aspirin", ReactionType.INTOLERANCE);
            allergy.setSeverity(AllergySeverity.MILD);

            assertEquals(AllergySeverity.MILD, allergy.getSeverity());
        }

        @Test
        @DisplayName("Ska kunna sätta livshotande reaktion")
        void shouldSetLifeThreateningSeverity() {
            DrugAllergy allergy = new DrugAllergy(patientId, "Penicillin", ReactionType.ALLERGY);
            allergy.setSeverity(AllergySeverity.LIFE_THREATENING);
            allergy.setReactionDescription("Anafylaktisk chock");

            assertEquals(AllergySeverity.LIFE_THREATENING, allergy.getSeverity());
        }
    }
}
