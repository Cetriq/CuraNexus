package se.curanexus.medication.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.curanexus.events.DomainEvent;
import se.curanexus.events.DomainEventPublisher;
import se.curanexus.medication.api.dto.CreateDrugAllergyRequest;
import se.curanexus.medication.api.dto.DrugAllergyDto;
import se.curanexus.medication.domain.DrugAllergy;
import se.curanexus.medication.repository.DrugAllergyRepository;

import java.util.List;
import java.util.UUID;

/**
 * Service för hantering av läkemedelsallergier.
 */
@Service
@Transactional
public class DrugAllergyService {

    private static final Logger log = LoggerFactory.getLogger(DrugAllergyService.class);

    private final DrugAllergyRepository allergyRepository;
    private final DomainEventPublisher eventPublisher;

    public DrugAllergyService(DrugAllergyRepository allergyRepository,
                               DomainEventPublisher eventPublisher) {
        this.allergyRepository = allergyRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Registrera läkemedelsallergi.
     */
    public DrugAllergyDto createAllergy(CreateDrugAllergyRequest request, UUID recordedById) {
        log.info("Creating drug allergy for patient {}: {}", request.patientId(), request.substanceName());

        DrugAllergy allergy = new DrugAllergy(
                request.patientId(),
                request.substanceName(),
                request.reactionType()
        );

        allergy.setMedicationId(request.medicationId());
        allergy.setAtcCode(request.atcCode());
        allergy.setSeverity(request.severity());
        allergy.setReactionDescription(request.reactionDescription());
        allergy.setOnsetDate(request.onsetDate());
        allergy.setSource(request.source());
        allergy.setNotes(request.notes());
        allergy.setRecordedById(recordedById);

        DrugAllergy saved = allergyRepository.save(allergy);
        log.info("Created allergy {} for patient {}", saved.getId(), request.patientId());

        eventPublisher.publish(new DrugAllergyCreatedEvent(saved));

        return DrugAllergyDto.from(saved);
    }

    /**
     * Hämta allergi via ID.
     */
    @Transactional(readOnly = true)
    public DrugAllergyDto getAllergy(UUID allergyId) {
        return allergyRepository.findById(allergyId)
                .map(DrugAllergyDto::from)
                .orElseThrow(() -> new DrugAllergyNotFoundException(allergyId));
    }

    /**
     * Hämta patients aktiva allergier.
     */
    @Transactional(readOnly = true)
    public List<DrugAllergyDto> getPatientAllergies(UUID patientId) {
        return allergyRepository.findByPatientIdAndActiveTrue(patientId)
                .stream()
                .map(DrugAllergyDto::from)
                .toList();
    }

    /**
     * Hämta alla allergier för patient (inkl. inaktiva).
     */
    @Transactional(readOnly = true)
    public List<DrugAllergyDto> getAllPatientAllergies(UUID patientId) {
        return allergyRepository.findByPatientId(patientId)
                .stream()
                .map(DrugAllergyDto::from)
                .toList();
    }

    /**
     * Kontrollera om patient har allergi mot givet läkemedel (via ATC).
     */
    @Transactional(readOnly = true)
    public List<DrugAllergyDto> checkAllergyForAtcCode(UUID patientId, String atcCode) {
        return allergyRepository.findMatchingAllergies(patientId, atcCode)
                .stream()
                .map(DrugAllergyDto::from)
                .toList();
    }

    /**
     * Verifiera allergi.
     */
    public DrugAllergyDto verifyAllergy(UUID allergyId, UUID verifiedById) {
        log.info("Verifying allergy {}", allergyId);

        DrugAllergy allergy = allergyRepository.findById(allergyId)
                .orElseThrow(() -> new DrugAllergyNotFoundException(allergyId));

        allergy.verify(verifiedById);
        DrugAllergy saved = allergyRepository.save(allergy);

        eventPublisher.publish(new DrugAllergyVerifiedEvent(saved));

        return DrugAllergyDto.from(saved);
    }

    /**
     * Inaktivera allergi.
     */
    public DrugAllergyDto deactivateAllergy(UUID allergyId, String reason) {
        log.info("Deactivating allergy {}: {}", allergyId, reason);

        DrugAllergy allergy = allergyRepository.findById(allergyId)
                .orElseThrow(() -> new DrugAllergyNotFoundException(allergyId));

        allergy.deactivate(reason);
        DrugAllergy saved = allergyRepository.save(allergy);

        eventPublisher.publish(new DrugAllergyDeactivatedEvent(saved, reason));

        return DrugAllergyDto.from(saved);
    }

    /**
     * Räkna aktiva allergier för patient.
     */
    @Transactional(readOnly = true)
    public long countActiveAllergies(UUID patientId) {
        return allergyRepository.countByPatientIdAndActiveTrue(patientId);
    }

    // Event classes

    public static class DrugAllergyCreatedEvent extends DomainEvent {
        private final DrugAllergy allergy;
        public DrugAllergyCreatedEvent(DrugAllergy allergy) {
            super(allergy);
            this.allergy = allergy;
        }
        public DrugAllergy getAllergy() { return allergy; }
        @Override public String getAggregateType() { return "DRUG_ALLERGY"; }
        @Override public UUID getAggregateId() { return allergy.getId(); }
        @Override public String getEventType() { return "CREATED"; }
    }

    public static class DrugAllergyVerifiedEvent extends DomainEvent {
        private final DrugAllergy allergy;
        public DrugAllergyVerifiedEvent(DrugAllergy allergy) {
            super(allergy);
            this.allergy = allergy;
        }
        public DrugAllergy getAllergy() { return allergy; }
        @Override public String getAggregateType() { return "DRUG_ALLERGY"; }
        @Override public UUID getAggregateId() { return allergy.getId(); }
        @Override public String getEventType() { return "VERIFIED"; }
    }

    public static class DrugAllergyDeactivatedEvent extends DomainEvent {
        private final DrugAllergy allergy;
        private final String reason;
        public DrugAllergyDeactivatedEvent(DrugAllergy allergy, String reason) {
            super(allergy);
            this.allergy = allergy;
            this.reason = reason;
        }
        public DrugAllergy getAllergy() { return allergy; }
        public String getReason() { return reason; }
        @Override public String getAggregateType() { return "DRUG_ALLERGY"; }
        @Override public UUID getAggregateId() { return allergy.getId(); }
        @Override public String getEventType() { return "DEACTIVATED"; }
    }
}
