package se.curanexus.medication.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.curanexus.events.DomainEvent;
import se.curanexus.events.DomainEventPublisher;
import se.curanexus.medication.api.dto.CreatePrescriptionRequest;
import se.curanexus.medication.api.dto.PrescriptionDto;
import se.curanexus.medication.domain.Medication;
import se.curanexus.medication.domain.Prescription;
import se.curanexus.medication.domain.PrescriptionStatus;
import se.curanexus.medication.repository.MedicationRepository;
import se.curanexus.medication.repository.PrescriptionRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Service för ordinationshantering.
 */
@Service
@Transactional
public class PrescriptionService {

    private static final Logger log = LoggerFactory.getLogger(PrescriptionService.class);

    private final PrescriptionRepository prescriptionRepository;
    private final MedicationRepository medicationRepository;
    private final DomainEventPublisher eventPublisher;

    public PrescriptionService(PrescriptionRepository prescriptionRepository,
                                MedicationRepository medicationRepository,
                                DomainEventPublisher eventPublisher) {
        this.prescriptionRepository = prescriptionRepository;
        this.medicationRepository = medicationRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Skapa ny ordination.
     */
    public PrescriptionDto createPrescription(CreatePrescriptionRequest request, UUID prescriberId) {
        log.info("Creating prescription for patient {}", request.patientId());

        Prescription prescription = new Prescription(request.patientId(), prescriberId);

        // Koppla läkemedel om angivet
        if (request.medicationId() != null) {
            Medication medication = medicationRepository.findById(request.medicationId())
                    .orElseThrow(() -> new MedicationNotFoundException(request.medicationId()));
            prescription.setMedication(medication);
            prescription.setAtcCode(medication.getAtcCode());
        } else {
            prescription.setMedicationText(request.medicationText());
            prescription.setAtcCode(request.atcCode());
        }

        prescription.setEncounterId(request.encounterId());
        prescription.setIndication(request.indication());
        prescription.setRoute(request.route());
        prescription.setDosageInstruction(request.dosageInstruction());
        prescription.setDoseQuantity(request.doseQuantity());
        prescription.setDoseUnit(request.doseUnit());
        prescription.setFrequency(request.frequency());
        prescription.setFrequencyPeriodHours(request.frequencyPeriodHours());
        prescription.setAsNeeded(request.asNeeded());
        prescription.setMaxDosePerDay(request.maxDosePerDay());
        prescription.setStartDate(request.startDate() != null ? request.startDate() : LocalDate.now());
        prescription.setEndDate(request.endDate());
        prescription.setDurationDays(request.durationDays());
        prescription.setDispenseQuantity(request.dispenseQuantity());
        prescription.setNumberOfRepeats(request.numberOfRepeats());
        prescription.setSubstitutionNotAllowed(request.substitutionNotAllowed());
        prescription.setSubstitutionReason(request.substitutionReason());
        prescription.setPrescriberHsaId(request.prescriberHsaId());
        prescription.setPrescriberName(request.prescriberName());
        prescription.setPrescriberCode(request.prescriberCode());
        prescription.setUnitId(request.unitId());
        prescription.setUnitHsaId(request.unitHsaId());
        prescription.setPharmacyNote(request.pharmacyNote());
        prescription.setInternalNote(request.internalNote());

        if (request.activateImmediately()) {
            prescription.activate();
        }

        Prescription saved = prescriptionRepository.save(prescription);
        log.info("Created prescription {} for patient {}", saved.getId(), request.patientId());

        eventPublisher.publish(new PrescriptionCreatedEvent(saved));

        return PrescriptionDto.from(saved);
    }

    /**
     * Hämta ordination via ID.
     */
    @Transactional(readOnly = true)
    public PrescriptionDto getPrescription(UUID prescriptionId) {
        return prescriptionRepository.findById(prescriptionId)
                .map(PrescriptionDto::from)
                .orElseThrow(() -> new PrescriptionNotFoundException(prescriptionId));
    }

    /**
     * Hämta patients alla ordinationer.
     */
    @Transactional(readOnly = true)
    public List<PrescriptionDto> getPatientPrescriptions(UUID patientId) {
        return prescriptionRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
                .stream()
                .map(PrescriptionDto::from)
                .toList();
    }

    /**
     * Hämta patients aktiva ordinationer.
     */
    @Transactional(readOnly = true)
    public List<PrescriptionDto> getActivePrescriptions(UUID patientId) {
        return prescriptionRepository.findActiveByPatientIdOnDate(patientId, LocalDate.now())
                .stream()
                .map(PrescriptionDto::from)
                .toList();
    }

    /**
     * Hämta ordinationer för vårdkontakt.
     */
    @Transactional(readOnly = true)
    public List<PrescriptionDto> getEncounterPrescriptions(UUID encounterId) {
        return prescriptionRepository.findByEncounterId(encounterId)
                .stream()
                .map(PrescriptionDto::from)
                .toList();
    }

    /**
     * Aktivera ordination.
     */
    public PrescriptionDto activatePrescription(UUID prescriptionId) {
        log.info("Activating prescription {}", prescriptionId);

        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new PrescriptionNotFoundException(prescriptionId));

        prescription.activate();
        Prescription saved = prescriptionRepository.save(prescription);

        eventPublisher.publish(new PrescriptionActivatedEvent(saved));

        return PrescriptionDto.from(saved);
    }

