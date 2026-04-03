package se.curanexus.medication.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.curanexus.events.DomainEvent;
import se.curanexus.events.DomainEventPublisher;
import se.curanexus.medication.api.dto.MedicationAdministrationDto;
import se.curanexus.medication.api.dto.RecordAdministrationRequest;
import se.curanexus.medication.domain.MedicationAdministration;
import se.curanexus.medication.domain.Prescription;
import se.curanexus.medication.repository.MedicationAdministrationRepository;
import se.curanexus.medication.repository.PrescriptionRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service för läkemedelsadministrering.
 */
@Service
@Transactional
public class MedicationAdministrationService {

    private static final Logger log = LoggerFactory.getLogger(MedicationAdministrationService.class);

    private final MedicationAdministrationRepository administrationRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final DomainEventPublisher eventPublisher;

    public MedicationAdministrationService(MedicationAdministrationRepository administrationRepository,
                                            PrescriptionRepository prescriptionRepository,
                                            DomainEventPublisher eventPublisher) {
        this.administrationRepository = administrationRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Registrera utförd läkemedelsadministrering.
     */
    public MedicationAdministrationDto recordAdministration(RecordAdministrationRequest request, UUID performerId) {
        log.info("Recording medication administration for prescription {}", request.prescriptionId());

        Prescription prescription = prescriptionRepository.findById(request.prescriptionId())
                .orElseThrow(() -> new PrescriptionNotFoundException(request.prescriptionId()));

        MedicationAdministration administration = new MedicationAdministration(
                prescription.getPatientId(),
                prescription
        );

        administration.setEncounterId(request.encounterId());
        administration.setRoute(request.route() != null ? request.route() : prescription.getRoute());
        administration.setBodySite(request.bodySite());
        administration.setMethod(request.method());
        administration.setRateQuantity(request.rateQuantity());
        administration.setRateUnit(request.rateUnit());
        administration.setPerformerHsaId(request.performerHsaId());
        administration.setPerformerName(request.performerName());
        administration.setNotes(request.notes());
        administration.setLotNumber(request.lotNumber());

        // Markera som utförd direkt
        administration.complete(
                performerId,
                request.performerName(),
                request.doseQuantity(),
                request.doseUnit()
        );

        if (request.administeredAt() != null) {
            administration.setAdministeredAt(request.administeredAt());
        }

        MedicationAdministration saved = administrationRepository.save(administration);
        log.info("Recorded administration {} for patient {}", saved.getId(), prescription.getPatientId());

        eventPublisher.publish(new MedicationAdministeredEvent(saved));

        return MedicationAdministrationDto.from(saved);
    }

    /**
     * Hämta administrering via ID.
     */
    @Transactional(readOnly = true)
    public MedicationAdministrationDto getAdministration(UUID administrationId) {
        return administrationRepository.findById(administrationId)
                .map(MedicationAdministrationDto::from)
                .orElseThrow(() -> new MedicationAdministrationNotFoundException(administrationId));
    }

    /**
     * Hämta administreringar för patient.
     */
    @Transactional(readOnly = true)
    public List<MedicationAdministrationDto> getPatientAdministrations(UUID patientId) {
        return administrationRepository.findByPatientIdOrderByAdministeredAtDesc(patientId)
                .stream()
                .map(MedicationAdministrationDto::from)
                .toList();
    }

    /**
     * Hämta administreringar för patient inom tidsintervall.
     */
    @Transactional(readOnly = true)
    public List<MedicationAdministrationDto> getPatientAdministrations(UUID patientId,
                                                                        LocalDateTime from,
                                                                        LocalDateTime to) {
        return administrationRepository.findByPatientAndDateRange(patientId, from, to)
                .stream()
                .map(MedicationAdministrationDto::from)
                .toList();
    }

    /**
     * Hämta administreringar för ordination.
     */
    @Transactional(readOnly = true)
    public List<MedicationAdministrationDto> getPrescriptionAdministrations(UUID prescriptionId) {
        return administrationRepository.findByPrescriptionIdOrderByAdministeredAtDesc(prescriptionId)
                .stream()
                .map(MedicationAdministrationDto::from)
                .toList();
    }

    /**
     * Hämta planerade administreringar för patient.
     */
    @Transactional(readOnly = true)
    public List<MedicationAdministrationDto> getPendingAdministrations(UUID patientId, LocalDateTime until) {
        return administrationRepository.findPendingByPatient(patientId, until)
                .stream()
                .map(MedicationAdministrationDto::from)
                .toList();
    }

    /**
     * Markera administrering som ej given.
     */
    public MedicationAdministrationDto markNotGiven(UUID administrationId, String reason) {
        log.info("Marking administration {} as not given: {}", administrationId, reason);

        MedicationAdministration administration = administrationRepository.findById(administrationId)
                .orElseThrow(() -> new MedicationAdministrationNotFoundException(administrationId));

        administration.markNotGiven(reason);
        MedicationAdministration saved = administrationRepository.save(administration);

        eventPublisher.publish(new MedicationNotGivenEvent(saved, reason));

        return MedicationAdministrationDto.from(saved);
    }

    /**
     * Hämta försenade administreringar.
     */
    @Transactional(readOnly = true)
    public List<MedicationAdministrationDto> getOverdueAdministrations() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(30);
        return administrationRepository.findOverdueAdministrations(threshold)
                .stream()
                .map(MedicationAdministrationDto::from)
                .toList();
    }

    /**
     * Räkna utförda administreringar för ordination.
     */
    @Transactional(readOnly = true)
    public long countCompletedAdministrations(UUID prescriptionId) {
        return administrationRepository.countCompletedByPrescription(prescriptionId);
    }

    // Event classes

    public static class MedicationAdministeredEvent extends DomainEvent {
        private final MedicationAdministration administration;
        public MedicationAdministeredEvent(MedicationAdministration administration) {
            super(administration);
            this.administration = administration;
        }
        public MedicationAdministration getAdministration() { return administration; }
        @Override public String getAggregateType() { return "MEDICATION_ADMINISTRATION"; }
        @Override public UUID getAggregateId() { return administration.getId(); }
        @Override public String getEventType() { return "ADMINISTERED"; }
    }

    public static class MedicationNotGivenEvent extends DomainEvent {
        private final MedicationAdministration administration;
        private final String reason;
        public MedicationNotGivenEvent(MedicationAdministration administration, String reason) {
            super(administration);
            this.administration = administration;
            this.reason = reason;
        }
        public MedicationAdministration getAdministration() { return administration; }
        public String getReason() { return reason; }
        @Override public String getAggregateType() { return "MEDICATION_ADMINISTRATION"; }
        @Override public UUID getAggregateId() { return administration.getId(); }
        @Override public String getEventType() { return "NOT_GIVEN"; }
    }
}
