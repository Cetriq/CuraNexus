package se.curanexus.lab.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.curanexus.events.DomainEvent;
import se.curanexus.events.DomainEventPublisher;
import se.curanexus.lab.api.dto.*;
import se.curanexus.lab.domain.*;
import se.curanexus.lab.repository.LabOrderRepository;
import se.curanexus.lab.repository.LabResultRepository;
import se.curanexus.lab.repository.LabSpecimenRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class LabService {

    private static final Logger log = LoggerFactory.getLogger(LabService.class);

    private final LabOrderRepository orderRepository;
    private final LabResultRepository resultRepository;
    private final LabSpecimenRepository specimenRepository;
    private final DomainEventPublisher eventPublisher;

    public LabService(LabOrderRepository orderRepository,
                      LabResultRepository resultRepository,
                      LabSpecimenRepository specimenRepository,
                      DomainEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.resultRepository = resultRepository;
        this.specimenRepository = specimenRepository;
        this.eventPublisher = eventPublisher;
    }

    // === Beställningshantering ===

    /**
     * Skapa ny labbeställning.
     */
    public LabOrderDto createOrder(CreateLabOrderRequest request, UUID orderingUnitId, UUID orderingPractitionerId) {
        log.info("Creating lab order for patient {}", request.patientId());

        LabOrder order = new LabOrder(request.patientId(), orderingUnitId, orderingPractitionerId);

        // Patientinfo
        order.setPatientPersonnummer(request.patientPersonnummer());
        order.setPatientName(request.patientName());

        // Prioritet
        if (request.priority() != null) {
            order.setPriority(request.priority());
        }

        // Beställare
        order.setOrderingUnitHsaId(request.orderingUnitHsaId());
        order.setOrderingUnitName(request.orderingUnitName());
        order.setOrderingPractitionerHsaId(request.orderingPractitionerHsaId());
        order.setOrderingPractitionerName(request.orderingPractitionerName());

        // Lab
        order.setPerformingLabId(request.performingLabId());
        order.setPerformingLabHsaId(request.performingLabHsaId());
        order.setPerformingLabName(request.performingLabName());

        // Klinisk info
        order.setClinicalIndication(request.clinicalIndication());
        order.setDiagnosisCode(request.diagnosisCode());
        order.setDiagnosisText(request.diagnosisText());
        order.setRelevantMedication(request.relevantMedication());
        order.setFastingRequired(request.fastingRequired());
        order.setLabComment(request.labComment());

        // Kopplingar
        order.setEncounterId(request.encounterId());
        order.setReferralId(request.referralId());

        // Lägg till beställda tester
        if (request.tests() != null) {
            for (LabTestRequest test : request.tests()) {
                LabOrderItem item = new LabOrderItem(
                        test.testCode(),
                        test.codeSystem(),
                        test.testName(),
                        test.specimenType()
                );
                item.setTestDescription(test.testDescription());
                item.setItemComment(test.comment());
                order.addOrderItem(item);
            }
        }

        if (request.sendImmediately() && !order.getOrderItems().isEmpty()) {
            order.send();
        }

        LabOrder saved = orderRepository.save(order);
        log.info("Created lab order {} with reference {}", saved.getId(), saved.getOrderReference());

        eventPublisher.publish(new LabOrderCreatedEvent(saved));

        return LabOrderDto.from(saved);
    }

    /**
     * Hämta labbeställning.
     */
    @Transactional(readOnly = true)
    public LabOrderDto getOrder(UUID orderId) {
        return orderRepository.findById(orderId)
                .map(LabOrderDto::from)
                .orElseThrow(() -> new LabOrderNotFoundException(orderId));
    }

    /**
     * Hämta labbeställning via referens.
     */
    @Transactional(readOnly = true)
    public LabOrderDto getOrderByReference(String orderReference) {
        return orderRepository.findByOrderReference(orderReference)
                .map(LabOrderDto::from)
                .orElseThrow(() -> new LabOrderNotFoundException("Beställningsreferens hittades ej: " + orderReference));
    }

    /**
     * Hämta patientens labbeställningar.
     */
    @Transactional(readOnly = true)
    public List<LabOrderDto> getPatientOrders(UUID patientId) {
        return orderRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
                .stream()
                .map(LabOrderDto::from)
                .toList();
    }

    /**
     * Lägg till test till beställning.
     */
    public LabOrderDto addTestToOrder(UUID orderId, LabTestRequest testRequest) {
        log.info("Adding test {} to order {}", testRequest.testCode(), orderId);

        LabOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new LabOrderNotFoundException(orderId));

        LabOrderItem item = new LabOrderItem(
                testRequest.testCode(),
                testRequest.codeSystem(),
                testRequest.testName(),
                testRequest.specimenType()
        );
        item.setTestDescription(testRequest.testDescription());
        item.setItemComment(testRequest.comment());

        order.addOrderItem(item);
        return LabOrderDto.from(orderRepository.save(order));
    }

    /**
     * Skicka beställning till lab.
     */
    public LabOrderDto sendOrder(UUID orderId) {
        log.info("Sending lab order {}", orderId);

        LabOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new LabOrderNotFoundException(orderId));

        order.send();
        LabOrder saved = orderRepository.save(order);

        eventPublisher.publish(new LabOrderSentEvent(saved));

        return LabOrderDto.from(saved);
    }

    /**
     * Markera beställning som mottagen av lab.
     */
    public LabOrderDto markOrderReceived(UUID orderId) {
        log.info("Marking lab order {} as received", orderId);

        LabOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new LabOrderNotFoundException(orderId));

        order.markReceived();
        return LabOrderDto.from(orderRepository.save(order));
    }

    /**
     * Makulera beställning.
     */
    public LabOrderDto cancelOrder(UUID orderId, String reason) {
        log.info("Cancelling lab order {}: {}", orderId, reason);

        LabOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new LabOrderNotFoundException(orderId));

        order.cancel(reason);
        LabOrder saved = orderRepository.save(order);

        eventPublisher.publish(new LabOrderCancelledEvent(saved, reason));

        return LabOrderDto.from(saved);
    }

    // === Provhantering ===

    /**
     * Registrera provtagning.
     */
    public LabOrderDto registerSpecimenCollection(UUID orderId, RegisterSpecimenRequest request,
                                                   UUID collectorId, String collectorName) {
        log.info("Registering specimen collection for order {}", orderId);

        LabOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new LabOrderNotFoundException(orderId));

        LabSpecimen specimen = new LabSpecimen(request.specimenType(), request.barcode());
        specimen.setCollectionMethod(request.collectionMethod());
        specimen.setBodySite(request.bodySite());
        specimen.setQuantity(request.quantity());
        specimen.setContainerType(request.containerType());
        specimen.setSpecimenComment(request.comment());
        specimen.collect(collectorId, collectorName);

        order.addSpecimen(specimen);
        order.markSpecimenCollected();

        LabOrder saved = orderRepository.save(order);
        eventPublisher.publish(new SpecimenCollectedEvent(saved, specimen));

        return LabOrderDto.from(saved);
    }

    /**
     * Registrera mottagning av prov på lab.
     */
    public LabSpecimenDto receiveSpecimenAtLab(UUID specimenId) {
        log.info("Receiving specimen {} at lab", specimenId);

        LabSpecimen specimen = specimenRepository.findById(specimenId)
                .orElseThrow(() -> new IllegalArgumentException("Prov hittades ej: " + specimenId));

        specimen.receiveAtLab();
        return LabSpecimenDto.from(specimenRepository.save(specimen));
    }

    /**
     * Avvisa prov.
     */
    public LabSpecimenDto rejectSpecimen(UUID specimenId, String reason) {
        log.info("Rejecting specimen {}: {}", specimenId, reason);

        LabSpecimen specimen = specimenRepository.findById(specimenId)
                .orElseThrow(() -> new IllegalArgumentException("Prov hittades ej: " + specimenId));

        specimen.reject(reason);
        return LabSpecimenDto.from(specimenRepository.save(specimen));
    }

    // === Resultathantering ===

    /**
     * Registrera analysresultat.
     */
    public LabResultDto registerResult(UUID orderItemId, RegisterResultRequest request,
                                        UUID analyzerId, String analyzerName) {
        log.info("Registering result for order item {}", orderItemId);

        LabOrder order = orderRepository.findAll().stream()
                .filter(o -> o.getOrderItems().stream().anyMatch(i -> i.getId().equals(orderItemId)))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Order item hittades ej: " + orderItemId));

        LabOrderItem item = order.getOrderItems().stream()
                .filter(i -> i.getId().equals(orderItemId))
                .findFirst()
                .orElseThrow();

        LabResult result = new LabResult(item);

        if (request.valueNumeric() != null) {
            result.registerNumericResult(
                    request.valueNumeric(),
                    request.unit(),
                    request.referenceLow(),
                    request.referenceHigh()
            );
        } else if (request.valueText() != null) {
            result.registerTextResult(request.valueText());
            if (request.abnormalFlag() != null) {
                result.setAbnormalFlagManually(request.abnormalFlag(),
                        request.abnormalFlag() == AbnormalFlag.CRITICAL_HIGH ||
                        request.abnormalFlag() == AbnormalFlag.CRITICAL_LOW);
            }
        }

        result.setReferenceRangeText(request.referenceRangeText());
        result.setMethod(request.method());
        result.setInstrument(request.instrument());
        result.setPerformingDepartment(request.performingDepartment());
        result.setLabComment(request.labComment());
        result.setAnalyzerId(analyzerId);
        result.setAnalyzerName(analyzerName);

        item.setResult(result);

        // Uppdatera orderstatus
        if (order.getStatus() == LabOrderStatus.SPECIMEN_COLLECTED ||
            order.getStatus() == LabOrderStatus.RECEIVED) {
            order.startAnalysis();
        }

        // Kontrollera om alla resultat är klara
        boolean allResultsRegistered = order.getOrderItems().stream()
                .allMatch(i -> i.getResult() != null);
        if (allResultsRegistered) {
            order.registerPartialResults();
        }

        LabOrder saved = orderRepository.save(order);

        // Publicera kritiskt resultat-event om relevant
        if (result.getIsCritical()) {
            eventPublisher.publish(new CriticalResultEvent(saved, result));
        }

        return LabResultDto.from(result);
    }

    /**
     * Granska och godkänn resultat.
     */
    public LabResultDto reviewResult(UUID resultId, UUID reviewerId, String reviewerName) {
        log.info("Reviewing result {}", resultId);

        LabResult result = resultRepository.findById(resultId)
                .orElseThrow(() -> new IllegalArgumentException("Resultat hittades ej: " + resultId));

        result.review(reviewerId, reviewerName);
        LabResult saved = resultRepository.save(result);

        // Kontrollera om hela ordern är klar
        LabOrder order = result.getOrderItem().getLabOrder();
        if (order.hasAllResults()) {
            order.complete();
            orderRepository.save(order);
            eventPublisher.publish(new LabOrderCompletedEvent(order));
        }

        return LabResultDto.from(saved);
    }

    /**
     * Hämta patientens labresultat.
     */
    @Transactional(readOnly = true)
    public List<LabResultDto> getPatientResults(UUID patientId) {
        return resultRepository.findByPatientId(patientId)
                .stream()
                .map(LabResultDto::from)
                .toList();
    }

    /**
     * Hämta resultattrend för specifikt test.
     */
    @Transactional(readOnly = true)
    public List<LabResultDto> getResultTrend(UUID patientId, String testCode) {
        return resultRepository.findByPatientAndTestCode(patientId, testCode)
                .stream()
                .map(LabResultDto::from)
                .toList();
    }

    // === Listor och statistik ===

    /**
     * Hämta beställningar väntande på provtagning.
     */
    @Transactional(readOnly = true)
    public List<LabOrderDto> getPendingSpecimenCollection(UUID unitId) {
        return orderRepository.findPendingSpecimenCollectionByUnit(unitId)
                .stream()
                .map(LabOrderDto::from)
                .toList();
    }

    /**
     * Hämta beställningar under analys för lab.
     */
    @Transactional(readOnly = true)
    public List<LabOrderDto> getInProgressByLab(UUID labId) {
        return orderRepository.findInProgressByLab(labId)
                .stream()
                .map(LabOrderDto::from)
                .toList();
    }

    /**
     * Hämta beställningar med kritiska resultat.
     */
    @Transactional(readOnly = true)
    public List<LabOrderDto> getOrdersWithCriticalResults(UUID unitId) {
        return orderRepository.findWithCriticalResultsByUnit(unitId)
                .stream()
                .map(LabOrderDto::from)
                .toList();
    }

    /**
     * Hämta resultat väntande på granskning.
     */
    @Transactional(readOnly = true)
    public List<LabResultDto> getPendingReview(UUID labId) {
        return resultRepository.findPendingReviewByLab(labId)
                .stream()
                .map(LabResultDto::from)
                .toList();
    }

    /**
     * Sök beställningar.
     */
    @Transactional(readOnly = true)
    public Page<LabOrderDto> searchOrders(UUID patientId, UUID orderingUnitId, UUID performingLabId,
                                           LabOrderStatus status, LabOrderPriority priority,
                                           Instant fromDate, Instant toDate, Pageable pageable) {
        return orderRepository.search(
                patientId, orderingUnitId, performingLabId, status, priority, fromDate, toDate, pageable
        ).map(LabOrderDto::from);
    }

    // === Event classes ===

    public static class LabOrderCreatedEvent extends DomainEvent {
        private final LabOrder order;
        public LabOrderCreatedEvent(LabOrder order) {
            super(order);
            this.order = order;
        }
        public LabOrder getOrder() { return order; }
        @Override public String getAggregateType() { return "LAB_ORDER"; }
        @Override public UUID getAggregateId() { return order.getId(); }
        @Override public String getEventType() { return "CREATED"; }
    }

    public static class LabOrderSentEvent extends DomainEvent {
        private final LabOrder order;
        public LabOrderSentEvent(LabOrder order) {
            super(order);
            this.order = order;
        }
        public LabOrder getOrder() { return order; }
        @Override public String getAggregateType() { return "LAB_ORDER"; }
        @Override public UUID getAggregateId() { return order.getId(); }
        @Override public String getEventType() { return "SENT"; }
    }

    public static class LabOrderCancelledEvent extends DomainEvent {
        private final LabOrder order;
        private final String reason;
        public LabOrderCancelledEvent(LabOrder order, String reason) {
            super(order);
            this.order = order;
            this.reason = reason;
        }
        public LabOrder getOrder() { return order; }
        public String getReason() { return reason; }
        @Override public String getAggregateType() { return "LAB_ORDER"; }
        @Override public UUID getAggregateId() { return order.getId(); }
        @Override public String getEventType() { return "CANCELLED"; }
    }

    public static class LabOrderCompletedEvent extends DomainEvent {
        private final LabOrder order;
        public LabOrderCompletedEvent(LabOrder order) {
            super(order);
            this.order = order;
        }
        public LabOrder getOrder() { return order; }
        @Override public String getAggregateType() { return "LAB_ORDER"; }
        @Override public UUID getAggregateId() { return order.getId(); }
        @Override public String getEventType() { return "COMPLETED"; }
    }

    public static class SpecimenCollectedEvent extends DomainEvent {
        private final LabOrder order;
        private final LabSpecimen specimen;
        public SpecimenCollectedEvent(LabOrder order, LabSpecimen specimen) {
            super(order);
            this.order = order;
            this.specimen = specimen;
        }
        public LabOrder getOrder() { return order; }
        public LabSpecimen getSpecimen() { return specimen; }
        @Override public String getAggregateType() { return "LAB_ORDER"; }
        @Override public UUID getAggregateId() { return order.getId(); }
        @Override public String getEventType() { return "SPECIMEN_COLLECTED"; }
    }

    public static class CriticalResultEvent extends DomainEvent {
        private final LabOrder order;
        private final LabResult result;
        public CriticalResultEvent(LabOrder order, LabResult result) {
            super(order);
            this.order = order;
            this.result = result;
        }
        public LabOrder getOrder() { return order; }
        public LabResult getResult() { return result; }
        @Override public String getAggregateType() { return "LAB_ORDER"; }
        @Override public UUID getAggregateId() { return order.getId(); }
        @Override public String getEventType() { return "CRITICAL_RESULT"; }
    }
}
