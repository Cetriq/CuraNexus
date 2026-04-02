package se.curanexus.patient.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import se.curanexus.patient.api.dto.*;
import se.curanexus.patient.domain.*;
import se.curanexus.patient.repository.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private ContactInfoRepository contactInfoRepository;

    @Mock
    private RelatedPersonRepository relatedPersonRepository;

    @Mock
    private ConsentRepository consentRepository;

    private PatientService patientService;

    @BeforeEach
    void setUp() {
        patientService = new PatientService(
                patientRepository,
                contactInfoRepository,
                relatedPersonRepository,
                consentRepository
        );
    }

    @Test
    void createPatient_shouldCreateNewPatient() {
        // Given - personnummer ending in 8 (even) = FEMALE
        CreatePatientRequest request = new CreatePatientRequest(
                "199001012384",
                "Anna",
                "Andersson",
                null,
                false
        );

        when(patientRepository.existsByPersonalIdentityNumber("199001012384")).thenReturn(false);
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> {
            Patient p = invocation.getArgument(0);
            return p;
        });

        // When
        PatientDto result = patientService.createPatient(request);

        // Then
        assertThat(result.personalIdentityNumber()).isEqualTo("199001012384");
        assertThat(result.givenName()).isEqualTo("Anna");
        assertThat(result.familyName()).isEqualTo("Andersson");
        assertThat(result.gender()).isEqualTo(Gender.FEMALE);
        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    void createPatient_shouldThrowWhenPatientExists() {
        // Given
        CreatePatientRequest request = new CreatePatientRequest(
                "199001011234",
                "Anna",
                "Andersson",
                null,
                false
        );

        when(patientRepository.existsByPersonalIdentityNumber("199001011234")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> patientService.createPatient(request))
                .isInstanceOf(PatientAlreadyExistsException.class);
    }

    @Test
    void getPatient_shouldReturnPatient() {
        // Given
        UUID patientId = UUID.randomUUID();
        Patient patient = new Patient("199001011234", "Anna", "Andersson");

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));

        // When
        PatientDto result = patientService.getPatient(patientId);

        // Then
        assertThat(result.givenName()).isEqualTo("Anna");
        assertThat(result.familyName()).isEqualTo("Andersson");
    }

    @Test
    void getPatient_shouldThrowWhenNotFound() {
        // Given
        UUID patientId = UUID.randomUUID();
        when(patientRepository.findById(patientId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> patientService.getPatient(patientId))
                .isInstanceOf(PatientNotFoundException.class);
    }

    @Test
    void updatePatient_shouldUpdateFields() {
        // Given
        UUID patientId = UUID.randomUUID();
        Patient patient = new Patient("199001011234", "Anna", "Andersson");
        UpdatePatientRequest request = new UpdatePatientRequest(
                "Anna-Maria",
                "Svensson",
                null,
                true,
                null,
                null
        );

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        PatientDto result = patientService.updatePatient(patientId, request);

        // Then
        assertThat(result.givenName()).isEqualTo("Anna-Maria");
        assertThat(result.familyName()).isEqualTo("Svensson");
        assertThat(result.protectedIdentity()).isTrue();
    }

    @Test
    void searchPatients_shouldReturnPagedResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Patient patient = new Patient("199001011234", "Anna", "Andersson");
        Page<Patient> patientPage = new PageImpl<>(List.of(patient), pageable, 1);

        when(patientRepository.searchPatients(null, "Anna", pageable)).thenReturn(patientPage);

        // When
        Page<PatientSummaryDto> result = patientService.searchPatients(null, "Anna", pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).givenName()).isEqualTo("Anna");
    }

    @Test
    void addPatientContact_shouldAddContact() {
        // Given
        UUID patientId = UUID.randomUUID();
        Patient patient = new Patient("199001011234", "Anna", "Andersson");
        CreateContactRequest request = new CreateContactRequest(
                ContactType.EMAIL,
                "anna@example.com",
                ContactUse.HOME,
                true,
                null,
                null
        );

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ContactInfoDto result = patientService.addPatientContact(patientId, request);

        // Then
        assertThat(result.type()).isEqualTo(ContactType.EMAIL);
        assertThat(result.value()).isEqualTo("anna@example.com");
        assertThat(result.primary()).isTrue();
    }

    @Test
    void addRelatedPerson_shouldAddRelatedPerson() {
        // Given
        UUID patientId = UUID.randomUUID();
        Patient patient = new Patient("199001011234", "Anna", "Andersson");
        CreateRelatedPersonRequest request = new CreateRelatedPersonRequest(
                RelationshipType.SPOUSE,
                null,
                "Erik",
                "Andersson",
                "0701234567",
                "erik@example.com",
                true,
                false,
                null,
                null
        );

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        RelatedPersonDto result = patientService.addRelatedPerson(patientId, request);

        // Then
        assertThat(result.relationship()).isEqualTo(RelationshipType.SPOUSE);
        assertThat(result.givenName()).isEqualTo("Erik");
        assertThat(result.isEmergencyContact()).isTrue();
    }

    @Test
    void registerConsent_shouldRegisterConsent() {
        // Given
        UUID patientId = UUID.randomUUID();
        Patient patient = new Patient("199001011234", "Anna", "Andersson");
        CreateConsentRequest request = new CreateConsentRequest(
                ConsentType.DATA_SHARING,
                "patient",
                null,
                null,
                "Share with primary care"
        );

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ConsentDto result = patientService.registerConsent(patientId, request);

        // Then
        assertThat(result.type()).isEqualTo(ConsentType.DATA_SHARING);
        assertThat(result.status()).isEqualTo(ConsentStatus.ACTIVE);
        assertThat(result.scope()).isEqualTo("Share with primary care");
    }

    @Test
    void revokeConsent_shouldRevokeConsent() {
        // Given
        UUID patientId = UUID.randomUUID();
        UUID consentId = UUID.randomUUID();
        Patient patient = new Patient("199001011234", "Anna", "Andersson");
        Consent consent = new Consent(ConsentType.DATA_SHARING);

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(consentRepository.findById(consentId)).thenReturn(Optional.of(consent));
        when(consentRepository.save(any(Consent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        patientService.revokeConsent(patientId, consentId);

        // Then
        verify(consentRepository).save(argThat(c -> c.getStatus() == ConsentStatus.REVOKED));
    }
}
