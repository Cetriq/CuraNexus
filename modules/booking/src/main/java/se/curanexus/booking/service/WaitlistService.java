package se.curanexus.booking.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.curanexus.booking.api.dto.CreateWaitlistEntryRequest;
import se.curanexus.booking.api.dto.WaitlistEntryDto;
import se.curanexus.booking.domain.WaitlistEntry;
import se.curanexus.booking.domain.WaitlistEntry.WaitlistPriority;
import se.curanexus.booking.domain.WaitlistEntry.WaitlistStatus;
import se.curanexus.booking.repository.WaitlistRepository;
import se.curanexus.events.DomainEvent;
import se.curanexus.events.DomainEventPublisher;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Service för att hantera väntelista.
 */
@Service
@Transactional
public class WaitlistService {

    private static final Logger log = LoggerFactory.getLogger(WaitlistService.class);
    private static final Duration NOTIFICATION_EXPIRY = Duration.ofHours(48);

    private final WaitlistRepository waitlistRepository;
    private final DomainEventPublisher eventPublisher;

    public WaitlistService(WaitlistRepository waitlistRepository, DomainEventPublisher eventPublisher) {
        this.waitlistRepository = waitlistRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Lägg till patient på väntelista.
     */
    public WaitlistEntryDto addToWaitlist(CreateWaitlistEntryRequest request) {
        log.info("Adding patient {} to waitlist for service {}",
                request.patientId(), request.serviceType());

        WaitlistEntry entry = new WaitlistEntry(
                request.patientId(),
                request.serviceType()
        );

        entry.setPractitionerId(request.practitionerId());
        entry.setUnitId(request.unitId());
        entry.setReasonText(request.reasonText());
        entry.setPriority(request.priority() != null ? request.priority() : WaitlistPriority.ROUTINE);
        entry.setPreferredDateFrom(request.preferredDateFrom());
        entry.setPreferredDateTo(request.preferredDateTo());

        WaitlistEntry saved = waitlistRepository.save(entry);
        log.info("Created waitlist entry {} for patient {}", saved.getId(), request.patientId());

        eventPublisher.publish(new WaitlistEntryCreatedEvent(saved));

        return WaitlistEntryDto.from(saved);
    }

    /**
     * Hämta väntelistpost via ID.
     */
    @Transactional(readOnly = true)
    public WaitlistEntryDto getWaitlistEntry(UUID entryId) {
        return waitlistRepository.findById(entryId)
                .map(WaitlistEntryDto::from)
                .orElseThrow(() -> new WaitlistEntryNotFoundException(entryId));
    }

    /**
     * Hämta patientens aktiva väntelistposter.
     */
    @Transactional(readOnly = true)
    public List<WaitlistEntryDto> getPatientWaitlistEntries(UUID patientId) {
        return waitlistRepository.findByPatientIdAndStatusIn(
                patientId,
                List.of(WaitlistStatus.WAITING, WaitlistStatus.NOTIFIED)
        ).stream()
                .map(WaitlistEntryDto::from)
                .toList();
    }

    /**
     * Hämta väntelista för vårdgivare.
     */
    @Transactional(readOnly = true)
    public List<WaitlistEntryDto> getPractitionerWaitlist(UUID practitionerId) {
        return waitlistRepository.findWaitingByPractitioner(practitionerId)
                .stream()
                .map(WaitlistEntryDto::from)
                .toList();
    }

    /**
     * Hämta väntelista för enhet.
     */
    @Transactional(readOnly = true)
    public List<WaitlistEntryDto> getUnitWaitlist(UUID unitId) {
        return waitlistRepository.findWaitingByUnit(unitId)
                .stream()
                .map(WaitlistEntryDto::from)
                .toList();
    }

    /**
     * Notifiera patient om tillgänglig tid.
     */
    public WaitlistEntryDto notifyPatient(UUID entryId) {
        WaitlistEntry entry = waitlistRepository.findById(entryId)
                .orElseThrow(() -> new WaitlistEntryNotFoundException(entryId));

        if (entry.getStatus() != WaitlistStatus.WAITING) {
            throw new IllegalStateException("Can only notify entries in WAITING status");
        }

        entry.setStatus(WaitlistStatus.NOTIFIED);
        entry.setNotifiedAt(Instant.now());
        WaitlistEntry saved = waitlistRepository.save(entry);

        log.info("Notified patient for waitlist entry {}", entryId);
        eventPublisher.publish(new WaitlistPatientNotifiedEvent(saved));

        return WaitlistEntryDto.from(saved);
    }

    /**
     * Markera väntelistpost som bokad.
     */
    public WaitlistEntryDto markAsBooked(UUID entryId, UUID appointmentId) {
        WaitlistEntry entry = waitlistRepository.findById(entryId)
                .orElseThrow(() -> new WaitlistEntryNotFoundException(entryId));

        entry.setStatus(WaitlistStatus.BOOKED);
        entry.setBookedAppointmentId(appointmentId);
        WaitlistEntry saved = waitlistRepository.save(entry);

        log.info("Marked waitlist entry {} as booked with appointment {}", entryId, appointmentId);
        return WaitlistEntryDto.from(saved);
    }

    /**
     * Avbryt väntelistpost.
     */
    public WaitlistEntryDto cancelWaitlistEntry(UUID entryId) {
        WaitlistEntry entry = waitlistRepository.findById(entryId)
                .orElseThrow(() -> new WaitlistEntryNotFoundException(entryId));

        if (entry.getStatus() == WaitlistStatus.BOOKED) {
            throw new IllegalStateException("Cannot cancel booked waitlist entry");
        }

        entry.setStatus(WaitlistStatus.CANCELLED);
        WaitlistEntry saved = waitlistRepository.save(entry);

        log.info("Cancelled waitlist entry {}", entryId);
        return WaitlistEntryDto.from(saved);
    }

    /**
     * Hitta matchande väntelistposter för en tillgänglig tid.
     */
    @Transactional(readOnly = true)
    public List<WaitlistEntryDto> findMatchingForAvailableSlot(String serviceType, LocalDate date) {
        return waitlistRepository.findMatchingForAvailableSlot(serviceType, date)
                .stream()
                .map(WaitlistEntryDto::from)
                .toList();
    }

    /**
     * Uppdatera prioritet.
     */
    public WaitlistEntryDto updatePriority(UUID entryId, WaitlistPriority newPriority) {
        WaitlistEntry entry = waitlistRepository.findById(entryId)
                .orElseThrow(() -> new WaitlistEntryNotFoundException(entryId));

        entry.setPriority(newPriority);
        WaitlistEntry saved = waitlistRepository.save(entry);

        log.info("Updated priority for waitlist entry {} to {}", entryId, newPriority);
        return WaitlistEntryDto.from(saved);
    }

    /**
     * Hantera utgångna notifieringar.
     */
    public int expireOldNotifications() {
        Instant threshold = Instant.now().minus(NOTIFICATION_EXPIRY);
        List<WaitlistEntry> expired = waitlistRepository.findExpiredNotifications(threshold);

        for (WaitlistEntry entry : expired) {
            entry.setStatus(WaitlistStatus.WAITING);
            entry.setNotifiedAt(null);
            waitlistRepository.save(entry);
        }

        log.info("Reset {} expired notifications to WAITING status", expired.size());
        return expired.size();
    }

    /**
     * Räkna antal väntande för vårdgivare.
     */
    @Transactional(readOnly = true)
    public long countWaitingByPractitioner(UUID practitionerId) {
        return waitlistRepository.countWaitingByPractitioner(practitionerId);
    }

    /**
     * Räkna antal väntande för enhet.
     */
    @Transactional(readOnly = true)
    public long countWaitingByUnit(UUID unitId) {
        return waitlistRepository.countWaitingByUnit(unitId);
    }

    // Event classes

    public static class WaitlistEntryCreatedEvent extends DomainEvent {
        private final WaitlistEntry entry;
        public WaitlistEntryCreatedEvent(WaitlistEntry entry) {
            super(entry);
            this.entry = entry;
        }
        public WaitlistEntry getEntry() { return entry; }
        @Override public String getAggregateType() { return "WAITLIST"; }
        @Override public UUID getAggregateId() { return entry.getId(); }
        @Override public String getEventType() { return "CREATED"; }
    }

    public static class WaitlistPatientNotifiedEvent extends DomainEvent {
        private final WaitlistEntry entry;
        public WaitlistPatientNotifiedEvent(WaitlistEntry entry) {
            super(entry);
            this.entry = entry;
        }
        public WaitlistEntry getEntry() { return entry; }
        @Override public String getAggregateType() { return "WAITLIST"; }
        @Override public UUID getAggregateId() { return entry.getId(); }
        @Override public String getEventType() { return "PATIENT_NOTIFIED"; }
    }
}
