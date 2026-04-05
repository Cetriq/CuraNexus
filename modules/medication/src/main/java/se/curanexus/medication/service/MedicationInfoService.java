package se.curanexus.medication.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.curanexus.medication.adapter.fass.FassAdapter;
import se.curanexus.medication.adapter.fass.FassApiException;
import se.curanexus.medication.adapter.fass.dto.FassDocumentSection;
import se.curanexus.medication.adapter.fass.dto.FassMedicinalProduct;
import se.curanexus.medication.adapter.fass.dto.FassProductDocument;
import se.curanexus.medication.api.dto.MedicationInfoDto;
import se.curanexus.medication.api.dto.ProductDocumentDto;
import se.curanexus.medication.domain.Medication;
import se.curanexus.medication.repository.MedicationRepository;

import java.util.List;
import java.util.Optional;

/**
 * Service for medication information from external sources.
 * Combines local data with external data from Fass (REQ-MED-021).
 *
 * This service provides:
 * - Product information (indications, dosage, contraindications)
 * - SMPC sections for clinical use
 * - Patient information leaflets
 *
 * According to REQ-MED-061: Business logic stays here, not in adapters.
 * According to REQ-MED-062: Graceful degradation when Fass is unavailable.
 */
@Service
public class MedicationInfoService {

    private static final Logger log = LoggerFactory.getLogger(MedicationInfoService.class);

    private final FassAdapter fassAdapter;
    private final MedicationRepository medicationRepository;

    public MedicationInfoService(FassAdapter fassAdapter, MedicationRepository medicationRepository) {
        this.fassAdapter = fassAdapter;
        this.medicationRepository = medicationRepository;
    }

    /**
     * Get comprehensive medication info by NPL-ID.
     * Combines local data with Fass data (REQ-MED-033).
     *
     * @param nplId NPL identifier
     * @return Medication info including clinical data from Fass
     */
    public Optional<MedicationInfoDto> getMedicationInfo(String nplId) {
        log.debug("Getting medication info for NPL-ID: {}", nplId);

        // First try local database
        Optional<Medication> localMedication = medicationRepository.findByNplId(nplId);

        // Then fetch from Fass for additional clinical info
        try {
            Optional<FassMedicinalProduct> fassProduct = fassAdapter.getRawProductData(nplId);

            if (fassProduct.isEmpty() && localMedication.isEmpty()) {
                log.debug("Medication not found locally or in Fass: {}", nplId);
                return Optional.empty();
            }

            // Build the combined info
            MedicationInfoDto.Builder builder = MedicationInfoDto.builder();

            // Use local data as base if available
            localMedication.ifPresent(med -> {
                builder.id(med.getId())
                       .nplId(med.getNplId())
                       .name(med.getName())
                       .genericName(med.getGenericName())
                       .strength(med.getStrength())
                       .atcCode(med.getAtcCode())
                       .manufacturer(med.getManufacturer())
                       .prescriptionRequired(med.isPrescriptionRequired())
                       .narcotic(med.isNarcotic())
                       .narcoticClass(med.getNarcoticClass());
            });

            // Overlay with Fass data
            fassProduct.ifPresent(fass -> {
                builder.nplId(fass.nplId())
                       .name(fass.name())
                       .strength(fass.strengthText())
                       .atcCode(fass.atcCode())
                       .atcText(fass.atcText())
                       .manufacturer(fass.marketingAuthorizationHolder())
                       .pharmaceuticalForm(fass.pharmaceuticalForm())
                       .prescriptionRequired(fass.prescriptionRequired() != null && fass.prescriptionRequired())
                       .narcotic(fass.narcotic() != null && fass.narcotic())
                       .narcoticClass(fass.narcoticClass())
                       .substitutable(fass.substitutable() == null || fass.substitutable())
                       .marketingStatus(fass.marketingStatus())
                       .dataSource("FASS");

                // Extract generic name from active substances
                if (fass.activeSubstances() != null && !fass.activeSubstances().isEmpty()) {
                    String genericName = fass.activeSubstances().stream()
                            .map(s -> s.substanceName())
                            .reduce((a, b) -> a + " + " + b)
                            .orElse(null);
                    builder.genericName(genericName);

                    // Add active substances list
                    builder.activeSubstances(fass.activeSubstances().stream()
                            .map(s -> s.substanceName() + (s.strengthText() != null ? " " + s.strengthText() : ""))
                            .toList());
                }
            });

            // Fetch SMPC sections for clinical info
            enrichWithSmpcData(builder, nplId);

            return Optional.of(builder.build());

        } catch (FassApiException e) {
            log.warn("Fass API error for {}: {}. Falling back to local data.", nplId, e.getMessage());

            // Graceful degradation (REQ-MED-062): return local data if available
            return localMedication.map(med -> MedicationInfoDto.builder()
                    .id(med.getId())
                    .nplId(med.getNplId())
                    .name(med.getName())
                    .genericName(med.getGenericName())
                    .strength(med.getStrength())
                    .atcCode(med.getAtcCode())
                    .manufacturer(med.getManufacturer())
                    .prescriptionRequired(med.isPrescriptionRequired())
                    .narcotic(med.isNarcotic())
                    .narcoticClass(med.getNarcoticClass())
                    .dataSource("LOCAL")
                    .fassUnavailable(true)
                    .build());
        }
    }

