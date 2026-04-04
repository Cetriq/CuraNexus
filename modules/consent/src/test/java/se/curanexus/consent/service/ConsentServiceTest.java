package se.curanexus.consent.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.curanexus.consent.api.dto.*;
import se.curanexus.consent.domain.Consent;
import se.curanexus.consent.domain.ConsentStatus;
import se.curanexus.consent.domain.ConsentType;
import se.curanexus.consent.exception.ConsentNotFoundException;
import se.curanexus.consent.repository.ConsentRepository;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsentServiceTest {

    @Mock
    private ConsentRepository consentRepository;

    @InjectMocks
    private ConsentService consentService;

    private UUID patientId;
    private UUID consentId;
    private Consent consent;

    @BeforeEach
    void setUp() throws Exception {
        patientId = UUID.randomUUID();
        consentId = UUID.randomUUID();

        consent = new Consent(patientId, ConsentType.TREATMENT);
        consent.setDescription("Test consent");
        setPrivateField(consent, "id", consentId);
        setPrivateField(consent, "createdAt", Instant.now());
        setPrivateField(consent, "updatedAt", Instant.now());
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void createConsent_ShouldCreateAndReturnConsent() {
        CreateConsentRequest request = new CreateConsentRequest(
                patientId,
                ConsentType.TREATMENT,
                "Treatment consent",
                "Emergency care",
                UUID.randomUUID(),
                "Test Hospital",
                null,
                null,
                null,
                "VERBAL",
                LocalDate.now(),
                LocalDate.now().plusYears(1),
                UUID.randomUUID(),
                "Dr. Smith",
                null
        );

        when(consentRepository.save(any(Consent.class))).thenReturn(consent);

        ConsentDto result = consentService.createConsent(request);

        assertNotNull(result);
        assertEquals(consentId, result.id());
        assertEquals(patientId, result.patientId());
        assertEquals(ConsentType.TREATMENT, result.type());
        verify(consentRepository).save(any(Consent.class));
    }

    @Test
    void getConsent_ShouldReturnConsent_WhenExists() {
        when(consentRepository.findById(consentId)).thenReturn(Optional.of(consent));

        ConsentDto result = consentService.getConsent(consentId);

        assertNotNull(result);
        assertEquals(consentId, result.id());
    }

    @Test
    void getConsent_ShouldThrowException_WhenNotExists() {
        when(consentRepository.findById(consentId)).thenReturn(Optional.empty());

        assertThrows(ConsentNotFoundException.class, () -> consentService.getConsent(consentId));
    }

    @Test
    void getPatientConsents_ShouldReturnList() {
        when(consentRepository.findByPatientId(patientId)).thenReturn(List.of(consent));

        List<ConsentSummaryDto> result = consentService.getPatientConsents(patientId);

        assertEquals(1, result.size());
        assertEquals(consentId, result.get(0).id());
    }

    @Test
    void getPatientConsentsByStatus_ShouldReturnFilteredList() {
        when(consentRepository.findByPatientIdAndStatus(patientId, ConsentStatus.PENDING))
                .thenReturn(List.of(consent));

        List<ConsentSummaryDto> result = consentService.getPatientConsentsByStatus(patientId, ConsentStatus.PENDING);

        assertEquals(1, result.size());
    }

    @Test
    void getActiveConsentsForPatient_ShouldReturnActiveConsents() {
        when(consentRepository.findActiveConsentsForPatient(patientId)).thenReturn(List.of(consent));

        List<ConsentSummaryDto> result = consentService.getActiveConsentsForPatient(patientId);

        assertEquals(1, result.size());
    }

    @Test
    void hasActiveConsent_ShouldReturnTrue_WhenExists() {
        when(consentRepository.findActiveConsentForPatientAndType(patientId, ConsentType.TREATMENT))
                .thenReturn(Optional.of(consent));

        boolean result = consentService.hasActiveConsent(patientId, ConsentType.TREATMENT);

        assertTrue(result);
    }

    @Test
    void hasActiveConsent_ShouldReturnFalse_WhenNotExists() {
        when(consentRepository.findActiveConsentForPatientAndType(patientId, ConsentType.RESEARCH))
                .thenReturn(Optional.empty());

        boolean result = consentService.hasActiveConsent(patientId, ConsentType.RESEARCH);

        assertFalse(result);
    }

    @Test
    void updateConsent_ShouldUpdateFields() {
        UpdateConsentRequest request = new UpdateConsentRequest(
                "Updated description",
                "Updated scope",
                null,
                null,
                null,
                null,
                null
        );

        when(consentRepository.findById(consentId)).thenReturn(Optional.of(consent));
        when(consentRepository.save(any(Consent.class))).thenReturn(consent);

        ConsentDto result = consentService.updateConsent(consentId, request);

        assertNotNull(result);
        verify(consentRepository).save(consent);
    }

    @Test
    void activateConsent_ShouldActivatePendingConsent() throws Exception {
        ActivateConsentRequest request = new ActivateConsentRequest(
                UUID.randomUUID(),
                "Patient Name",
                "VERBAL"
        );

        when(consentRepository.findById(consentId)).thenReturn(Optional.of(consent));
        when(consentRepository.save(any(Consent.class))).thenReturn(consent);

        ConsentDto result = consentService.activateConsent(consentId, request);

        assertNotNull(result);
        verify(consentRepository).save(consent);
    }

    @Test
    void activateConsent_ShouldThrowException_WhenNotPending() throws Exception {
        setPrivateField(consent, "status", ConsentStatus.ACTIVE);

        ActivateConsentRequest request = new ActivateConsentRequest(
                UUID.randomUUID(),
                "Patient Name",
                "VERBAL"
        );

        when(consentRepository.findById(consentId)).thenReturn(Optional.of(consent));

        assertThrows(IllegalStateException.class, () ->
                consentService.activateConsent(consentId, request));
    }

    @Test
    void withdrawConsent_ShouldWithdrawActiveConsent() throws Exception {
        consent.activate();

        WithdrawConsentRequest request = new WithdrawConsentRequest("Patient request");

        when(consentRepository.findById(consentId)).thenReturn(Optional.of(consent));
        when(consentRepository.save(any(Consent.class))).thenReturn(consent);

        ConsentDto result = consentService.withdrawConsent(consentId, request);

        assertNotNull(result);
        verify(consentRepository).save(consent);
    }

    @Test
    void withdrawConsent_ShouldThrowException_WhenNotActive() {
        WithdrawConsentRequest request = new WithdrawConsentRequest("Patient request");

        when(consentRepository.findById(consentId)).thenReturn(Optional.of(consent));

        assertThrows(IllegalStateException.class, () ->
                consentService.withdrawConsent(consentId, request));
    }

    @Test
    void rejectConsent_ShouldRejectPendingConsent() {
        when(consentRepository.findById(consentId)).thenReturn(Optional.of(consent));
        when(consentRepository.save(any(Consent.class))).thenReturn(consent);

        ConsentDto result = consentService.rejectConsent(consentId);

        assertNotNull(result);
        verify(consentRepository).save(consent);
    }

    @Test
    void deleteConsent_ShouldDelete_WhenNotActive() throws Exception {
        setPrivateField(consent, "status", ConsentStatus.WITHDRAWN);

        when(consentRepository.findById(consentId)).thenReturn(Optional.of(consent));

        consentService.deleteConsent(consentId);

        verify(consentRepository).delete(consent);
    }

    @Test
    void deleteConsent_ShouldThrowException_WhenActive() throws Exception {
        setPrivateField(consent, "status", ConsentStatus.ACTIVE);

        when(consentRepository.findById(consentId)).thenReturn(Optional.of(consent));

        assertThrows(IllegalStateException.class, () -> consentService.deleteConsent(consentId));
    }
}