    /**
     * Pausa ordination.
     */
    public PrescriptionDto putOnHold(UUID prescriptionId, String reason) {
        log.info("Putting prescription {} on hold: {}", prescriptionId, reason);

        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new PrescriptionNotFoundException(prescriptionId));

        prescription.putOnHold(reason);
        Prescription saved = prescriptionRepository.save(prescription);

        eventPublisher.publish(new PrescriptionPutOnHoldEvent(saved, reason));

        return PrescriptionDto.from(saved);
    }

    /**
     * Avsluta ordination.
     */
    public PrescriptionDto completePrescription(UUID prescriptionId) {
        log.info("Completing prescription {}", prescriptionId);

        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new PrescriptionNotFoundException(prescriptionId));

        prescription.complete();
        Prescription saved = prescriptionRepository.save(prescription);

        eventPublisher.publish(new PrescriptionCompletedEvent(saved));

        return PrescriptionDto.from(saved);
    }

    /**
     * Avbryt ordination.
     */
    public PrescriptionDto cancelPrescription(UUID prescriptionId, String reason) {
        log.info("Cancelling prescription {}: {}", prescriptionId, reason);

        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new PrescriptionNotFoundException(prescriptionId));

        prescription.cancel(reason);
        Prescription saved = prescriptionRepository.save(prescription);

        eventPublisher.publish(new PrescriptionCancelledEvent(saved, reason));

        return PrescriptionDto.from(saved);
    }

    /**
     * Sök ordinationer.
     */
    @Transactional(readOnly = true)
    public Page<PrescriptionDto> searchPrescriptions(UUID patientId, UUID prescriberId,
                                                      PrescriptionStatus status, String atcCode,
                                                      Pageable pageable) {
        return prescriptionRepository.search(patientId, prescriberId, status, atcCode, pageable)
                .map(PrescriptionDto::from);
    }

    /**
     * Räkna aktiva ordinationer för patient.
     */
    @Transactional(readOnly = true)
    public long countActivePrescriptions(UUID patientId) {
        return prescriptionRepository.countActiveByPatient(patientId);
    }

    // Event classes

    public static class PrescriptionCreatedEvent extends DomainEvent {
        private final Prescription prescription;
        public PrescriptionCreatedEvent(Prescription prescription) {
            super(prescription);
            this.prescription = prescription;
        }
        public Prescription getPrescription() { return prescription; }
        @Override public String getAggregateType() { return "PRESCRIPTION"; }
        @Override public UUID getAggregateId() { return prescription.getId(); }
        @Override public String getEventType() { return "CREATED"; }
    }

    public static class PrescriptionActivatedEvent extends DomainEvent {
        private final Prescription prescription;
        public PrescriptionActivatedEvent(Prescription prescription) {
            super(prescription);
            this.prescription = prescription;
        }
        public Prescription getPrescription() { return prescription; }
        @Override public String getAggregateType() { return "PRESCRIPTION"; }
        @Override public UUID getAggregateId() { return prescription.getId(); }
        @Override public String getEventType() { return "ACTIVATED"; }
    }

    public static class PrescriptionPutOnHoldEvent extends DomainEvent {
        private final Prescription prescription;
        private final String reason;
        public PrescriptionPutOnHoldEvent(Prescription prescription, String reason) {
            super(prescription);
            this.prescription = prescription;
            this.reason = reason;
        }
        public Prescription getPrescription() { return prescription; }
        public String getReason() { return reason; }
        @Override public String getAggregateType() { return "PRESCRIPTION"; }
        @Override public UUID getAggregateId() { return prescription.getId(); }
        @Override public String getEventType() { return "PUT_ON_HOLD"; }
    }

    public static class PrescriptionCompletedEvent extends DomainEvent {
        private final Prescription prescription;
        public PrescriptionCompletedEvent(Prescription prescription) {
            super(prescription);
            this.prescription = prescription;
        }
        public Prescription getPrescription() { return prescription; }
        @Override public String getAggregateType() { return "PRESCRIPTION"; }
        @Override public UUID getAggregateId() { return prescription.getId(); }
        @Override public String getEventType() { return "COMPLETED"; }
    }

    public static class PrescriptionCancelledEvent extends DomainEvent {
        private final Prescription prescription;
        private final String reason;
        public PrescriptionCancelledEvent(Prescription prescription, String reason) {
            super(prescription);
            this.prescription = prescription;
            this.reason = reason;
        }
        public Prescription getPrescription() { return prescription; }
        public String getReason() { return reason; }
        @Override public String getAggregateType() { return "PRESCRIPTION"; }
        @Override public UUID getAggregateId() { return prescription.getId(); }
        @Override public String getEventType() { return "CANCELLED"; }
    }
}
