package se.curanexus.certificates.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.curanexus.certificates.api.dto.*;
import se.curanexus.certificates.domain.CertificateStatus;
import se.curanexus.certificates.service.CertificateService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/certificates")
@Tag(name = "Certificates", description = "API för intyg och läkarutlåtanden")
public class CertificateController {

    private final CertificateService certificateService;

    public CertificateController(CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    @PostMapping
    @Operation(summary = "Skapa nytt intyg")
    public ResponseEntity<CertificateDto> createCertificate(
            @Valid @RequestBody CreateCertificateRequest request) {
        CertificateDto created = certificateService.createCertificate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Hämta intyg med ID")
    public CertificateDto getCertificate(@PathVariable UUID id) {
        return certificateService.getCertificate(id);
    }

    @GetMapping("/number/{certificateNumber}")
    @Operation(summary = "Hämta intyg med intygsnummer")
    public CertificateDto getCertificateByNumber(@PathVariable String certificateNumber) {
        return certificateService.getCertificateByNumber(certificateNumber);
    }

    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Hämta patientens intyg")
    public List<CertificateSummaryDto> getPatientCertificates(
            @PathVariable UUID patientId,
            @RequestParam(required = false) CertificateStatus status) {
        return certificateService.getPatientCertificates(patientId, status);
    }

    @GetMapping("/encounter/{encounterId}")
    @Operation(summary = "Hämta intyg för vårdkontakt")
    public List<CertificateSummaryDto> getEncounterCertificates(
            @PathVariable UUID encounterId) {
        return certificateService.getEncounterCertificates(encounterId);
    }

    @GetMapping("/issuer/{issuerId}")
    @Operation(summary = "Hämta intyg utfärdade av vårdgivare")
    public Page<CertificateSummaryDto> getIssuerCertificates(
            @PathVariable UUID issuerId,
            Pageable pageable) {
        return certificateService.getIssuerCertificates(issuerId, pageable);
    }

    @GetMapping("/issuer/{issuerId}/drafts")
    @Operation(summary = "Hämta utkast för vårdgivare")
    public List<CertificateSummaryDto> getDraftCertificates(@PathVariable UUID issuerId) {
        return certificateService.getDraftCertificates(issuerId);
    }

    @GetMapping("/patient/{patientId}/sick-leaves/active")
    @Operation(summary = "Hämta aktiva sjukintyg för patient")
    public List<CertificateSummaryDto> getActiveSickLeaves(@PathVariable UUID patientId) {
        return certificateService.getActiveSickLeaves(patientId);
    }

    @GetMapping("/pending-send")
    @Operation(summary = "Hämta intyg som väntar på att skickas")
    public List<CertificateSummaryDto> getPendingSendCertificates() {
        return certificateService.getPendingSendCertificates();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Uppdatera intyg (endast utkast)")
    public CertificateDto updateCertificate(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCertificateRequest request) {
        return certificateService.updateCertificate(id, request);
    }

    @PostMapping("/{id}/sign")
    @Operation(summary = "Signera intyg")
    public CertificateDto signCertificate(
            @PathVariable UUID id,
            @Valid @RequestBody SignCertificateRequest request) {
        return certificateService.signCertificate(id, request);
    }

    @PostMapping("/{id}/send")
    @Operation(summary = "Skicka intyg till mottagare")
    public CertificateDto sendCertificate(@PathVariable UUID id) {
        return certificateService.sendCertificate(id);
    }

    @PostMapping("/{id}/revoke")
    @Operation(summary = "Makulera intyg")
    public CertificateDto revokeCertificate(
            @PathVariable UUID id,
            @Valid @RequestBody RevokeCertificateRequest request) {
        return certificateService.revokeCertificate(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Ta bort intyg (endast utkast)")
    public ResponseEntity<Void> deleteCertificate(@PathVariable UUID id) {
        certificateService.deleteCertificate(id);
        return ResponseEntity.noContent().build();
    }
}