    /**
     * Get indications (therapeutic uses) for a medication.
     *
     * @param nplId NPL identifier
     * @return Indications text
     */
    public Optional<String> getIndications(String nplId) {
        log.debug("Getting indications for: {}", nplId);
        return fassAdapter.getIndications(nplId);
    }

    /**
     * Get dosage guidelines for a medication.
     *
     * @param nplId NPL identifier
     * @return Dosage guidelines text
     */
    public Optional<String> getDosageGuidelines(String nplId) {
        log.debug("Getting dosage guidelines for: {}", nplId);
        return fassAdapter.getDosageGuidelines(nplId);
    }

    /**
     * Get contraindications for a medication.
     *
     * @param nplId NPL identifier
     * @return Contraindications text
     */
    public Optional<String> getContraindications(String nplId) {
        log.debug("Getting contraindications for: {}", nplId);
        return fassAdapter.getContraindications(nplId);
    }

    /**
     * Get warnings and precautions for a medication.
     *
     * @param nplId NPL identifier
     * @return Warnings text
     */
    public Optional<String> getWarnings(String nplId) {
        log.debug("Getting warnings for: {}", nplId);
        return fassAdapter.getWarnings(nplId);
    }

    /**
     * Get drug interactions for a medication.
     *
     * @param nplId NPL identifier
     * @return Interactions text
     */
    public Optional<String> getInteractions(String nplId) {
        log.debug("Getting interactions for: {}", nplId);
        return fassAdapter.getInteractions(nplId);
    }

    /**
     * Get pregnancy and breastfeeding info.
     *
     * @param nplId NPL identifier
     * @return Pregnancy info text
     */
    public Optional<String> getPregnancyInfo(String nplId) {
        log.debug("Getting pregnancy info for: {}", nplId);
        return fassAdapter.getPregnancyInfo(nplId);
    }

    /**
     * Get adverse reactions (side effects).
     *
     * @param nplId NPL identifier
     * @return Adverse reactions text
     */
    public Optional<String> getAdverseReactions(String nplId) {
        log.debug("Getting adverse reactions for: {}", nplId);
        return fassAdapter.getAdverseReactions(nplId);
    }

    /**
     * Get full SMPC (Summary of Product Characteristics).
     * For healthcare professionals.
     *
     * @param nplId NPL identifier
     * @return Full SMPC document
     */
    public Optional<ProductDocumentDto> getSmpc(String nplId) {
        log.debug("Getting full SMPC for: {}", nplId);

        return fassAdapter.getFullSmpc(nplId)
                .map(this::toProductDocumentDto);
    }

    /**
     * Get PIL (Patient Information Leaflet).
     * For patients.
     *
     * @param nplId NPL identifier
     * @return Patient information document
     */
    public Optional<ProductDocumentDto> getPatientInformation(String nplId) {
        log.debug("Getting PIL for: {}", nplId);

        return fassAdapter.getPatientInformation(nplId)
                .map(this::toProductDocumentDto);
    }

    /**
     * Search medications via Fass.
     *
     * @param query Search query
     * @param limit Maximum results
     * @return List of matching medications
     */
    public List<Medication> searchFass(String query, int limit) {
        log.debug("Searching Fass: query={}, limit={}", query, limit);

        try {
            return fassAdapter.searchMedications(query, limit);
        } catch (FassApiException e) {
            log.warn("Fass search failed: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * Get direct link to Fass for a medication.
     * Alternative to API when full integration isn't needed (REQ-MED-021).
     *
     * @param nplId NPL identifier
     * @return URL to Fass page
     */
    public String getFassLink(String nplId) {
        return "https://fass.se/LIF/product?nplId=" + nplId;
    }

    /**
     * Get direct link to patient information on Fass.
     *
     * @param nplId NPL identifier
     * @return URL to patient info page
     */
    public String getFassPatientLink(String nplId) {
        return "https://fass.se/LIF/product?nplId=" + nplId + "&type=fass";
    }

    /**
     * Check if Fass integration is available.
     */
    public boolean isFassAvailable() {
        return fassAdapter.isEnabled();
    }

    /**
     * Enrich medication info with SMPC data.
     */
    private void enrichWithSmpcData(MedicationInfoDto.Builder builder, String nplId) {
        try {
            fassAdapter.getIndications(nplId).ifPresent(builder::indications);
            fassAdapter.getContraindications(nplId).ifPresent(builder::contraindications);
            fassAdapter.getWarnings(nplId).ifPresent(builder::warnings);
        } catch (FassApiException e) {
            log.debug("Could not fetch SMPC sections for {}: {}", nplId, e.getMessage());
            // Continue without SMPC data - graceful degradation
        }
    }

    /**
     * Convert Fass document to DTO.
     */
    private ProductDocumentDto toProductDocumentDto(FassProductDocument doc) {
        List<ProductDocumentDto.Section> sections = doc.sections() != null
                ? doc.sections().stream()
                    .map(this::toSectionDto)
                    .toList()
                : List.of();

        return new ProductDocumentDto(
                doc.nplId(),
                doc.documentType(),
                sections,
                doc.lastUpdated(),
                doc.approvalDate()
        );
    }

    /**
     * Convert Fass section to DTO.
     */
    private ProductDocumentDto.Section toSectionDto(FassDocumentSection section) {
        return new ProductDocumentDto.Section(
                section.sectionNumber(),
                section.sectionTitle(),
                section.content(),
                section.htmlContent()
        );
    }
}
