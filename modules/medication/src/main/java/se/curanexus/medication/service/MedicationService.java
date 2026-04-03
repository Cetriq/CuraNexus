package se.curanexus.medication.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.curanexus.medication.api.dto.MedicationDto;
import se.curanexus.medication.domain.Medication;
import se.curanexus.medication.repository.MedicationRepository;

import java.util.List;
import java.util.UUID;

/**
 * Service för läkemedelsregister (referensdata).
 */
@Service
@Transactional(readOnly = true)
public class MedicationService {

    private static final Logger log = LoggerFactory.getLogger(MedicationService.class);

    private final MedicationRepository medicationRepository;

    public MedicationService(MedicationRepository medicationRepository) {
        this.medicationRepository = medicationRepository;
    }

    /**
     * Hämta läkemedel via ID.
     */
    public MedicationDto getMedication(UUID medicationId) {
        return medicationRepository.findById(medicationId)
                .map(MedicationDto::from)
                .orElseThrow(() -> new MedicationNotFoundException(medicationId));
    }

    /**
     * Hämta läkemedel via NPL-ID.
     */
    public MedicationDto getMedicationByNplId(String nplId) {
        return medicationRepository.findByNplId(nplId)
                .map(MedicationDto::from)
                .orElseThrow(() -> new MedicationNotFoundException("NPL-ID hittades ej: " + nplId));
    }

    /**
     * Sök läkemedel via namn eller ATC-kod.
     */
    public Page<MedicationDto> searchMedications(String query, Pageable pageable) {
        log.debug("Searching medications with query: {}", query);
        return medicationRepository.search(query, pageable)
                .map(MedicationDto::from);
    }

    /**
     * Snabbsökning via namn (för autocomplete).
     */
    public List<MedicationDto> quickSearchByName(String query) {
        if (query == null || query.length() < 2) {
            return List.of();
        }
        return medicationRepository.searchByName(query)
                .stream()
                .limit(20)
                .map(MedicationDto::from)
                .toList();
    }

    /**
     * Hämta läkemedel via ATC-kod.
     */
    public List<MedicationDto> getMedicationsByAtcCode(String atcCode) {
        return medicationRepository.findByAtcCode(atcCode)
                .stream()
                .map(MedicationDto::from)
                .toList();
    }

    /**
     * Hämta läkemedel via ATC-kodprefix (substansgrupp).
     */
    public List<MedicationDto> getMedicationsByAtcPrefix(String atcPrefix) {
        return medicationRepository.findByAtcCodeStartingWith(atcPrefix)
                .stream()
                .map(MedicationDto::from)
                .toList();
    }

    /**
     * Hämta narkotikaklassade läkemedel.
     */
    public List<MedicationDto> getNarcoticMedications() {
        return medicationRepository.findByNarcoticTrueAndActiveTrue()
                .stream()
                .map(MedicationDto::from)
                .toList();
    }

    /**
     * Skapa nytt läkemedel (för admin/import).
     */
    @Transactional
    public MedicationDto createMedication(Medication medication) {
        log.info("Creating medication: {}", medication.getName());
        Medication saved = medicationRepository.save(medication);
        return MedicationDto.from(saved);
    }

    /**
     * Uppdatera läkemedel.
     */
    @Transactional
    public MedicationDto updateMedication(UUID medicationId, Medication updates) {
        Medication medication = medicationRepository.findById(medicationId)
                .orElseThrow(() -> new MedicationNotFoundException(medicationId));

        // Uppdatera fält
        if (updates.getName() != null) medication.setName(updates.getName());
        if (updates.getGenericName() != null) medication.setGenericName(updates.getGenericName());
        if (updates.getAtcCode() != null) medication.setAtcCode(updates.getAtcCode());
        if (updates.getStrength() != null) medication.setStrength(updates.getStrength());
        if (updates.getDosageForm() != null) medication.setDosageForm(updates.getDosageForm());
        if (updates.getRoute() != null) medication.setRoute(updates.getRoute());

        medication.setUpdatedAt(java.time.Instant.now());

        Medication saved = medicationRepository.save(medication);
        log.info("Updated medication: {}", saved.getId());
        return MedicationDto.from(saved);
    }

    /**
     * Inaktivera läkemedel.
     */
    @Transactional
    public void deactivateMedication(UUID medicationId) {
        Medication medication = medicationRepository.findById(medicationId)
                .orElseThrow(() -> new MedicationNotFoundException(medicationId));

        medication.setActive(false);
        medication.setUpdatedAt(java.time.Instant.now());
        medicationRepository.save(medication);

        log.info("Deactivated medication: {}", medicationId);
    }
}
