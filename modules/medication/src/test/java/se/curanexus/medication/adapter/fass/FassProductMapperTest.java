package se.curanexus.medication.adapter.fass;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import se.curanexus.medication.adapter.fass.dto.FassActiveSubstance;
import se.curanexus.medication.adapter.fass.dto.FassMedicinalProduct;
import se.curanexus.medication.adapter.fass.dto.FassPackage;
import se.curanexus.medication.domain.DosageForm;
import se.curanexus.medication.domain.Medication;
import se.curanexus.medication.domain.RouteOfAdministration;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FassProductMapper Tests")
class FassProductMapperTest {

    private FassProductMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new FassProductMapper();
    }

    @Test
    @DisplayName("should map Fass product to Medication domain object")
    void shouldMapFassProductToMedication() {
        // Given
        FassMedicinalProduct fassProduct = new FassMedicinalProduct(
                "20010131000022",                    // nplId
                "Alvedon",                           // name
                "500 mg",                            // strengthText
                500.0,                               // strengthNumeric
                "mg",                                // strengthUnit
                "Tablett",                           // pharmaceuticalForm
                "N02BE01",                           // atcCode
                "Paracetamol",                       // atcText
                "GlaxoSmithKline Consumer Healthcare", // marketingAuthorizationHolder
                List.of(new FassActiveSubstance(    // activeSubstances
                        "S001",
                        "Paracetamol",
                        "500 mg",
                        500.0,
                        "mg"
                )),
                List.of(new FassPackage(            // packages
                        "20010131000022-1",
                        "100 tabletter",
                        100,
                        "tabletter",
                        "Burk",
                        new BigDecimal("45.00"),
                        new BigDecimal("59.00"),
                        true,
                        "Tillgänglig",
                        false
                )),
                false,                               // prescriptionRequired
                false,                               // narcotic
                null,                                // narcoticClass
                true,                                // humanMedicineOnly
                true,                                // substitutable
                "Saluförs",                          // marketingStatus
                true,                                // approved
                "1999-01-01",                        // approvalDate
                false,                               // parallelImport
                null                                 // exportCountry
        );

        // When
        Medication medication = mapper.toDomain(fassProduct);

        // Then
        assertNotNull(medication);
        assertEquals("Alvedon", medication.getName());
        assertEquals("Paracetamol", medication.getGenericName());
        assertEquals("20010131000022", medication.getNplId());
        assertEquals("N02BE01", medication.getAtcCode());
        assertEquals("500 mg", medication.getStrength());
        assertEquals(new BigDecimal("500.0"), medication.getStrengthValue());
        assertEquals("mg", medication.getStrengthUnit());
        assertEquals(DosageForm.TABLET, medication.getDosageForm());
        assertEquals(RouteOfAdministration.ORAL, medication.getRoute());
        assertEquals("GlaxoSmithKline Consumer Healthcare", medication.getManufacturer());
        assertFalse(medication.isPrescriptionRequired());
        assertFalse(medication.isNarcotic());
        assertTrue(medication.isSubstitutable());
        assertTrue(medication.isActive());
    }

    @Test
    @DisplayName("should return null for null input")
    void shouldReturnNullForNullInput() {
        assertNull(mapper.toDomain(null));
    }

    @Test
    @DisplayName("should map extended release tablet correctly")
    void shouldMapExtendedReleaseTablet() {
        FassMedicinalProduct fassProduct = createProductWithForm("Depottablett");

        Medication medication = mapper.toDomain(fassProduct);

        // Depottablett maps to TABLET (since TABLET_EXTENDED_RELEASE doesn't exist)
        assertEquals(DosageForm.TABLET, medication.getDosageForm());
        assertEquals(RouteOfAdministration.ORAL, medication.getRoute());
    }

    @Test
    @DisplayName("should map effervescent tablet correctly")
    void shouldMapEffervescentTablet() {
        FassMedicinalProduct fassProduct = createProductWithForm("Brustablett");

        Medication medication = mapper.toDomain(fassProduct);

        assertEquals(DosageForm.EFFERVESCENT_TABLET, medication.getDosageForm());
        assertEquals(RouteOfAdministration.ORAL, medication.getRoute());
    }

    @Test
    @DisplayName("should map injection solution correctly")
    void shouldMapInjectionSolution() {
        FassMedicinalProduct fassProduct = createProductWithForm("Injektionsvätska, lösning");

        Medication medication = mapper.toDomain(fassProduct);

        assertEquals(DosageForm.INJECTION, medication.getDosageForm());
        assertEquals(RouteOfAdministration.SUBCUTANEOUS, medication.getRoute());
    }

    @Test
    @DisplayName("should map infusion solution correctly")
    void shouldMapInfusionSolution() {
        FassMedicinalProduct fassProduct = createProductWithForm("Infusionsvätska, lösning");

        Medication medication = mapper.toDomain(fassProduct);

        assertEquals(DosageForm.INFUSION, medication.getDosageForm());
        assertEquals(RouteOfAdministration.INTRAVENOUS, medication.getRoute());
    }

    @Test
    @DisplayName("should map cream correctly")
    void shouldMapCream() {
        FassMedicinalProduct fassProduct = createProductWithForm("Kräm");

        Medication medication = mapper.toDomain(fassProduct);

        assertEquals(DosageForm.CREAM, medication.getDosageForm());
        assertEquals(RouteOfAdministration.TOPICAL, medication.getRoute());
    }

    @Test
    @DisplayName("should map transdermal patch correctly")
    void shouldMapTransdermalPatch() {
        FassMedicinalProduct fassProduct = createProductWithForm("Depotplåster");

        Medication medication = mapper.toDomain(fassProduct);

        assertEquals(DosageForm.PATCH, medication.getDosageForm());
        assertEquals(RouteOfAdministration.TRANSDERMAL, medication.getRoute());
    }

    @Test
    @DisplayName("should map suppository correctly")
    void shouldMapSuppository() {
        FassMedicinalProduct fassProduct = createProductWithForm("Suppositorium");

        Medication medication = mapper.toDomain(fassProduct);

        assertEquals(DosageForm.SUPPOSITORY, medication.getDosageForm());
        assertEquals(RouteOfAdministration.RECTAL, medication.getRoute());
    }

    @Test
    @DisplayName("should map eye drops correctly")
    void shouldMapEyeDrops() {
        FassMedicinalProduct fassProduct = createProductWithForm("Ögondroppar, lösning");

        Medication medication = mapper.toDomain(fassProduct);

        assertEquals(DosageForm.EYE_DROPS, medication.getDosageForm());
        assertEquals(RouteOfAdministration.OPHTHALMIC, medication.getRoute());
    }

    @Test
    @DisplayName("should map inhaler correctly")
    void shouldMapInhaler() {
        FassMedicinalProduct fassProduct = createProductWithForm("Inhalationsspray");

        Medication medication = mapper.toDomain(fassProduct);

        assertEquals(DosageForm.INHALER, medication.getDosageForm());
        assertEquals(RouteOfAdministration.INHALATION, medication.getRoute());
    }

    @Test
    @DisplayName("should map narcotic medication correctly")
    void shouldMapNarcoticMedication() {
        FassMedicinalProduct fassProduct = new FassMedicinalProduct(
                "20010131000099",
                "OxyContin",
                "10 mg",
                10.0,
                "mg",
                "Depottablett",
                "N02AA05",
                "Oxykodon",
                "Mundipharma",
                List.of(new FassActiveSubstance("S002", "Oxykodonhydroklorid", "10 mg", 10.0, "mg")),
                null,
                true,                                // prescriptionRequired
                true,                                // narcotic
                "II",                                // narcoticClass
                true,
                false,                               // not substitutable
                "Saluförs",
                true,
                "2005-01-01",
                false,
                null
        );

        Medication medication = mapper.toDomain(fassProduct);

        assertTrue(medication.isPrescriptionRequired());
        assertTrue(medication.isNarcotic());
        assertEquals("II", medication.getNarcoticClass());
        assertFalse(medication.isSubstitutable());
    }

    @Test
    @DisplayName("should handle withdrawn product correctly")
    void shouldHandleWithdrawnProduct() {
        FassMedicinalProduct fassProduct = new FassMedicinalProduct(
                "20010131000088",
                "Avregistrerat Läkemedel",
                "100 mg",
                100.0,
                "mg",
                "Tablett",
                "N02BE01",
                "Substans",
                "Tillverkare",
                null,
                null,
                false,
                false,
                null,
                true,
                true,
                "Avregistrerad",                     // withdrawn status
                false,
                "2000-01-01",
                false,
                null
        );

        Medication medication = mapper.toDomain(fassProduct);

        assertFalse(medication.isActive());
    }

    @Test
    @DisplayName("should combine multiple active substances")
    void shouldCombineMultipleActiveSubstances() {
        FassMedicinalProduct fassProduct = new FassMedicinalProduct(
                "20010131000077",
                "Citodon",
                "500 mg/30 mg",
                null,
                null,
                "Tablett",
                "N02AJ06",
                "Kombinationer av kodein",
                "Meda",
                List.of(
                        new FassActiveSubstance("S001", "Paracetamol", "500 mg", 500.0, "mg"),
                        new FassActiveSubstance("S002", "Kodeinfosfathemihydrat", "30 mg", 30.0, "mg")
                ),
                null,
                true,
                true,
                "III",
                true,
                false,
                "Saluförs",
                true,
                "1990-01-01",
                false,
                null
        );

        Medication medication = mapper.toDomain(fassProduct);

        assertEquals("Paracetamol + Kodeinfosfathemihydrat", medication.getGenericName());
    }

    /**
     * Helper method to create a product with a specific pharmaceutical form.
     */
    private FassMedicinalProduct createProductWithForm(String pharmaceuticalForm) {
        return new FassMedicinalProduct(
                "20010131000001",
                "TestMedicin",
                "100 mg",
                100.0,
                "mg",
                pharmaceuticalForm,
                "N02BE01",
                "Test",
                "Tillverkare",
                null,
                null,
                false,
                false,
                null,
                true,
                true,
                "Saluförs",
                true,
                "2000-01-01",
                false,
                null
        );
    }
}
