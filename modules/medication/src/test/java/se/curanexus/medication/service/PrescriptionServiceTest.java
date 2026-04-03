package se.curanexus.medication.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.curanexus.events.DomainEventPublisher;
import se.curanexus.medication.api.dto.CreatePrescriptionRequest;
import se.curanexus.medication.api.dto.PrescriptionDto;
import se.curanexus.medication.domain.Prescription;
import se.curanexus.medication.domain.PrescriptionStatus;
import se.curanexus.medication.domain.RouteOfAdministration;
import se.curanexus.medication.repository.MedicationRepository;
import se.curanexus.medication.repository.PrescriptionRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PrescriptionService")
class PrescriptionServiceTest {

    @Mock
    private PrescriptionRepository prescriptionRepository;

    @Mock
    private MedicationRepository medicationRepository;

    @Mock
    private DomainEventPublisher eventPublisher;

    private PrescriptionService prescriptionService;

    private UUID patientId;
    private UUID prescriberId;

    @BeforeEach
    void setUp() {
        prescriptionService = new PrescriptionService(
                prescriptionRepository,
                medicationRepository,
                eventPublisher
        );
        patientId = UUID.randomUUID();
        prescriberId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("Skapa ordination")
    class CreatePrescription {

        @Test
        @DisplayName("Ska skapa ordination med fritextläkemedel")
        void shouldCreateWithMedicationText() {
            CreatePrescriptionRequest request = new CreatePrescriptionRequest(
                    patientId,
                    null, // encounterId
                    null, // medicationId
                    "Paracetamol 500 mg",
                    "N02BE01",
                    "Smärtlindring",
                    RouteOfAdministration.ORAL,
                    "1 tablett vid behov, max 4 gånger dagligen",
                    new BigDecimal("500"),
                    "mg",
                    4, // frequency
                    24, // frequencyPeriodHours
                    true, // asNeeded
                    new BigDecimal("2000"), // maxDosePerDay
                    LocalDate.now(),
                    null,
                    null,
                    null,
                    null,
                    false,
                    null,
                    "SE1234567890",
                    "Dr. Anna Läkare",
                    "LA123",
                    null,
                    null,
                    null,
                    null,
                    false
            );

            when(prescriptionRepository.save(any(Prescription.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            PrescriptionDto result = prescriptionService.createPrescription(request, prescriberId);

            assertNotNull(result);
            assertEquals(patientId, result.patientId());
            assertEquals("Paracetamol 500 mg", result.medicationText());
            assertEquals("N02BE01", result.atcCode());
            assertEquals(PrescriptionStatus.DRAFT, result.status());
            assertTrue(result.asNeeded());

            verify(prescriptionRepository).save(any(Prescription.class));
            verify(eventPublisher).publish(any(PrescriptionService.PrescriptionCreatedEvent.class));
        }

        @Test
        @DisplayName("Ska aktivera ordination direkt om begärt")
        void shouldActivateImmediately() {
            CreatePrescriptionRequest request = new CreatePrescriptionRequest(
                    patientId, null, null, "Test", "N02", null, null, null,
                    null, null, null, null, false, null, null, null, null,
                    null, null, false, null, null, null, null, null, null,
                    null, null, true // activateImmediately
            );

            when(prescriptionRepository.save(any(Prescription.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            PrescriptionDto result = prescriptionService.createPrescription(request, prescriberId);

            assertEquals(PrescriptionStatus.ACTIVE, result.status());
            assertNotNull(result.activatedAt());
        }
    }

    @Nested
    @DisplayName("Hämta ordinationer")
    class GetPrescriptions {

        @Test
        @DisplayName("Ska hämta ordination via ID")
        void shouldGetById() {
            UUID prescriptionId = UUID.randomUUID();
            Prescription prescription = new Prescription(patientId, prescriberId);
            prescription.setMedicationText("Test");

            when(prescriptionRepository.findById(prescriptionId))
                    .thenReturn(Optional.of(prescription));

            PrescriptionDto result = prescriptionService.getPrescription(prescriptionId);

            assertEquals(patientId, result.patientId());
        }

        @Test
        @DisplayName("Ska kasta exception om ordination ej hittas")
        void shouldThrowIfNotFound() {
            UUID prescriptionId = UUID.randomUUID();
            when(prescriptionRepository.findById(prescriptionId))
                    .thenReturn(Optional.empty());

            assertThrows(PrescriptionNotFoundException.class, () ->
                    prescriptionService.getPrescription(prescriptionId));
        }

        @Test
        @DisplayName("Ska hämta patients ordinationer")
        void shouldGetPatientPrescriptions() {
            Prescription p1 = new Prescription(patientId, prescriberId);
            p1.setMedicationText("Med 1");
            Prescription p2 = new Prescription(patientId, prescriberId);
            p2.setMedicationText("Med 2");

            when(prescriptionRepository.findByPatientIdOrderByCreatedAtDesc(patientId))
                    .thenReturn(List.of(p1, p2));

            List<PrescriptionDto> result = prescriptionService.getPatientPrescriptions(patientId);

            assertEquals(2, result.size());
        }
    }

    @Nested
    @DisplayName("Statusändring")
    class StatusChange {

        @Test
        @DisplayName("Ska aktivera ordination")
        void shouldActivate() {
            UUID prescriptionId = UUID.randomUUID();
            Prescription prescription = new Prescription(patientId, prescriberId);
            prescription.setMedicationText("Test");

            when(prescriptionRepository.findById(prescriptionId))
                    .thenReturn(Optional.of(prescription));
            when(prescriptionRepository.save(any(Prescription.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            PrescriptionDto result = prescriptionService.activatePrescription(prescriptionId);

            assertEquals(PrescriptionStatus.ACTIVE, result.status());
            verify(eventPublisher).publish(any(PrescriptionService.PrescriptionActivatedEvent.class));
        }

        @Test
        @DisplayName("Ska pausa ordination")
        void shouldPutOnHold() {
            UUID prescriptionId = UUID.randomUUID();
            Prescription prescription = new Prescription(patientId, prescriberId);
            prescription.setMedicationText("Test");
            prescription.activate();

            when(prescriptionRepository.findById(prescriptionId))
                    .thenReturn(Optional.of(prescription));
            when(prescriptionRepository.save(any(Prescription.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            PrescriptionDto result = prescriptionService.putOnHold(prescriptionId, "Biverkning");

            assertEquals(PrescriptionStatus.ON_HOLD, result.status());
            verify(eventPublisher).publish(any(PrescriptionService.PrescriptionPutOnHoldEvent.class));
        }

        @Test
        @DisplayName("Ska avsluta ordination")
        void shouldComplete() {
            UUID prescriptionId = UUID.randomUUID();
            Prescription prescription = new Prescription(patientId, prescriberId);
            prescription.setMedicationText("Test");
            prescription.activate();

            when(prescriptionRepository.findById(prescriptionId))
                    .thenReturn(Optional.of(prescription));
            when(prescriptionRepository.save(any(Prescription.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            PrescriptionDto result = prescriptionService.completePrescription(prescriptionId);

            assertEquals(PrescriptionStatus.COMPLETED, result.status());
            verify(eventPublisher).publish(any(PrescriptionService.PrescriptionCompletedEvent.class));
        }

        @Test
        @DisplayName("Ska avbryta ordination")
        void shouldCancel() {
            UUID prescriptionId = UUID.randomUUID();
            Prescription prescription = new Prescription(patientId, prescriberId);
            prescription.setMedicationText("Test");
            prescription.activate();

            when(prescriptionRepository.findById(prescriptionId))
                    .thenReturn(Optional.of(prescription));
            when(prescriptionRepository.save(any(Prescription.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            PrescriptionDto result = prescriptionService.cancelPrescription(prescriptionId, "Bytt preparat");

            assertEquals(PrescriptionStatus.CANCELLED, result.status());
            assertEquals("Bytt preparat", result.discontinuationReason());

            ArgumentCaptor<PrescriptionService.PrescriptionCancelledEvent> eventCaptor =
                    ArgumentCaptor.forClass(PrescriptionService.PrescriptionCancelledEvent.class);
            verify(eventPublisher).publish(eventCaptor.capture());
            assertEquals("Bytt preparat", eventCaptor.getValue().getReason());
        }
    }
}
