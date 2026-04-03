package se.curanexus.lab.api;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.curanexus.lab.api.dto.*;
import se.curanexus.lab.domain.LabOrderPriority;
import se.curanexus.lab.domain.LabOrderStatus;
import se.curanexus.lab.service.LabService;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/lab")
public class LabController {

    private final LabService labService;

    public LabController(LabService labService) {
        this.labService = labService;
    }

    // === Beställningar ===

    @PostMapping("/orders")
    public ResponseEntity<LabOrderDto> createOrder(
            @Valid @RequestBody CreateLabOrderRequest request,
            @RequestHeader("X-Unit-Id") UUID unitId,
            @RequestHeader("X-Practitioner-Id") UUID practitionerId) {
        LabOrderDto created = labService.createOrder(request, unitId, practitionerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<LabOrderDto> getOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(labService.getOrder(id));
    }

    @GetMapping("/orders/reference/{reference}")
    public ResponseEntity<LabOrderDto> getOrderByReference(@PathVariable String reference) {
        return ResponseEntity.ok(labService.getOrderByReference(reference));
    }

    @PostMapping("/orders/{id}/tests")
    public ResponseEntity<LabOrderDto> addTestToOrder(
            @PathVariable UUID id,
            @Valid @RequestBody LabTestRequest testRequest) {
        return ResponseEntity.ok(labService.addTestToOrder(id, testRequest));
    }

    @PostMapping("/orders/{id}/send")
    public ResponseEntity<LabOrderDto> sendOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(labService.sendOrder(id));
    }

    @PostMapping("/orders/{id}/receive")
    public ResponseEntity<LabOrderDto> markOrderReceived(@PathVariable UUID id) {
        return ResponseEntity.ok(labService.markOrderReceived(id));
    }

    @DeleteMapping("/orders/{id}")
    public ResponseEntity<LabOrderDto> cancelOrder(
            @PathVariable UUID id,
            @RequestParam String reason) {
        return ResponseEntity.ok(labService.cancelOrder(id, reason));
    }

    // === Prover ===

    @PostMapping("/orders/{orderId}/specimens")
    public ResponseEntity<LabOrderDto> registerSpecimenCollection(
            @PathVariable UUID orderId,
            @Valid @RequestBody RegisterSpecimenRequest request,
            @RequestHeader("X-Practitioner-Id") UUID collectorId,
            @RequestHeader(value = "X-Practitioner-Name", required = false) String collectorName) {
        return ResponseEntity.ok(labService.registerSpecimenCollection(orderId, request, collectorId, collectorName));
    }

    @PostMapping("/specimens/{id}/receive")
    public ResponseEntity<LabSpecimenDto> receiveSpecimenAtLab(@PathVariable UUID id) {
        return ResponseEntity.ok(labService.receiveSpecimenAtLab(id));
    }

    @PostMapping("/specimens/{id}/reject")
    public ResponseEntity<LabSpecimenDto> rejectSpecimen(
            @PathVariable UUID id,
            @RequestParam String reason) {
        return ResponseEntity.ok(labService.rejectSpecimen(id, reason));
    }

    // === Resultat ===

    @PostMapping("/order-items/{itemId}/results")
    public ResponseEntity<LabResultDto> registerResult(
            @PathVariable UUID itemId,
            @Valid @RequestBody RegisterResultRequest request,
            @RequestHeader("X-Practitioner-Id") UUID analyzerId,
            @RequestHeader(value = "X-Practitioner-Name", required = false) String analyzerName) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(labService.registerResult(itemId, request, analyzerId, analyzerName));
    }

    @PostMapping("/results/{id}/review")
    public ResponseEntity<LabResultDto> reviewResult(
            @PathVariable UUID id,
            @RequestHeader("X-Practitioner-Id") UUID reviewerId,
            @RequestHeader(value = "X-Practitioner-Name", required = false) String reviewerName) {
        return ResponseEntity.ok(labService.reviewResult(id, reviewerId, reviewerName));
    }

    // === Patientdata ===

    @GetMapping("/patient/{patientId}/orders")
    public ResponseEntity<List<LabOrderDto>> getPatientOrders(@PathVariable UUID patientId) {
        return ResponseEntity.ok(labService.getPatientOrders(patientId));
    }

    @GetMapping("/patient/{patientId}/results")
    public ResponseEntity<List<LabResultDto>> getPatientResults(@PathVariable UUID patientId) {
        return ResponseEntity.ok(labService.getPatientResults(patientId));
    }

    @GetMapping("/patient/{patientId}/results/trend")
    public ResponseEntity<List<LabResultDto>> getResultTrend(
            @PathVariable UUID patientId,
            @RequestParam String testCode) {
        return ResponseEntity.ok(labService.getResultTrend(patientId, testCode));
    }

    // === Arbetslistor ===

    @GetMapping("/unit/{unitId}/pending-collection")
    public ResponseEntity<List<LabOrderDto>> getPendingSpecimenCollection(@PathVariable UUID unitId) {
        return ResponseEntity.ok(labService.getPendingSpecimenCollection(unitId));
    }

    @GetMapping("/unit/{unitId}/critical-results")
    public ResponseEntity<List<LabOrderDto>> getOrdersWithCriticalResults(@PathVariable UUID unitId) {
        return ResponseEntity.ok(labService.getOrdersWithCriticalResults(unitId));
    }

    @GetMapping("/lab/{labId}/in-progress")
    public ResponseEntity<List<LabOrderDto>> getInProgressByLab(@PathVariable UUID labId) {
        return ResponseEntity.ok(labService.getInProgressByLab(labId));
    }

    @GetMapping("/lab/{labId}/pending-review")
    public ResponseEntity<List<LabResultDto>> getPendingReview(@PathVariable UUID labId) {
        return ResponseEntity.ok(labService.getPendingReview(labId));
    }

    // === Sök ===

    @GetMapping("/orders/search")
    public ResponseEntity<Page<LabOrderDto>> searchOrders(
            @RequestParam(required = false) UUID patientId,
            @RequestParam(required = false) UUID orderingUnitId,
            @RequestParam(required = false) UUID performingLabId,
            @RequestParam(required = false) LabOrderStatus status,
            @RequestParam(required = false) LabOrderPriority priority,
            @RequestParam(required = false) Instant fromDate,
            @RequestParam(required = false) Instant toDate,
            Pageable pageable) {
        return ResponseEntity.ok(labService.searchOrders(
                patientId, orderingUnitId, performingLabId, status, priority, fromDate, toDate, pageable));
    }
}
