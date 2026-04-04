package se.curanexus.consent.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.curanexus.consent.api.dto.*;
import se.curanexus.consent.domain.Consent;
import se.curanexus.consent.domain.ConsentStatus;
import se.curanexus.consent.domain.ConsentType;
import se.curanexus.consent.exception.ConsentNotFoundException;
import se.curanexus.consent.repository.ConsentRepository;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ConsentService {

    private final ConsentRepository consentRepository;

    public ConsentService(ConsentRepository consentRepository) {
        this.consentRepository = consentRepository;
    }

    public ConsentDto createConsent(CreateConsentRequest request) {
        Consent consent = new Consent(request.patientId(), request.type());
        consent.setDescription(request.description());
        consent.setScope(request.scope());
        consent.setManagingUnitId(request.managingUnitId());
        consent.setManagingUnitName(request.managingUnitName());
        consent.setGivenBy(request.givenBy());
        consent.setGivenByName(request.givenByName());
        consent.setRepresentativeRelation(request.representativeRelation());
        consent.setCollectionMethod(request.collectionMethod());
        consent.setValidFrom(request.validFrom());
        consent.setValidUntil(request.validUntil());
        consent.setRecordedBy(request.recordedBy());
        consent.setRecordedByName(request.recordedByName());
        consent.setDocumentReference(request.documentReference());

        Consent saved = consentRepository.save(consent);
        return ConsentDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public ConsentDto getConsent(UUID id) {
        Consent consent = consentRepository.findById(id)
                .orElseThrow(() -> new ConsentNotFoundException(id));
        return ConsentDto.fromEntity(consent);
    }

    @Transactional(readOnly = true)
    public List<ConsentSummaryDto> getPatientConsents(UUID patientId) {
        return consentRepository.findByPatientId(patientId).stream()
                .map(ConsentSummaryDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ConsentSummaryDto> getPatientConsentsByStatus(UUID patientId, ConsentStatus status) {
        return consentRepository.findByPatientIdAndStatus(patientId, status).stream()
                .map(ConsentSummaryDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ConsentSummaryDto> getPatientConsentsByType(UUID patientId, ConsentType type) {
        return consentRepository.findByPatientIdAndType(patientId, type).stream()
                .map(ConsentSummaryDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ConsentSummaryDto> getActiveConsentsForPatient(UUID patientId) {
        return consentRepository.findActiveConsentsForPatient(patientId).stream()
                .map(ConsentSummaryDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean hasActiveConsent(UUID patientId, ConsentType type) {
        return consentRepository.findActiveConsentForPatientAndType(patientId, type).isPresent();
    }

    @Transactional(readOnly = true)
    public List<ConsentSummaryDto> getConsentsForUnit(UUID managingUnitId) {
        return consentRepository.findByManagingUnitId(managingUnitId).stream()
                .map(ConsentSummaryDto::fromEntity)
                .toList();
    }

    public ConsentDto updateConsent(UUID id, UpdateConsentRequest request) {
        Consent consent = consentRepository.findById(id)
                .orElseThrow(() -> new ConsentNotFoundException(id));

        if (request.description() != null) {
            consent.setDescription(request.description());
        }
        if (request.scope() != null) {
            consent.setScope(request.scope());
        }
        if (request.managingUnitId() != null) {
            consent.setManagingUnitId(request.managingUnitId());
        }
        if (request.managingUnitName() != null) {
            consent.setManagingUnitName(request.managingUnitName());
        }
        if (request.validFrom() != null) {
            consent.setValidFrom(request.validFrom());
        }
        if (request.validUntil() != null) {
            consent.setValidUntil(request.validUntil());
        }
        if (request.documentReference() != null) {
            consent.setDocumentReference(request.documentReference());
        }

        Consent saved = consentRepository.save(consent);
        return ConsentDto.fromEntity(saved);
    }

    public ConsentDto activateConsent(UUID id, ActivateConsentRequest request) {
        Consent consent = consentRepository.findById(id)
                .orElseThrow(() -> new ConsentNotFoundException(id));

        if (consent.getStatus() != ConsentStatus.PENDING) {
            throw new IllegalStateException("Only pending consents can be activated");
        }

        consent.setGivenBy(request.givenBy());
        consent.setGivenByName(request.givenByName());
        consent.setCollectionMethod(request.collectionMethod());
        consent.activate();

        Consent saved = consentRepository.save(consent);
        return ConsentDto.fromEntity(saved);
    }

    public ConsentDto withdrawConsent(UUID id, WithdrawConsentRequest request) {
        Consent consent = consentRepository.findById(id)
                .orElseThrow(() -> new ConsentNotFoundException(id));

        if (consent.getStatus() != ConsentStatus.ACTIVE) {
            throw new IllegalStateException("Only active consents can be withdrawn");
        }

        consent.withdraw(request.reason());

        Consent saved = consentRepository.save(consent);
        return ConsentDto.fromEntity(saved);
    }

    public ConsentDto rejectConsent(UUID id) {
        Consent consent = consentRepository.findById(id)
                .orElseThrow(() -> new ConsentNotFoundException(id));

        if (consent.getStatus() != ConsentStatus.PENDING) {
            throw new IllegalStateException("Only pending consents can be rejected");
        }

        consent.reject();

        Consent saved = consentRepository.save(consent);
        return ConsentDto.fromEntity(saved);
    }

    public void expireConsents() {
        List<Consent> expiredConsents = consentRepository.findExpiredConsents();
        for (Consent consent : expiredConsents) {
            consent.expire();
            consentRepository.save(consent);
        }
    }

    public void deleteConsent(UUID id) {
        Consent consent = consentRepository.findById(id)
                .orElseThrow(() -> new ConsentNotFoundException(id));

        if (consent.getStatus() == ConsentStatus.ACTIVE) {
            throw new IllegalStateException("Cannot delete active consent. Withdraw it first.");
        }

        consentRepository.delete(consent);
    }
}
