package se.curanexus.patient.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.curanexus.patient.api.dto.*;
import se.curanexus.patient.domain.*;
import se.curanexus.patient.repository.*;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PatientService {

    private final PatientRepository patientRepository;
    private final ContactInfoRepository contactInfoRepository;
    private final RelatedPersonRepository relatedPersonRepository;
    private final ConsentRepository consentRepository;

    public PatientService(PatientRepository patientRepository,
                          ContactInfoRepository contactInfoRepository,
                          RelatedPersonRepository relatedPersonRepository,
                          ConsentRepository consentRepository) {
        this.patientRepository = patientRepository;
        this.contactInfoRepository = contactInfoRepository;
        this.relatedPersonRepository = relatedPersonRepository;
        this.consentRepository = consentRepository;
    }

    // Patient operations

    @Transactional(readOnly = true)
    public Page<PatientSummaryDto> searchPatients(String personalIdentityNumber, String name, Pageable pageable) {
        return patientRepository.searchPatients(personalIdentityNumber, name, pageable)
                .map(PatientSummaryDto::from);
    }

    @Transactional(readOnly = true)
    public PatientDto getPatient(UUID patientId) {
        Patient patient = findPatientOrThrow(patientId);
        return PatientDto.from(patient);
    }

    public PatientDto createPatient(CreatePatientRequest request) {
        if (patientRepository.existsByPersonalIdentityNumber(request.personalIdentityNumber())) {
            throw new PatientAlreadyExistsException(request.personalIdentityNumber());
        }

        Patient patient = new Patient(
                request.personalIdentityNumber(),
                request.givenName(),
                request.familyName()
        );
        patient.setMiddleName(request.middleName());
        if (request.protectedIdentity() != null) {
            patient.setProtectedIdentity(request.protectedIdentity());
        }

        Patient saved = patientRepository.save(patient);
        return PatientDto.from(saved);
    }

    public PatientDto updatePatient(UUID patientId, UpdatePatientRequest request) {
        Patient patient = findPatientOrThrow(patientId);

        if (request.givenName() != null) {
            patient.setGivenName(request.givenName());
        }
        if (request.familyName() != null) {
            patient.setFamilyName(request.familyName());
        }
        if (request.middleName() != null) {
            patient.setMiddleName(request.middleName());
        }
        if (request.protectedIdentity() != null) {
            patient.setProtectedIdentity(request.protectedIdentity());
        }
        if (request.deceased() != null) {
            patient.setDeceased(request.deceased());
        }
        if (request.deceasedDate() != null) {
            patient.setDeceasedDate(request.deceasedDate());
        }

        Patient saved = patientRepository.save(patient);
        return PatientDto.from(saved);
    }

    // Contact operations

    @Transactional(readOnly = true)
    public List<ContactInfoDto> getPatientContacts(UUID patientId) {
        findPatientOrThrow(patientId);
        return contactInfoRepository.findByPatientId(patientId).stream()
                .map(ContactInfoDto::from)
                .toList();
    }

    public ContactInfoDto addPatientContact(UUID patientId, CreateContactRequest request) {
        Patient patient = findPatientOrThrow(patientId);

        ContactInfo contact = new ContactInfo(request.type(), request.value());
        contact.setUse(request.use());
        if (request.primary() != null) {
            contact.setPrimary(request.primary());
        }
        contact.setValidFrom(request.validFrom());
        contact.setValidTo(request.validTo());

        patient.addContact(contact);
        patientRepository.save(patient);

        return ContactInfoDto.from(contact);
    }

    public ContactInfoDto updatePatientContact(UUID patientId, UUID contactId, UpdateContactRequest request) {
        findPatientOrThrow(patientId);
        ContactInfo contact = contactInfoRepository.findById(contactId)
                .orElseThrow(() -> new ResourceNotFoundException("Contact", contactId));

        if (request.value() != null) {
            contact.setValue(request.value());
        }
        if (request.use() != null) {
            contact.setUse(request.use());
        }
        if (request.primary() != null) {
            contact.setPrimary(request.primary());
        }
        if (request.validFrom() != null) {
            contact.setValidFrom(request.validFrom());
        }
        if (request.validTo() != null) {
            contact.setValidTo(request.validTo());
        }

        ContactInfo saved = contactInfoRepository.save(contact);
        return ContactInfoDto.from(saved);
    }

    public void deletePatientContact(UUID patientId, UUID contactId) {
        Patient patient = findPatientOrThrow(patientId);
        ContactInfo contact = contactInfoRepository.findById(contactId)
                .orElseThrow(() -> new ResourceNotFoundException("Contact", contactId));

        patient.removeContact(contact);
        patientRepository.save(patient);
    }

    // Related person operations

    @Transactional(readOnly = true)
    public List<RelatedPersonDto> getRelatedPersons(UUID patientId) {
        findPatientOrThrow(patientId);
        return relatedPersonRepository.findByPatientId(patientId).stream()
                .map(RelatedPersonDto::from)
                .toList();
    }

    public RelatedPersonDto addRelatedPerson(UUID patientId, CreateRelatedPersonRequest request) {
        Patient patient = findPatientOrThrow(patientId);

        RelatedPerson relatedPerson = new RelatedPerson(
                request.relationship(),
                request.givenName(),
                request.familyName()
        );
        relatedPerson.setPersonalIdentityNumber(request.personalIdentityNumber());
        relatedPerson.setPhone(request.phone());
        relatedPerson.setEmail(request.email());
        if (request.isEmergencyContact() != null) {
            relatedPerson.setEmergencyContact(request.isEmergencyContact());
        }
        if (request.isLegalGuardian() != null) {
            relatedPerson.setLegalGuardian(request.isLegalGuardian());
        }
        relatedPerson.setValidFrom(request.validFrom());
        relatedPerson.setValidTo(request.validTo());

        patient.addRelatedPerson(relatedPerson);
        patientRepository.save(patient);

        return RelatedPersonDto.from(relatedPerson);
    }

    public RelatedPersonDto updateRelatedPerson(UUID patientId, UUID relatedPersonId, UpdateRelatedPersonRequest request) {
        findPatientOrThrow(patientId);
        RelatedPerson relatedPerson = relatedPersonRepository.findById(relatedPersonId)
                .orElseThrow(() -> new ResourceNotFoundException("RelatedPerson", relatedPersonId));

        if (request.relationship() != null) {
            relatedPerson.setRelationship(request.relationship());
        }
        if (request.givenName() != null) {
            relatedPerson.setGivenName(request.givenName());
        }
        if (request.familyName() != null) {
            relatedPerson.setFamilyName(request.familyName());
        }
        if (request.phone() != null) {
            relatedPerson.setPhone(request.phone());
        }
        if (request.email() != null) {
            relatedPerson.setEmail(request.email());
        }
        if (request.isEmergencyContact() != null) {
            relatedPerson.setEmergencyContact(request.isEmergencyContact());
        }
        if (request.isLegalGuardian() != null) {
            relatedPerson.setLegalGuardian(request.isLegalGuardian());
        }
        if (request.validFrom() != null) {
            relatedPerson.setValidFrom(request.validFrom());
        }
        if (request.validTo() != null) {
            relatedPerson.setValidTo(request.validTo());
        }

        RelatedPerson saved = relatedPersonRepository.save(relatedPerson);
        return RelatedPersonDto.from(saved);
    }

    public void deleteRelatedPerson(UUID patientId, UUID relatedPersonId) {
        Patient patient = findPatientOrThrow(patientId);
        RelatedPerson relatedPerson = relatedPersonRepository.findById(relatedPersonId)
                .orElseThrow(() -> new ResourceNotFoundException("RelatedPerson", relatedPersonId));

        patient.removeRelatedPerson(relatedPerson);
        patientRepository.save(patient);
    }

    // Consent operations

    @Transactional(readOnly = true)
    public List<ConsentDto> getPatientConsents(UUID patientId) {
        findPatientOrThrow(patientId);
        return consentRepository.findByPatientId(patientId).stream()
                .map(ConsentDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ConsentDto getConsent(UUID patientId, UUID consentId) {
        findPatientOrThrow(patientId);
        Consent consent = consentRepository.findById(consentId)
                .orElseThrow(() -> new ResourceNotFoundException("Consent", consentId));
        return ConsentDto.from(consent);
    }

    public ConsentDto registerConsent(UUID patientId, CreateConsentRequest request) {
        Patient patient = findPatientOrThrow(patientId);

        Consent consent = new Consent(request.type());
        consent.setGivenBy(request.givenBy());
        consent.setValidFrom(request.validFrom());
        consent.setValidTo(request.validTo());
        consent.setScope(request.scope());

        patient.addConsent(consent);
        patientRepository.save(patient);

        return ConsentDto.from(consent);
    }

    public void revokeConsent(UUID patientId, UUID consentId) {
        findPatientOrThrow(patientId);
        Consent consent = consentRepository.findById(consentId)
                .orElseThrow(() -> new ResourceNotFoundException("Consent", consentId));

        consent.revoke();
        consentRepository.save(consent);
    }

    private Patient findPatientOrThrow(UUID patientId) {
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException(patientId));
    }
}
