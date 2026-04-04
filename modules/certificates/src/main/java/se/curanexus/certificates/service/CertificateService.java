package se.curanexus.certificates.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.curanexus.certificates.api.dto.*;
import se.curanexus.certificates.domain.*;
import se.curanexus.certificates.repository.CertificateRepository;
import se.curanexus.certificates.repository.CertificateTemplateRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final CertificateTemplateRepository templateRepository;

    public CertificateService(CertificateRepository certificateRepository,
                               CertificateTemplateRepository templateRepository) {
        this.certificateRepository = certificateRepository;
        this.templateRepository = templateRepository;
    }

    @Transactional(readOnly = true)
    public CertificateDto getCertificate(UUID id) {
        Certificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> new CertificateNotFoundException(id));
        return CertificateDto.from(certificate);
    }

    @Transactional(readOnly = true)
    public CertificateDto getCertificateByNumber(String certificateNumber) {
        Certificate certificate = certificateRepository.findByCertificateNumber(certificateNumber)
                .orElseThrow(() -> new CertificateNotFoundException(certificateNumber));
        return CertificateDto.from(certificate);
    }

    @Transactional(readOnly = true)
    public List<CertificateSummaryDto> getPatientCertificates(UUID patientId, CertificateStatus status) {
        List<Certificate> certificates;
        if (status != null) {
            certificates = certificateRepository.findByPatientIdAndStatus(patientId, status);
        } else {
            certificates = certificateRepository.findByPatientId(patientId);
        }
        return certificates.stream()
                .map(CertificateSummaryDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CertificateSummaryDto> getEncounterCertificates(UUID encounterId) {
        return certificateRepository.findByEncounterId(encounterId).stream()
                .map(CertificateSummaryDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<CertificateSummaryDto> getIssuerCertificates(UUID issuerId, Pageable pageable) {
        return certificateRepository.findByIssuerId(issuerId, pageable)
                .map(CertificateSummaryDto::from);
    }

    @Transactional(readOnly = true)
    public List<CertificateSummaryDto> getDraftCertificates(UUID issuerId) {
        return certificateRepository.findDraftsByIssuer(issuerId).stream()
                .map(CertificateSummaryDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CertificateSummaryDto> getActiveSickLeaves(UUID patientId) {
        return certificateRepository.findActiveSickLeaves(patientId, LocalDate.now()).stream()
                .map(CertificateSummaryDto::from)
                .toList();
    }

    public CertificateDto createCertificate(CreateCertificateRequest request) {
        CertificateTemplate template = templateRepository.findActiveByCode(request.templateCode())
                .orElseThrow(() -> new CertificateTemplateNotFoundException(request.templateCode()));

        Certificate certificate = new Certificate(template, request.patientId(), request.issuerId());
        certificate.setEncounterId(request.encounterId());
        certificate.setData(request.data() != null ? request.data() : "{}");
        certificate.setPeriodStart(request.periodStart());
        certificate.setPeriodEnd(request.periodEnd());
        certificate.setDiagnosisCodes(request.diagnosisCodes());
        certificate.setDiagnosisDescription(request.diagnosisDescription());
        certificate.setIssuerName(request.issuerName());
        certificate.setIssuerRole(request.issuerRole());
        certificate.setIssuerUnitId(request.issuerUnitId());
        certificate.setIssuerUnitName(request.issuerUnitName());

        if (request.replacesId() != null) {
            Certificate replaced = certificateRepository.findById(request.replacesId())
                    .orElseThrow(() -> new CertificateNotFoundException(request.replacesId()));
            certificate.setReplacesId(request.replacesId());
        }

        Certificate saved = certificateRepository.save(certificate);
        return CertificateDto.from(saved);
    }

    public CertificateDto updateCertificate(UUID id, UpdateCertificateRequest request) {
        Certificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> new CertificateNotFoundException(id));

        if (certificate.getStatus() != CertificateStatus.DRAFT) {
            throw new IllegalStateException("Can only update certificates in DRAFT status");
        }

        if (request.data() != null) {
            certificate.setData(request.data());
        }
        if (request.periodStart() != null) {
            certificate.setPeriodStart(request.periodStart());
        }
        if (request.periodEnd() != null) {
            certificate.setPeriodEnd(request.periodEnd());
        }
        if (request.diagnosisCodes() != null) {
            certificate.setDiagnosisCodes(request.diagnosisCodes());
        }
        if (request.diagnosisDescription() != null) {
            certificate.setDiagnosisDescription(request.diagnosisDescription());
        }

        Certificate saved = certificateRepository.save(certificate);
        return CertificateDto.from(saved);
    }

    public CertificateDto signCertificate(UUID id, SignCertificateRequest request) {
        Certificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> new CertificateNotFoundException(id));

        if (certificate.getStatus() != CertificateStatus.DRAFT) {
            throw new IllegalStateException("Can only sign certificates in DRAFT status");
        }

        certificate.sign(request.signature());

        // Mark replaced certificate if exists
        if (certificate.getReplacesId() != null) {
            Certificate replaced = certificateRepository.findById(certificate.getReplacesId())
                    .orElse(null);
            if (replaced != null) {
                replaced.replaceWith(certificate.getId());
                certificateRepository.save(replaced);
            }
        }

        Certificate saved = certificateRepository.save(certificate);
        return CertificateDto.from(saved);
    }

    public CertificateDto sendCertificate(UUID id) {
        Certificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> new CertificateNotFoundException(id));

        if (certificate.getStatus() != CertificateStatus.SIGNED) {
            throw new IllegalStateException("Can only send signed certificates");
        }

        // In real implementation, this would integrate with external systems
        // like Intygshjälpen or Försäkringskassan
        String trackingId = "TRACK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        certificate.markSent(trackingId);

        Certificate saved = certificateRepository.save(certificate);
        return CertificateDto.from(saved);
    }

    public CertificateDto revokeCertificate(UUID id, RevokeCertificateRequest request) {
        Certificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> new CertificateNotFoundException(id));

        if (certificate.getStatus() == CertificateStatus.DRAFT) {
            throw new IllegalStateException("Cannot revoke draft certificates - delete them instead");
        }

        certificate.revoke(request.reason());

        Certificate saved = certificateRepository.save(certificate);
        return CertificateDto.from(saved);
    }

    public void deleteCertificate(UUID id) {
        Certificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> new CertificateNotFoundException(id));

        if (certificate.getStatus() != CertificateStatus.DRAFT) {
            throw new IllegalStateException("Can only delete certificates in DRAFT status");
        }

        certificateRepository.delete(certificate);
    }

    @Transactional(readOnly = true)
    public List<CertificateSummaryDto> getPendingSendCertificates() {
        return certificateRepository.findPendingSend().stream()
                .map(CertificateSummaryDto::from)
                .toList();
    }
}
