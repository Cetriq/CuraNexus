package se.curanexus.certificates.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import se.curanexus.certificates.api.dto.*;
import se.curanexus.certificates.domain.*;
import se.curanexus.certificates.repository.CertificateRepository;
import se.curanexus.certificates.repository.CertificateTemplateRepository;

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
class CertificateServiceTest {

    @Mock
    private CertificateRepository certificateRepository;

    @Mock
    private CertificateTemplateRepository templateRepository;

    @InjectMocks
    private CertificateService certificateService;

    private UUID certificateId;
    private UUID patientId;
    private UUID issuerId;
    private UUID templateId;
    private Certificate certificate;
    private CertificateTemplate template;

    @BeforeEach
    void setUp() throws Exception {
        certificateId = UUID.randomUUID();
        patientId = UUID.randomUUID();
        issuerId = UUID.randomUUID();
        templateId = UUID.randomUUID();

        template = new CertificateTemplate("FK_7804", "Läkarintyg för sjukpenning", CertificateType.FK_7804);
        setPrivateField(template, "id", templateId);
        setPrivateField(template, "status", TemplateStatus.ACTIVE);
        setPrivateField(template, "createdAt", Instant.now());
        setPrivateField(template, "updatedAt", Instant.now());

        certificate = new Certificate(template, patientId, issuerId);
        setPrivateField(certificate, "id", certificateId);
        setPrivateField(certificate, "createdAt", Instant.now());
        setPrivateField(certificate, "updatedAt", Instant.now());
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void getCertificate_ShouldReturnCertificate_WhenExists() {
        when(certificateRepository.findById(certificateId)).thenReturn(Optional.of(certificate));

        CertificateDto result = certificateService.getCertificate(certificateId);

        assertNotNull(result);
        assertEquals(certificateId, result.id());
        assertEquals(patientId, result.patientId());
    }

    @Test
    void getCertificate_ShouldThrowException_WhenNotExists() {
        when(certificateRepository.findById(certificateId)).thenReturn(Optional.empty());

        assertThrows(CertificateNotFoundException.class, () ->
                certificateService.getCertificate(certificateId));
    }

    @Test
    void getCertificateByNumber_ShouldReturnCertificate() {
        String certNumber = certificate.getCertificateNumber();
        when(certificateRepository.findByCertificateNumber(certNumber))
                .thenReturn(Optional.of(certificate));

        CertificateDto result = certificateService.getCertificateByNumber(certNumber);

        assertNotNull(result);
        assertEquals(certNumber, result.certificateNumber());
    }

    @Test
    void getPatientCertificates_ShouldReturnList() {
        when(certificateRepository.findByPatientId(patientId))
                .thenReturn(List.of(certificate));

        List<CertificateSummaryDto> result = certificateService.getPatientCertificates(patientId, null);

        assertEquals(1, result.size());
        assertEquals(certificateId, result.get(0).id());
    }

    @Test
    void getPatientCertificates_WithStatus_ShouldReturnFilteredList() {
        when(certificateRepository.findByPatientIdAndStatus(patientId, CertificateStatus.DRAFT))
                .thenReturn(List.of(certificate));

        List<CertificateSummaryDto> result = certificateService.getPatientCertificates(patientId, CertificateStatus.DRAFT);

        assertEquals(1, result.size());
    }

    @Test
    void getEncounterCertificates_ShouldReturnList() {
        UUID encounterId = UUID.randomUUID();
        when(certificateRepository.findByEncounterId(encounterId))
                .thenReturn(List.of(certificate));

        List<CertificateSummaryDto> result = certificateService.getEncounterCertificates(encounterId);

        assertEquals(1, result.size());
    }

    @Test
    void getIssuerCertificates_ShouldReturnPage() {
        Page<Certificate> page = new PageImpl<>(List.of(certificate));
        when(certificateRepository.findByIssuerId(eq(issuerId), any(Pageable.class)))
                .thenReturn(page);

        Page<CertificateSummaryDto> result = certificateService.getIssuerCertificates(issuerId, Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getDraftCertificates_ShouldReturnDrafts() {
        when(certificateRepository.findDraftsByIssuer(issuerId))
                .thenReturn(List.of(certificate));

        List<CertificateSummaryDto> result = certificateService.getDraftCertificates(issuerId);

        assertEquals(1, result.size());
    }

    @Test
    void createCertificate_ShouldCreateAndReturn() {
        CreateCertificateRequest request = new CreateCertificateRequest(
                "FK_7804",
                patientId,
                UUID.randomUUID(),
                issuerId,
                "Dr. Smith",
                "DOCTOR",
                UUID.randomUUID(),
                "Akuten",
                "{}",
                LocalDate.now(),
                LocalDate.now().plusWeeks(2),
                "J06.9",
                "Akut övre luftvägsinfektion",
                null
        );

        when(templateRepository.findActiveByCode("FK_7804")).thenReturn(Optional.of(template));
        when(certificateRepository.save(any(Certificate.class))).thenReturn(certificate);

        CertificateDto result = certificateService.createCertificate(request);

        assertNotNull(result);
        verify(certificateRepository).save(any(Certificate.class));
    }

    @Test
    void createCertificate_ShouldThrowException_WhenTemplateNotFound() {
        CreateCertificateRequest request = new CreateCertificateRequest(
                "INVALID",
                patientId,
                null,
                issuerId,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        when(templateRepository.findActiveByCode("INVALID")).thenReturn(Optional.empty());

        assertThrows(CertificateTemplateNotFoundException.class, () ->
                certificateService.createCertificate(request));
    }

    @Test
    void updateCertificate_ShouldUpdate_WhenDraft() {
        UpdateCertificateRequest request = new UpdateCertificateRequest(
                "{\"updated\": true}",
                LocalDate.now(),
                LocalDate.now().plusWeeks(3),
                "J06.9, J20.9",
                "Updated diagnosis"
        );

        when(certificateRepository.findById(certificateId)).thenReturn(Optional.of(certificate));
        when(certificateRepository.save(any(Certificate.class))).thenReturn(certificate);

        CertificateDto result = certificateService.updateCertificate(certificateId, request);

        assertNotNull(result);
        verify(certificateRepository).save(certificate);
    }

    @Test
    void updateCertificate_ShouldThrowException_WhenNotDraft() throws Exception {
        setPrivateField(certificate, "status", CertificateStatus.SIGNED);

        UpdateCertificateRequest request = new UpdateCertificateRequest(
                "{}", null, null, null, null
        );

        when(certificateRepository.findById(certificateId)).thenReturn(Optional.of(certificate));

        assertThrows(IllegalStateException.class, () ->
                certificateService.updateCertificate(certificateId, request));
    }

    @Test
    void signCertificate_ShouldSign_WhenDraft() {
        SignCertificateRequest request = new SignCertificateRequest("BANKID-SIGNATURE-DATA");

        when(certificateRepository.findById(certificateId)).thenReturn(Optional.of(certificate));
        when(certificateRepository.save(any(Certificate.class))).thenReturn(certificate);

        CertificateDto result = certificateService.signCertificate(certificateId, request);

        assertNotNull(result);
        verify(certificateRepository).save(certificate);
    }

    @Test
    void signCertificate_ShouldThrowException_WhenNotDraft() throws Exception {
        setPrivateField(certificate, "status", CertificateStatus.SENT);

        SignCertificateRequest request = new SignCertificateRequest("SIGNATURE");

        when(certificateRepository.findById(certificateId)).thenReturn(Optional.of(certificate));

        assertThrows(IllegalStateException.class, () ->
                certificateService.signCertificate(certificateId, request));
    }

    @Test
    void sendCertificate_ShouldSend_WhenSigned() throws Exception {
        setPrivateField(certificate, "status", CertificateStatus.SIGNED);

        when(certificateRepository.findById(certificateId)).thenReturn(Optional.of(certificate));
        when(certificateRepository.save(any(Certificate.class))).thenReturn(certificate);

        CertificateDto result = certificateService.sendCertificate(certificateId);

        assertNotNull(result);
        verify(certificateRepository).save(certificate);
    }

    @Test
    void sendCertificate_ShouldThrowException_WhenNotSigned() {
        when(certificateRepository.findById(certificateId)).thenReturn(Optional.of(certificate));

        assertThrows(IllegalStateException.class, () ->
                certificateService.sendCertificate(certificateId));
    }

    @Test
    void revokeCertificate_ShouldRevoke_WhenSignedOrSent() throws Exception {
        setPrivateField(certificate, "status", CertificateStatus.SENT);

        RevokeCertificateRequest request = new RevokeCertificateRequest("Felaktigt utfärdat");

        when(certificateRepository.findById(certificateId)).thenReturn(Optional.of(certificate));
        when(certificateRepository.save(any(Certificate.class))).thenReturn(certificate);

        CertificateDto result = certificateService.revokeCertificate(certificateId, request);

        assertNotNull(result);
        verify(certificateRepository).save(certificate);
    }

    @Test
    void revokeCertificate_ShouldThrowException_WhenDraft() {
        RevokeCertificateRequest request = new RevokeCertificateRequest("Reason");

        when(certificateRepository.findById(certificateId)).thenReturn(Optional.of(certificate));

        assertThrows(IllegalStateException.class, () ->
                certificateService.revokeCertificate(certificateId, request));
    }

    @Test
    void deleteCertificate_ShouldDelete_WhenDraft() {
        when(certificateRepository.findById(certificateId)).thenReturn(Optional.of(certificate));

        certificateService.deleteCertificate(certificateId);

        verify(certificateRepository).delete(certificate);
    }

    @Test
    void deleteCertificate_ShouldThrowException_WhenNotDraft() throws Exception {
        setPrivateField(certificate, "status", CertificateStatus.SIGNED);

        when(certificateRepository.findById(certificateId)).thenReturn(Optional.of(certificate));

        assertThrows(IllegalStateException.class, () ->
                certificateService.deleteCertificate(certificateId));
    }

    @Test
    void getPendingSendCertificates_ShouldReturnList() {
        when(certificateRepository.findPendingSend()).thenReturn(List.of(certificate));

        List<CertificateSummaryDto> result = certificateService.getPendingSendCertificates();

        assertEquals(1, result.size());
    }
}
