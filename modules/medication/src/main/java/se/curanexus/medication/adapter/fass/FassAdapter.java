package se.curanexus.medication.adapter.fass;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import se.curanexus.medication.adapter.fass.dto.FassDocumentSection;
import se.curanexus.medication.adapter.fass.dto.FassMedicinalProduct;
import se.curanexus.medication.adapter.fass.dto.FassProductDocument;
import se.curanexus.medication.domain.Medication;

import java.util.List;
import java.util.Optional;

/**
 * Adapter for Fass integration.
 * Isolates external Fass API behind a clean interface (REQ-MED-060).
 * No business logic here - only data transformation (REQ-MED-061).
 *
 * Fass provides:
 * - Product information (name, strength, form, ATC)
 * - SMPC (Summary of Product Characteristics) - produktresume
 * - PIL (Patient Information Leaflet) - bipacksedel
 *
 * @see <a href="https://api.fass.se/documentation">Fass API Documentation</a>
 */
@Component
public class FassAdapter {

    private static final Logger log = LoggerFactory.getLogger(FassAdapter.class);

    private final FassApiOperations apiClient;
    private final FassProductMapper mapper;
    private final FassApiProperties properties;

    public FassAdapter(FassApiOperations apiClient, FassProductMapper mapper, FassApiProperties properties) {
        this.apiClient = apiClient;
        this.mapper = mapper;
        this.properties = properties;
    }

    /**
     * Check if Fass integration is enabled.
     */
    public boolean isEnabled() {
        return properties.isEnabled();
    }

    /**
     * Fetch medication info from Fass by NPL-ID.
     *
     * @param nplId NPL identifier
     * @return Medication domain object, empty if not found
     */
    public Optional<Medication> fetchMedicationByNplId(String nplId) {
        log.debug("Fetching medication from Fass: nplId={}", nplId);

        return apiClient.getMedicinalProduct(nplId)
                .map(mapper::toDomain);
    }

    /**
     * Search for medications by name.
     *
     * @param query Search query
     * @param limit Maximum results
     * @return List of matching medications
     */
    public List<Medication> searchMedications(String query, int limit) {
        log.debug("Searching Fass: query={}", query);

        return apiClient.searchProducts(query, limit).stream()
                .map(mapper::toDomain)
                .toList();
    }

    /**
     * Get medications by ATC code.
     *
     * @param atcCode ATC code (e.g., "N02BE01")
     * @return List of medications with this ATC code
     */
    public List<Medication> getMedicationsByAtcCode(String atcCode) {
        log.debug("Fetching from Fass by ATC: {}", atcCode);

        return apiClient.getProductsByAtcCode(atcCode).stream()
                .map(mapper::toDomain)
                .toList();
    }

    /**
     * Get medications by ATC group.
     *
     * @param atcGroup ATC group prefix (e.g., "N02")
     * @return List of medications in this group
     */
    public List<Medication> getMedicationsByAtcGroup(String atcGroup) {
        log.debug("Fetching from Fass by ATC group: {}", atcGroup);

        return apiClient.getProductsByAtcGroup(atcGroup).stream()
                .map(mapper::toDomain)
                .toList();
    }

    /**
     * Get indications (section 4.1) from SMPC.
     *
     * @param nplId NPL identifier
     * @return Indications text, empty if not available
     */
    public Optional<String> getIndications(String nplId) {
        return getSmpcSection(nplId, "4.1");
    }

    /**
     * Get dosage and administration (section 4.2) from SMPC.
     *
     * @param nplId NPL identifier
     * @return Dosage text, empty if not available
     */
    public Optional<String> getDosageGuidelines(String nplId) {
        return getSmpcSection(nplId, "4.2");
    }

    /**
     * Get contraindications (section 4.3) from SMPC.
     *
     * @param nplId NPL identifier
     * @return Contraindications text, empty if not available
     */
    public Optional<String> getContraindications(String nplId) {
        return getSmpcSection(nplId, "4.3");
    }

    /**
     * Get warnings and precautions (section 4.4) from SMPC.
     *
     * @param nplId NPL identifier
     * @return Warnings text, empty if not available
     */
    public Optional<String> getWarnings(String nplId) {
        return getSmpcSection(nplId, "4.4");
    }

    /**
     * Get interactions (section 4.5) from SMPC.
     *
     * @param nplId NPL identifier
     * @return Interactions text, empty if not available
     */
    public Optional<String> getInteractions(String nplId) {
        return getSmpcSection(nplId, "4.5");
    }

    /**
     * Get pregnancy and lactation info (section 4.6) from SMPC.
     *
     * @param nplId NPL identifier
     * @return Pregnancy info, empty if not available
     */
    public Optional<String> getPregnancyInfo(String nplId) {
        return getSmpcSection(nplId, "4.6");
    }

    /**
     * Get adverse reactions (section 4.8) from SMPC.
     *
     * @param nplId NPL identifier
     * @return Adverse reactions text, empty if not available
     */
    public Optional<String> getAdverseReactions(String nplId) {
        return getSmpcSection(nplId, "4.8");
    }

    /**
     * Get full SMPC document.
     *
     * @param nplId NPL identifier
     * @return Full SMPC document, empty if not available
     */
    public Optional<FassProductDocument> getFullSmpc(String nplId) {
        log.debug("Fetching full SMPC from Fass: {}", nplId);
        return apiClient.getSmpc(nplId);
    }

    /**
     * Get patient information leaflet (PIL).
     *
     * @param nplId NPL identifier
     * @return PIL document, empty if not available
     */
    public Optional<FassProductDocument> getPatientInformation(String nplId) {
        log.debug("Fetching PIL from Fass: {}", nplId);
        return apiClient.getPil(nplId);
    }

    /**
     * Get raw Fass product data (for cases where full mapping isn't needed).
     *
     * @param nplId NPL identifier
     * @return Raw Fass product data
     */
    public Optional<FassMedicinalProduct> getRawProductData(String nplId) {
        return apiClient.getMedicinalProduct(nplId);
    }

    /**
     * Get a specific section from SMPC.
     */
    private Optional<String> getSmpcSection(String nplId, String sectionNumber) {
        log.debug("Fetching SMPC section {} for: {}", sectionNumber, nplId);

        return apiClient.getSmpc(nplId)
                .flatMap(doc -> findSection(doc, sectionNumber))
                .map(FassDocumentSection::content);
    }

    /**
     * Find a section by number in the document.
     */
    private Optional<FassDocumentSection> findSection(FassProductDocument doc, String sectionNumber) {
        if (doc.sections() == null) {
            return Optional.empty();
        }

        return doc.sections().stream()
                .filter(s -> sectionNumber.equals(s.sectionNumber()) ||
                            (s.sectionNumber() != null && s.sectionNumber().startsWith(sectionNumber + ".")))
                .findFirst();
    }
}
