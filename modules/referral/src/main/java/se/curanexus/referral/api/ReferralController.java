package se.curanexus.referral.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.curanexus.referral.api.dto.*;
import se.curanexus.referral.domain.ReferralPriority;
import se.curanexus.referral.domain.ReferralStatus;
import se.curanexus.referral.domain.ReferralType;
import se.curanexus.referral.service.ReferralService;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/referrals")
@Tag(name = "Referrals", description = "Remisshantering")
public class ReferralController {

    private final ReferralService referralService;

    public ReferralController(ReferralService referralService) {
        this.referralService = referralService;
    }

    @PostMapping
    @Operation(summary = "Skapa ny remiss")
    public ResponseEntity<ReferralDto> createReferral(
            @Valid @RequestBody CreateReferralRequest request,
            @RequestHeader("X-Unit-Id") UUID senderUnitId,
            @RequestHeader("X-User-Id") UUID senderPractitionerId) {
        ReferralDto referral = referralService.createReferral(request, senderUnitId, senderPractitionerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(referral);
    }

    @GetMapping("/{referralId}")
    @Operation(summary = "Hämta remiss via ID")
    public ResponseEntity<ReferralDto> getReferral(@PathVariable UUID referralId) {
        return ResponseEntity.ok(referralService.getReferral(referralId));
    }

    @GetMapping("/reference/{referralReference}")
    @Operation(summary = "Hämta remiss via referensnummer")
    public ResponseEntity<ReferralDto> getReferralByReference(@PathVariable String referralReference) {
        return ResponseEntity.ok(referralService.getReferralByReference(referralReference));
    }

    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Hämta patientens remisser")
    public ResponseEntity<List<ReferralDto>> getPatientReferrals(@PathVariable UUID patientId) {
        return ResponseEntity.ok(referralService.getPatientReferrals(patientId));
    }

    @PostMapping("/{referralId}/send")
    @Operation(summary = "Skicka remiss")
    public ResponseEntity<ReferralDto> sendReferral(@PathVariable UUID referralId) {
        return ResponseEntity.ok(referralService.sendReferral(referralId));
    }

    @PostMapping("/{referralId}/receive")
    @Operation(summary = "Markera remiss som mottagen")
    public ResponseEntity<ReferralDto> markReceived(@PathVariable UUID referralId) {
        return ResponseEntity.ok(referralService.markReceived(referralId));
    }

    @PostMapping("/{referralId}/start-assessment")
    @Operation(summary = "Starta bedömning av remiss")
    public ResponseEntity<ReferralDto> startAssessment(
            @PathVariable UUID referralId,
            @RequestHeader("X-User-Id") UUID assessorId,
            @RequestHeader(value = "X-User-Name", required = false) String assessorName) {
        return ResponseEntity.ok(referralService.startAssessment(referralId, assessorId, assessorName));
    }

    @PostMapping("/{referralId}/assess")
    @Operation(summary = "Bedöm remiss (acceptera/avvisa/begär komplettering)")
    public ResponseEntity<ReferralDto> assessReferral(
            @PathVariable UUID referralId,
            @Valid @RequestBody AssessReferralRequest request,
            @RequestHeader("X-User-Id") UUID assessorId,
            @RequestHeader(value = "X-User-Name", required = false) String assessorName) {
        return ResponseEntity.ok(referralService.assessReferral(referralId, request, assessorId, assessorName));
    }

    @PostMapping("/{referralId}/forward")
    @Operation(summary = "Vidareskicka remiss till annan enhet")
    public ResponseEntity<ReferralDto> forwardReferral(
            @PathVariable UUID referralId,
            @Valid @RequestBody ForwardReferralRequest request,
            @RequestHeader("X-User-Id") UUID forwarderId,
            @RequestHeader(value = "X-User-Name", required = false) String forwarderName) {
        return ResponseEntity.ok(referralService.forwardReferral(referralId, request, forwarderId, forwarderName));
    }

    @PostMapping("/{referralId}/complete")
    @Operation(summary = "Avsluta remiss (besök genomfört)")
    public ResponseEntity<ReferralDto> completeReferral(
            @PathVariable UUID referralId,
            @RequestParam UUID resultingEncounterId,
            @RequestParam(required = false) String finalResponse,
            @RequestHeader("X-User-Id") UUID responderId,
            @RequestHeader(value = "X-User-Name", required = false) String responderName) {
        return ResponseEntity.ok(referralService.completeReferral(
                referralId, resultingEncounterId, finalResponse, responderId, responderName));
    }

    @PostMapping("/{referralId}/cancel")
    @Operation(summary = "Makulera remiss")
    public ResponseEntity<ReferralDto> cancelReferral(
            @PathVariable UUID referralId,
            @RequestParam String reason) {
        return ResponseEntity.ok(referralService.cancelReferral(referralId, reason));
    }

    @GetMapping("/unit/{unitId}/pending")
    @Operation(summary = "Hämta remisser väntande på bedömning för enhet")
    public ResponseEntity<List<ReferralDto>> getPendingAssessments(@PathVariable UUID unitId) {
        return ResponseEntity.ok(referralService.getPendingAssessments(unitId));
    }

    @GetMapping("/unit/{unitId}/sent")
    @Operation(summary = "Hämta skickade remisser för enhet")
    public ResponseEntity<List<ReferralDto>> getSentReferrals(@PathVariable UUID unitId) {
        return ResponseEntity.ok(referralService.getSentReferrals(unitId));
    }

    @GetMapping("/unit/{unitId}/received")
    @Operation(summary = "Hämta mottagna remisser för enhet")
    public ResponseEntity<List<ReferralDto>> getReceivedReferrals(@PathVariable UUID unitId) {
        return ResponseEntity.ok(referralService.getReceivedReferrals(unitId));
    }

    @GetMapping("/unit/{unitId}/pending/count")
    @Operation(summary = "Räkna remisser väntande på bedömning")
    public ResponseEntity<Long> countPendingAssessments(@PathVariable UUID unitId) {
        return ResponseEntity.ok(referralService.countPendingAssessments(unitId));
    }

    @GetMapping("/search")
    @Operation(summary = "Sök remisser")
    public ResponseEntity<Page<ReferralDto>> searchReferrals(
            @RequestParam(required = false) UUID patientId,
            @RequestParam(required = false) UUID senderUnitId,
            @RequestParam(required = false) UUID receiverUnitId,
            @RequestParam(required = false) ReferralStatus status,
            @RequestParam(required = false) ReferralType referralType,
            @RequestParam(required = false) ReferralPriority priority,
            @RequestParam(required = false) Instant fromDate,
            @RequestParam(required = false) Instant toDate,
            Pageable pageable) {
        return ResponseEntity.ok(referralService.searchReferrals(
                patientId, senderUnitId, receiverUnitId, status, referralType, priority, fromDate, toDate, pageable));
    }
}
