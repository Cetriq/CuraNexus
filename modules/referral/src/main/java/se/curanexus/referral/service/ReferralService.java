package se.curanexus.referral.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.curanexus.events.DomainEvent;
import se.curanexus.events.DomainEventPublisher;
import se.curanexus.referral.api.dto.AssessReferralRequest;
import se.curanexus.referral.api.dto.CreateReferralRequest;
import se.curanexus.referral.api.dto.ForwardReferralRequest;
import se.curanexus.referral.api.dto.ReferralDto;
import se.curanexus.referral.domain.*;
import se.curanexus.referral.repository.ReferralRepository;
import se.curanexus.referral.repository.ReferralResponseRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Service för remisshantering.
 */
@Service
@Transactional
public class ReferralService {

    private static final Logger log = LoggerFactory.getLogger(ReferralService.class);

    private final ReferralRepository referralRepository;
    private final ReferralResponseRepository responseRepository;
    private final DomainEventPublisher eventPublisher;

    public ReferralService(ReferralRepository referralRepository,
                            ReferralResponseRepository responseRepository,
                            DomainEventPublisher eventPublisher) {
        this.referralRepository = referralRepository;
        this.responseRepository = responseRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Skapa ny remiss.
     */
    public ReferralDto createReferral(CreateReferralRequest request, UUID senderUnitId, UUID senderPractitionerId) {
        log.info("Creating referral for patient {}", request.patientId());

        Referral referral = new Referral(
                request.patientId(),
                senderUnitId,
                senderPractitionerId,
                request.referralType(),
                request.reason()
        );

        // Patientinfo
        referral.setPatientPersonnummer(request.patientPersonnummer());
        referral.setPatientName(request.patientName());

        // Prioritet
        if (request.priority() != null) {
            referral.setPriority(request.priority());
        }

        // Avsändare
        referral.setSenderUnitHsaId(request.senderUnitHsaId());
        referral.setSenderUnitName(request.senderUnitName());
        referral.setSenderPractitionerHsaId(request.senderPractitionerHsaId());
        referral.setSenderPractitionerName(request.senderPractitionerName());

        // Mottagare
        referral.setReceiverUnitId(request.receiverUnitId());
        referral.setReceiverUnitHsaId(request.receiverUnitHsaId());
        referral.setReceiverUnitName(request.receiverUnitName());
        referral.setRequestedSpecialty(request.requestedSpecialty());

        // Klinisk info
        referral.setDiagnosisCode(request.diagnosisCode());
        referral.setDiagnosisText(request.diagnosisText());
        referral.setClinicalHistory(request.clinicalHistory());
        referral.setCurrentStatus(request.currentStatus());
        referral.setExaminationsDone(request.examinationsDone());
        referral.setCurrentMedication(request.currentMedication());
        referral.setAllergies(request.allergies());
        referral.setAdditionalInfo(request.additionalInfo());

        // Kopplingar
        referral.setSourceEncounterId(request.sourceEncounterId());
        referral.setRequestedDate(request.requestedDate());
        referral.setValidUntil(request.validUntil());

        if (request.sendImmediately()) {
            referral.send();
        }

        Referral saved = referralRepository.save(referral);
        log.info("Created referral {} with reference {}", saved.getId(), saved.getReferralReference());

        eventPublisher.publish(new ReferralCreatedEvent(saved));

        return ReferralDto.fromWithoutResponses(saved);
    }

    /**
     * Hämta remiss via ID.
     */
    @Transactional(readOnly = true)
    public ReferralDto getReferral(UUID referralId) {
        return referralRepository.findById(referralId)
                .map(ReferralDto::from)
                .orElseThrow(() -> new ReferralNotFoundException(referralId));
    }

    /**
     * Hämta remiss via referensnummer.
     */
    @Transactional(readOnly = true)
    public ReferralDto getReferralByReference(String referralReference) {
        return referralRepository.findByReferralReference(referralReference)
                .map(ReferralDto::from)
                .orElseThrow(() -> new ReferralNotFoundException("Remissreferens hittades ej: " + referralReference));
    }

    /**
     * Hämta patientens remisser.
     */
    @Transactional(readOnly = true)
    public List<ReferralDto> getPatientReferrals(UUID patientId) {
        return referralRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
                .stream()
                .map(ReferralDto::fromWithoutResponses)
                .toList();
    }

    /**
     * Skicka remiss.
     */
    public ReferralDto sendReferral(UUID referralId) {
        log.info("Sending referral {}", referralId);

        Referral referral = referralRepository.findById(referralId)
                .orElseThrow(() -> new ReferralNotFoundException(referralId));

        referral.send();
        Referral saved = referralRepository.save(referral);

        eventPublisher.publish(new ReferralSentEvent(saved));

        return ReferralDto.fromWithoutResponses(saved);
    }

    /**
     * Markera remiss som mottagen.
     */
    public ReferralDto markReceived(UUID referralId) {
        log.info("Marking referral {} as received", referralId);

        Referral referral = referralRepository.findById(referralId)
                .orElseThrow(() -> new ReferralNotFoundException(referralId));

        referral.markReceived();
        Referral saved = referralRepository.save(referral);

        eventPublisher.publish(new ReferralReceivedEvent(saved));

        return ReferralDto.fromWithoutResponses(saved);
    }

    /**
     * Starta bedömning av remiss.
     */
    public ReferralDto startAssessment(UUID referralId, UUID assessorId, String assessorName) {
        log.info("Starting assessment of referral {} by {}", referralId, assessorName);

        Referral referral = referralRepository.findById(referralId)
                .orElseThrow(() -> new ReferralNotFoundException(referralId));

        referral.startAssessment(assessorId, assessorName);
        return ReferralDto.fromWithoutResponses(referralRepository.save(referral));
    }

    /**
     * Bedöm remiss (acceptera/avvisa/begär komplettering).
     */
    public ReferralDto assessReferral(UUID referralId, AssessReferralRequest request, UUID assessorId, String assessorName) {
        log.info("Assessing referral {} with decision {}", referralId, request.decision());

        Referral referral = referralRepository.findById(referralId)
                .orElseThrow(() -> new ReferralNotFoundException(referralId));

        ReferralResponse response;

        switch (request.decision()) {
            case ACCEPT:
                referral.accept(
                        request.assessedPriority() != null ? request.assessedPriority() : referral.getPriority(),
                        request.plannedDate()
                );
                response = ReferralResponse.createAcceptance(
                        referral, assessorId, request.assessedPriority(), request.plannedDate(), request.responseText()
                );
                response.setResponderName(assessorName);
                referral.addResponse(response);
                eventPublisher.publish(new ReferralAcceptedEvent(referral));
                break;

            case REJECT:
                referral.reject(request.rejectionReason());
                response = ReferralResponse.createRejection(referral, assessorId, request.rejectionReason());
                response.setResponderName(assessorName);
                referral.addResponse(response);
                eventPublisher.publish(new ReferralRejectedEvent(referral, request.rejectionReason()));
                break;

            case REQUEST_INFORMATION:
                referral.requestMoreInformation(request.requestedInformation());
                response = ReferralResponse.createInformationRequest(referral, assessorId, request.requestedInformation());
                response.setResponderName(assessorName);
                referral.addResponse(response);
                eventPublisher.publish(new ReferralInformationRequestedEvent(referral, request.requestedInformation()));
                break;

            default:
                throw new IllegalArgumentException("Okänt bedömningsbeslut: " + request.decision());
        }

        return ReferralDto.from(referralRepository.save(referral));
    }

    /**
     * Vidareskicka remiss.
     */
    public ReferralDto forwardReferral(UUID referralId, ForwardReferralRequest request, UUID forwarderId, String forwarderName) {
        log.info("Forwarding referral {} to unit {}", referralId, request.targetUnitId());

        Referral referral = referralRepository.findById(referralId)
                .orElseThrow(() -> new ReferralNotFoundException(referralId));

        referral.forward(request.targetUnitId(), request.targetUnitName(), request.forwardReason());

        ReferralResponse response = ReferralResponse.createForward(
                referral, forwarderId, request.targetUnitId(), request.targetUnitName(), request.forwardReason()
        );
        response.setResponderName(forwarderName);
        referral.addResponse(response);

        Referral saved = referralRepository.save(referral);
        eventPublisher.publish(new ReferralForwardedEvent(saved, request.targetUnitId()));

        return ReferralDto.from(saved);
    }

    /**
     * Avsluta remiss (besök genomfört).
     */
    public ReferralDto completeReferral(UUID referralId, UUID resultingEncounterId, String finalResponseText, UUID responderId, String responderName) {
        log.info("Completing referral {} with encounter {}", referralId, resultingEncounterId);

        Referral referral = referralRepository.findById(referralId)
                .orElseThrow(() -> new ReferralNotFoundException(referralId));

        referral.complete(resultingEncounterId);

        if (finalResponseText != null && !finalResponseText.isBlank()) {
            ReferralResponse response = ReferralResponse.createFinalResponse(referral, responderId, finalResponseText);
            response.setResponderName(responderName);
            referral.addResponse(response);
        }

        Referral saved = referralRepository.save(referral);
        eventPublisher.publish(new ReferralCompletedEvent(saved));

        return ReferralDto.from(saved);
    }

    /**
     * Makulera remiss.
     */
    public ReferralDto cancelReferral(UUID referralId, String cancellationReason) {
        log.info("Cancelling referral {}: {}", referralId, cancellationReason);

        Referral referral = referralRepository.findById(referralId)
                .orElseThrow(() -> new ReferralNotFoundException(referralId));

        referral.cancel(cancellationReason);
        Referral saved = referralRepository.save(referral);

        eventPublisher.publish(new ReferralCancelledEvent(saved, cancellationReason));

        return ReferralDto.fromWithoutResponses(saved);
    }

    /**
     * Hämta remisser väntande på bedömning för enhet.
     */
    @Transactional(readOnly = true)
    public List<ReferralDto> getPendingAssessments(UUID receiverUnitId) {
        return referralRepository.findPendingAssessmentByUnit(receiverUnitId)
                .stream()
                .map(ReferralDto::fromWithoutResponses)
                .toList();
    }

    /**
     * Hämta skickade remisser för enhet.
     */
    @Transactional(readOnly = true)
    public List<ReferralDto> getSentReferrals(UUID senderUnitId) {
        return referralRepository.findBySenderUnitIdOrderByCreatedAtDesc(senderUnitId)
                .stream()
                .map(ReferralDto::fromWithoutResponses)
                .toList();
    }

    /**
     * Hämta mottagna remisser för enhet.
     */
    @Transactional(readOnly = true)
    public List<ReferralDto> getReceivedReferrals(UUID receiverUnitId) {
        return referralRepository.findByReceiverUnitIdOrderByReceivedAtDesc(receiverUnitId)
                .stream()
                .map(ReferralDto::fromWithoutResponses)
                .toList();
    }

    /**
     * Sök remisser.
     */
    @Transactional(readOnly = true)
    public Page<ReferralDto> searchReferrals(UUID patientId, UUID senderUnitId, UUID receiverUnitId,
                                              ReferralStatus status, ReferralType referralType,
                                              ReferralPriority priority, Instant fromDate, Instant toDate,
                                              Pageable pageable) {
        return referralRepository.search(
                patientId, senderUnitId, receiverUnitId, status, referralType, priority, fromDate, toDate, pageable
        ).map(ReferralDto::fromWithoutResponses);
    }

    /**
     * Räkna remisser väntande på bedömning.
     */
    @Transactional(readOnly = true)
    public long countPendingAssessments(UUID receiverUnitId) {
        return referralRepository.countPendingByReceiverUnit(receiverUnitId);
    }

    // Event classes

    public static class ReferralCreatedEvent extends DomainEvent {
        private final Referral referral;
        public ReferralCreatedEvent(Referral referral) {
            super(referral);
            this.referral = referral;
        }
        public Referral getReferral() { return referral; }
        @Override public String getAggregateType() { return "REFERRAL"; }
        @Override public UUID getAggregateId() { return referral.getId(); }
        @Override public String getEventType() { return "CREATED"; }
    }

    public static class ReferralSentEvent extends DomainEvent {
        private final Referral referral;
        public ReferralSentEvent(Referral referral) {
            super(referral);
            this.referral = referral;
        }
        public Referral getReferral() { return referral; }
        @Override public String getAggregateType() { return "REFERRAL"; }
        @Override public UUID getAggregateId() { return referral.getId(); }
        @Override public String getEventType() { return "SENT"; }
    }

    public static class ReferralReceivedEvent extends DomainEvent {
        private final Referral referral;
        public ReferralReceivedEvent(Referral referral) {
            super(referral);
            this.referral = referral;
        }
        public Referral getReferral() { return referral; }
        @Override public String getAggregateType() { return "REFERRAL"; }
        @Override public UUID getAggregateId() { return referral.getId(); }
        @Override public String getEventType() { return "RECEIVED"; }
    }

    public static class ReferralAcceptedEvent extends DomainEvent {
        private final Referral referral;
        public ReferralAcceptedEvent(Referral referral) {
            super(referral);
            this.referral = referral;
        }
        public Referral getReferral() { return referral; }
        @Override public String getAggregateType() { return "REFERRAL"; }
        @Override public UUID getAggregateId() { return referral.getId(); }
        @Override public String getEventType() { return "ACCEPTED"; }
    }

    public static class ReferralRejectedEvent extends DomainEvent {
        private final Referral referral;
        private final String reason;
        public ReferralRejectedEvent(Referral referral, String reason) {
            super(referral);
            this.referral = referral;
            this.reason = reason;
        }
        public Referral getReferral() { return referral; }
        public String getReason() { return reason; }
        @Override public String getAggregateType() { return "REFERRAL"; }
        @Override public UUID getAggregateId() { return referral.getId(); }
        @Override public String getEventType() { return "REJECTED"; }
    }

    public static class ReferralInformationRequestedEvent extends DomainEvent {
        private final Referral referral;
        private final String requestedInfo;
        public ReferralInformationRequestedEvent(Referral referral, String requestedInfo) {
            super(referral);
            this.referral = referral;
            this.requestedInfo = requestedInfo;
        }
        public Referral getReferral() { return referral; }
        public String getRequestedInfo() { return requestedInfo; }
        @Override public String getAggregateType() { return "REFERRAL"; }
        @Override public UUID getAggregateId() { return referral.getId(); }
        @Override public String getEventType() { return "INFORMATION_REQUESTED"; }
    }

    public static class ReferralForwardedEvent extends DomainEvent {
        private final Referral referral;
        private final UUID targetUnitId;
        public ReferralForwardedEvent(Referral referral, UUID targetUnitId) {
            super(referral);
            this.referral = referral;
            this.targetUnitId = targetUnitId;
        }
        public Referral getReferral() { return referral; }
        public UUID getTargetUnitId() { return targetUnitId; }
        @Override public String getAggregateType() { return "REFERRAL"; }
        @Override public UUID getAggregateId() { return referral.getId(); }
        @Override public String getEventType() { return "FORWARDED"; }
    }

    public static class ReferralCompletedEvent extends DomainEvent {
        private final Referral referral;
        public ReferralCompletedEvent(Referral referral) {
            super(referral);
            this.referral = referral;
        }
        public Referral getReferral() { return referral; }
        @Override public String getAggregateType() { return "REFERRAL"; }
        @Override public UUID getAggregateId() { return referral.getId(); }
        @Override public String getEventType() { return "COMPLETED"; }
    }

    public static class ReferralCancelledEvent extends DomainEvent {
        private final Referral referral;
        private final String reason;
        public ReferralCancelledEvent(Referral referral, String reason) {
            super(referral);
            this.referral = referral;
            this.reason = reason;
        }
        public Referral getReferral() { return referral; }
        public String getReason() { return reason; }
        @Override public String getAggregateType() { return "REFERRAL"; }
        @Override public UUID getAggregateId() { return referral.getId(); }
        @Override public String getEventType() { return "CANCELLED"; }
    }
}
