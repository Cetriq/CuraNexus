package se.curanexus.medication.adapter.fass;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import se.curanexus.medication.adapter.fass.dto.*;
import se.curanexus.medication.domain.Medication;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FassAdapter Tests")
class FassAdapterTest {

    private FassAdapter adapter;
    private FassProductMapper mapper;
    private TestFassApiOperations testApiClient;
    private FassApiProperties properties;

    @BeforeEach
    void setUp() {
        mapper = new FassProductMapper();
        testApiClient = new TestFassApiOperations();
        properties = new FassApiProperties();
        properties.setEnabled(true);
        adapter = new FassAdapter(testApiClient, mapper, properties);
    }

    @Test
    @DisplayName("should fetch medication by NPL-ID when enabled")
    void shouldFetchMedicationByNplIdWhenEnabled() {
        // Given
        FassMedicinalProduct fassProduct = createTestProduct("20010131000022", "Alvedon");
        testApiClient.setProductToReturn(fassProduct);

        // When
        Optional<Medication> result = adapter.fetchMedicationByNplId("20010131000022");

        // Then
        assertTrue(result.isPresent());
        assertEquals("Alvedon", result.get().getName());
    }

    @Test
    @DisplayName("should return empty when product not found")
    void shouldReturnEmptyWhenProductNotFound() {
        // Given - no product set

        // When
        Optional<Medication> result = adapter.fetchMedicationByNplId("nonexistent");

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("should search medications")
    void shouldSearchMedications() {
        // Given
        List<FassMedicinalProduct> fassProducts = List.of(
                createTestProduct("20010131000022", "Alvedon"),
                createTestProduct("20010131000023", "Alvedon Forte")
        );
        testApiClient.setSearchResults(fassProducts);

        // When
        List<Medication> results = adapter.searchMedications("Alvedon", 10);

        // Then
        assertEquals(2, results.size());
        assertEquals("Alvedon", results.get(0).getName());
        assertEquals("Alvedon Forte", results.get(1).getName());
    }

    @Test
    @DisplayName("should get indications from SMPC")
    void shouldGetIndicationsFromSmpc() {
        // Given
        FassProductDocument smpc = createSmpcWithSection("4.1", "Indikationer", "Behandling av lätt till måttlig smärta");
        testApiClient.setSmpcToReturn(smpc);

        // When
        Optional<String> result = adapter.getIndications("20010131000022");

        // Then
        assertTrue(result.isPresent());
        assertEquals("Behandling av lätt till måttlig smärta", result.get());
    }

    @Test
    @DisplayName("should get contraindications from SMPC")
    void shouldGetContraindicationsFromSmpc() {
        // Given
        FassProductDocument smpc = createSmpcWithSection("4.3", "Kontraindikationer", "Överkänslighet mot paracetamol");
        testApiClient.setSmpcToReturn(smpc);

        // When
        Optional<String> result = adapter.getContraindications("20010131000022");

        // Then
        assertTrue(result.isPresent());
        assertEquals("Överkänslighet mot paracetamol", result.get());
    }

    @Test
    @DisplayName("should get full SMPC document")
    void shouldGetFullSmpcDocument() {
        // Given
        FassProductDocument smpc = new FassProductDocument(
                "20010131000022",
                "SMPC",
                List.of(
                        new FassDocumentSection("1", "4.1", "Indikationer", "Smärtlindring", null),
                        new FassDocumentSection("2", "4.2", "Dosering", "1-2 tabletter", null),
                        new FassDocumentSection("3", "4.3", "Kontraindikationer", "Allergi", null)
                ),
                "2024-01-15",
                "1999-01-01"
        );
        testApiClient.setSmpcToReturn(smpc);

        // When
        Optional<FassProductDocument> result = adapter.getFullSmpc("20010131000022");

        // Then
        assertTrue(result.isPresent());
        assertEquals(3, result.get().sections().size());
    }

    @Test
    @DisplayName("should check if integration is enabled")
    void shouldCheckIfIntegrationIsEnabled() {
        // When/Then
        assertTrue(adapter.isEnabled());

        // Given - disable
        properties.setEnabled(false);

        // When/Then
        assertFalse(adapter.isEnabled());
    }

    @Test
    @DisplayName("should get medications by ATC code")
    void shouldGetMedicationsByAtcCode() {
        // Given
        List<FassMedicinalProduct> products = List.of(
                createTestProduct("20010131000022", "Alvedon"),
                createTestProduct("20010131000024", "Panodil")
        );
        testApiClient.setAtcCodeResults(products);

        // When
        List<Medication> results = adapter.getMedicationsByAtcCode("N02BE01");

        // Then
        assertEquals(2, results.size());
    }

    @Test
    @DisplayName("should get patient information leaflet")
    void shouldGetPatientInformationLeaflet() {
        // Given
        FassProductDocument pil = new FassProductDocument(
                "20010131000022",
                "PIL",
                List.of(new FassDocumentSection("1", "1", "Vad är Alvedon", "Ett smärtstillande läkemedel", null)),
                "2024-01-15",
                null
        );
        testApiClient.setPilToReturn(pil);

        // When
        Optional<FassProductDocument> result = adapter.getPatientInformation("20010131000022");

        // Then
        assertTrue(result.isPresent());
        assertEquals("PIL", result.get().documentType());
    }

    /**
     * Helper to create a test Fass product.
     */
    private FassMedicinalProduct createTestProduct(String nplId, String name) {
        return new FassMedicinalProduct(
                nplId,
                name,
                "500 mg",
                500.0,
                "mg",
                "Tablett",
                "N02BE01",
                "Paracetamol",
                "Tillverkare",
                List.of(new FassActiveSubstance("S001", "Paracetamol", "500 mg", 500.0, "mg")),
                null,
                false,
                false,
                null,
                true,
                true,
                "Saluförs",
                true,
                "1999-01-01",
                false,
                null
        );
    }

    /**
     * Helper to create SMPC with a specific section.
     */
    private FassProductDocument createSmpcWithSection(String sectionNumber, String title, String content) {
        return new FassProductDocument(
                "20010131000022",
                "SMPC",
                List.of(new FassDocumentSection("1", sectionNumber, title, content, null)),
                "2024-01-15",
                "1999-01-01"
        );
    }

    /**
     * Test implementation of FassApiOperations for unit testing.
     */
    private static class TestFassApiOperations implements FassApiOperations {
        private FassMedicinalProduct productToReturn;
        private List<FassMedicinalProduct> searchResults = List.of();
        private List<FassMedicinalProduct> atcCodeResults = List.of();
        private FassProductDocument smpcToReturn;
        private FassProductDocument pilToReturn;

        void setProductToReturn(FassMedicinalProduct product) {
            this.productToReturn = product;
        }

        void setSearchResults(List<FassMedicinalProduct> results) {
            this.searchResults = results;
        }

        void setAtcCodeResults(List<FassMedicinalProduct> results) {
            this.atcCodeResults = results;
        }

        void setSmpcToReturn(FassProductDocument smpc) {
            this.smpcToReturn = smpc;
        }

        void setPilToReturn(FassProductDocument pil) {
            this.pilToReturn = pil;
        }

        @Override
        public Optional<FassMedicinalProduct> getMedicinalProduct(String nplId) {
            return Optional.ofNullable(productToReturn);
        }

        @Override
        public List<FassMedicinalProduct> searchProducts(String query, int limit) {
            return searchResults;
        }

        @Override
        public List<FassMedicinalProduct> getProductsByAtcCode(String atcCode) {
            return atcCodeResults;
        }

        @Override
        public List<FassMedicinalProduct> getProductsByAtcGroup(String atcGroup) {
            return atcCodeResults;
        }

        @Override
        public Optional<FassProductDocument> getSmpc(String nplId) {
            return Optional.ofNullable(smpcToReturn);
        }

        @Override
        public Optional<FassProductDocument> getPil(String nplId) {
            return Optional.ofNullable(pilToReturn);
        }

        @Override
        public List<FassAtcCode> getAtcCodes() {
            return List.of();
        }
    }
}
