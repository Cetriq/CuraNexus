package se.curanexus.medication.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Medication domänentitet")
class MedicationTest {

    @Nested
    @DisplayName("Skapande")
    class Creation {

        @Test
        @DisplayName("Ska skapa läkemedel med korrekta värden")
        void shouldCreateWithCorrectValues() {
            Medication medication = new Medication("Alvedon", "Paracetamol");

            assertEquals("Alvedon", medication.getName());
            assertEquals("Paracetamol", medication.getGenericName());
            assertTrue(medication.isActive());
            assertTrue(medication.isPrescriptionRequired());
            assertTrue(medication.isSubstitutable());
            assertFalse(medication.isNarcotic());
            assertNotNull(medication.getCreatedAt());
        }
    }

    @Nested
    @DisplayName("Fullständig beskrivning")
    class FullDescription {

        @Test
        @DisplayName("Ska generera beskrivning med endast namn")
        void shouldGenerateDescriptionWithNameOnly() {
            Medication medication = new Medication("Alvedon", "Paracetamol");

            assertEquals("Alvedon", medication.getFullDescription());
        }

        @Test
        @DisplayName("Ska generera beskrivning med styrka")
        void shouldGenerateDescriptionWithStrength() {
            Medication medication = new Medication("Alvedon", "Paracetamol");
            medication.setStrength("500 mg");

            assertEquals("Alvedon 500 mg", medication.getFullDescription());
        }

        @Test
        @DisplayName("Ska generera fullständig beskrivning")
        void shouldGenerateFullDescription() {
            Medication medication = new Medication("Alvedon", "Paracetamol");
            medication.setStrength("500 mg");
            medication.setDosageForm(DosageForm.TABLET);

            assertEquals("Alvedon 500 mg tablet", medication.getFullDescription());
        }
    }

    @Nested
    @DisplayName("Läkemedelsinformation")
    class MedicationInfo {

        @Test
        @DisplayName("Ska kunna sätta NPL-information")
        void shouldSetNplInfo() {
            Medication medication = new Medication("Alvedon", "Paracetamol");
            medication.setNplId("20010101000001");
            medication.setNplPackId("20010101000001001");

            assertEquals("20010101000001", medication.getNplId());
            assertEquals("20010101000001001", medication.getNplPackId());
        }

        @Test
        @DisplayName("Ska kunna sätta ATC-kod")
        void shouldSetAtcCode() {
            Medication medication = new Medication("Alvedon", "Paracetamol");
            medication.setAtcCode("N02BE01");

            assertEquals("N02BE01", medication.getAtcCode());
        }

        @Test
        @DisplayName("Ska kunna sätta styrka numeriskt")
        void shouldSetStrengthNumerically() {
            Medication medication = new Medication("Alvedon", "Paracetamol");
            medication.setStrengthValue(new BigDecimal("500"));
            medication.setStrengthUnit("mg");

            assertEquals(new BigDecimal("500"), medication.getStrengthValue());
            assertEquals("mg", medication.getStrengthUnit());
        }

        @Test
        @DisplayName("Ska kunna sätta förpackningsinformation")
        void shouldSetPackageInfo() {
            Medication medication = new Medication("Alvedon", "Paracetamol");
            medication.setPackageSize(100);
            medication.setPackageUnit("st");

            assertEquals(100, medication.getPackageSize());
            assertEquals("st", medication.getPackageUnit());
        }
    }

    @Nested
    @DisplayName("Narkotikaklassning")
    class NarcoticClassification {

        @Test
        @DisplayName("Ska kunna markera som narkotikaklassad")
        void shouldMarkAsNarcotic() {
            Medication medication = new Medication("Oxynorm", "Oxikodon");
            medication.setNarcotic(true);
            medication.setNarcoticClass("II");

            assertTrue(medication.isNarcotic());
            assertEquals("II", medication.getNarcoticClass());
        }

        @Test
        @DisplayName("Ska kunna markera missbrukspotential")
        void shouldMarkAbusePotential() {
            Medication medication = new Medication("Tramadol", "Tramadol");
            medication.setAbusePotential(true);

            assertTrue(medication.isAbusePotential());
        }
    }

    @Nested
    @DisplayName("Utbytbarhet")
    class Substitutability {

        @Test
        @DisplayName("Ska vara utbytbar som standard")
        void shouldBeSubstitutableByDefault() {
            Medication medication = new Medication("Alvedon", "Paracetamol");

            assertTrue(medication.isSubstitutable());
        }

        @Test
        @DisplayName("Ska kunna markera som ej utbytbar")
        void shouldMarkAsNotSubstitutable() {
            Medication medication = new Medication("Lantus", "Insulin glargin");
            medication.setSubstitutable(false);

            assertFalse(medication.isSubstitutable());
        }
    }

    @Nested
    @DisplayName("Receptstatus")
    class PrescriptionStatus {

        @Test
        @DisplayName("Ska vara receptbelagd som standard")
        void shouldRequirePrescriptionByDefault() {
            Medication medication = new Medication("Alvedon", "Paracetamol");

            assertTrue(medication.isPrescriptionRequired());
        }

        @Test
        @DisplayName("Ska kunna markera som receptfri")
        void shouldMarkAsOtc() {
            Medication medication = new Medication("Ipren", "Ibuprofen");
            medication.setPrescriptionRequired(false);

            assertFalse(medication.isPrescriptionRequired());
        }
    }

    @Nested
    @DisplayName("Administreringsväg")
    class RouteOfAdministrationTest {

        @Test
        @DisplayName("Ska kunna sätta oral administration")
        void shouldSetOralRoute() {
            Medication medication = new Medication("Alvedon", "Paracetamol");
            medication.setDosageForm(DosageForm.TABLET);
            medication.setRoute(RouteOfAdministration.ORAL);

            assertEquals(DosageForm.TABLET, medication.getDosageForm());
            assertEquals(RouteOfAdministration.ORAL, medication.getRoute());
        }

        @Test
        @DisplayName("Ska kunna sätta injektion")
        void shouldSetInjectionRoute() {
            Medication medication = new Medication("Novorapid", "Insulin aspart");
            medication.setDosageForm(DosageForm.INJECTION);
            medication.setRoute(RouteOfAdministration.SUBCUTANEOUS);

            assertEquals(DosageForm.INJECTION, medication.getDosageForm());
            assertEquals(RouteOfAdministration.SUBCUTANEOUS, medication.getRoute());
        }
    }
}
