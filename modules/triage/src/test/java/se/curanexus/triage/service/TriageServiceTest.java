package se.curanexus.triage.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.curanexus.triage.domain.*;
import se.curanexus.triage.repository.*;
import se.curanexus.triage.service.exception.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TriageService")
class TriageServiceTest {

    @Mock
    private TriageAssessmentRepository assessmentRepository;

    @Mock
    private SymptomRepository symptomRepository;

    @Mock
    private VitalSignsRepository vitalSignsRepository;

    @Mock
    private TriageProtocolRepository protocolRepository;

    @InjectMocks
    private TriageService triageService;

    private UUID patientId;
    private UUID encounterId;
    private UUID nurseId;
    private UUID locationId;

    @BeforeEach
    void setUp() {
        patientId = UUID.randomUUID();
        encounterId = UUID.randomUUID();
        nurseId = UUID.randomUUID();
        locationId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("createAssessment")
    class CreateAssessment {

        @Test
        @DisplayName("should create assessment successfully")
        void shouldCreateAssessmentSuccessfully() {
            when(assessmentRepository.findByEncounterId(encounterId)).thenReturn(Optional.empty());
            when(assessmentRepository.save(any(TriageAssessment.class))).thenAnswer(inv -> inv.getArgument(0));

            TriageAssessment result = triageService.createAssessment(
                    patientId, encounterId, nurseId, "Chest pain", ArrivalMode.AMBULANCE, locationId);

            assertNotNull(result);
            assertEquals(patientId, result.getPatientId());
            assertEquals(encounterId, result.getEncounterId());
            assertEquals(nurseId, result.getTriageNurseId());
            assertEquals("Chest pain", result.getChiefComplaint());
            assertEquals(ArrivalMode.AMBULANCE, result.getArrivalMode());
            assertEquals(locationId, result.getLocationId());
            verify(assessmentRepository).save(any(TriageAssessment.class));
        }

        @Test
        @DisplayName("should throw exception when assessment already exists for encounter")
        void shouldThrowExceptionWhenAssessmentAlreadyExists() {
            TriageAssessment existing = new TriageAssessment(patientId, encounterId, nurseId, "Existing");
            when(assessmentRepository.findByEncounterId(encounterId)).thenReturn(Optional.of(existing));

            assertThrows(AssessmentAlreadyExistsException.class, () ->
                    triageService.createAssessment(patientId, encounterId, nurseId, "Chest pain", ArrivalMode.WALK_IN, locationId));

            verify(assessmentRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("updateAssessment")
    class UpdateAssessment {

        @Test
        @DisplayName("should update assessment successfully")
        void shouldUpdateAssessmentSuccessfully() {
            UUID assessmentId = UUID.randomUUID();
            TriageAssessment assessment = new TriageAssessment(patientId, encounterId, nurseId, "Initial");
            when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
            when(assessmentRepository.save(any(TriageAssessment.class))).thenAnswer(inv -> inv.getArgument(0));

            TriageAssessment result = triageService.updateAssessment(
                    assessmentId, "Updated complaint", "Some notes", TriagePriority.URGENT, CareLevel.EMERGENCY_CARE);

            assertEquals("Updated complaint", result.getChiefComplaint());
            assertEquals("Some notes", result.getNotes());
            assertEquals(TriagePriority.URGENT, result.getPriority());
            assertEquals(CareLevel.EMERGENCY_CARE, result.getCareLevel());
        }

        @Test
        @DisplayName("should throw exception when assessment not found")
        void shouldThrowExceptionWhenAssessmentNotFound() {
            UUID assessmentId = UUID.randomUUID();
            when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.empty());

            assertThrows(AssessmentNotFoundException.class, () ->
                    triageService.updateAssessment(assessmentId, "Complaint", null, null, null));
        }

        @Test
        @DisplayName("should throw exception when assessment is already completed")
        void shouldThrowExceptionWhenAssessmentAlreadyCompleted() {
            UUID assessmentId = UUID.randomUUID();
            TriageAssessment assessment = new TriageAssessment(patientId, encounterId, nurseId, "Initial");
            assessment.complete(TriagePriority.URGENT, CareLevel.EMERGENCY_CARE, Disposition.ADMIT);
            when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));

            assertThrows(AssessmentAlreadyCompletedException.class, () ->
                    triageService.updateAssessment(assessmentId, "Complaint", null, null, null));
        }
    }

    @Nested
    @DisplayName("completeAssessment")
    class CompleteAssessment {

        @Test
        @DisplayName("should complete assessment successfully")
        void shouldCompleteAssessmentSuccessfully() {
            UUID assessmentId = UUID.randomUUID();
            UUID protocolId = UUID.randomUUID();
            TriageAssessment assessment = new TriageAssessment(patientId, encounterId, nurseId, "Chest pain");
            when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
            when(assessmentRepository.save(any(TriageAssessment.class))).thenAnswer(inv -> inv.getArgument(0));

            TriageAssessment result = triageService.completeAssessment(
                    assessmentId, TriagePriority.EMERGENT, CareLevel.EMERGENCY_CARE,
                    Disposition.ADMIT, "Assessment notes", protocolId);

            assertEquals(TriagePriority.EMERGENT, result.getPriority());
            assertEquals(CareLevel.EMERGENCY_CARE, result.getCareLevel());
            assertEquals(Disposition.ADMIT, result.getDisposition());
            assertEquals(AssessmentStatus.COMPLETED, result.getStatus());
            assertEquals("Assessment notes", result.getNotes());
            assertEquals(protocolId, result.getRecommendedProtocolId());
        }

        @Test
        @DisplayName("should throw exception when completing already completed assessment")
        void shouldThrowExceptionWhenCompletingAlreadyCompletedAssessment() {
            UUID assessmentId = UUID.randomUUID();
            TriageAssessment assessment = new TriageAssessment(patientId, encounterId, nurseId, "Chest pain");
            assessment.complete(TriagePriority.URGENT, CareLevel.EMERGENCY_CARE, Disposition.ADMIT);
            when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));

            assertThrows(AssessmentAlreadyCompletedException.class, () ->
                    triageService.completeAssessment(assessmentId, TriagePriority.EMERGENT,
                            CareLevel.EMERGENCY_CARE, Disposition.ADMIT, null, null));
        }
    }

    @Nested
    @DisplayName("escalatePriority")
    class EscalatePriority {

        @Test
        @DisplayName("should escalate priority successfully")
        void shouldEscalatePrioritySuccessfully() {
            UUID assessmentId = UUID.randomUUID();
            UUID escalatedBy = UUID.randomUUID();
            TriageAssessment assessment = new TriageAssessment(patientId, encounterId, nurseId, "Chest pain");
            assessment.setPriority(TriagePriority.URGENT);
            when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
            when(assessmentRepository.save(any(TriageAssessment.class))).thenAnswer(inv -> inv.getArgument(0));

            TriageAssessment result = triageService.escalatePriority(
                    assessmentId, TriagePriority.EMERGENT, "Worsening symptoms", escalatedBy);

            assertEquals(TriagePriority.EMERGENT, result.getPriority());
            assertEquals(1, result.getEscalationHistory().size());
        }
    }

    @Nested
    @DisplayName("addSymptom")
    class AddSymptom {

        @Test
        @DisplayName("should add symptom to assessment")
        void shouldAddSymptomToAssessment() {
            UUID assessmentId = UUID.randomUUID();
            TriageAssessment assessment = new TriageAssessment(patientId, encounterId, nurseId, "Chest pain");
            when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
            when(assessmentRepository.save(any(TriageAssessment.class))).thenAnswer(inv -> inv.getArgument(0));

            Symptom result = triageService.addSymptom(
                    assessmentId, "CHEST_PAIN", "Sharp chest pain",
                    Instant.now().minusSeconds(3600), "1 hour",
                    Severity.SEVERE, "Left chest", true);

            assertNotNull(result);
            assertEquals("CHEST_PAIN", result.getSymptomCode());
            assertEquals("Sharp chest pain", result.getDescription());
            assertEquals(Severity.SEVERE, result.getSeverity());
            assertTrue(result.isChiefComplaint());
        }
    }

    @Nested
    @DisplayName("recordVitalSigns")
    class RecordVitalSigns {

        @Test
        @DisplayName("should record vital signs")
        void shouldRecordVitalSigns() {
            UUID assessmentId = UUID.randomUUID();
            UUID recordedBy = UUID.randomUUID();
            TriageAssessment assessment = new TriageAssessment(patientId, encounterId, nurseId, "Chest pain");
            when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
            when(assessmentRepository.save(any(TriageAssessment.class))).thenAnswer(inv -> inv.getArgument(0));

            VitalSigns result = triageService.recordVitalSigns(
                    assessmentId, recordedBy,
                    120, 80, 75, 16, 37.0, 98, 3,
                    ConsciousnessLevel.ALERT, 5.5);

            assertNotNull(result);
            assertEquals(120, result.getBloodPressureSystolic());
            assertEquals(80, result.getBloodPressureDiastolic());
            assertEquals(75, result.getHeartRate());
            assertEquals(16, result.getRespiratoryRate());
            assertEquals(37.0, result.getTemperature());
            assertEquals(98, result.getOxygenSaturation());
            assertEquals(3, result.getPainLevel());
            assertEquals(ConsciousnessLevel.ALERT, result.getConsciousnessLevel());
            assertEquals(5.5, result.getGlucoseLevel());
        }
    }

    @Nested
    @DisplayName("getTriageQueue")
    class GetTriageQueue {

        @Test
        @DisplayName("should return queue info")
        void shouldReturnQueueInfo() {
            when(assessmentRepository.findActiveByLocationOrderByPriority(locationId))
                    .thenReturn(List.of());
            when(assessmentRepository.countWaitingByLocation(locationId)).thenReturn(5L);
            when(assessmentRepository.findInProgressByLocation(locationId)).thenReturn(List.of());
            when(assessmentRepository.countWaitingByLocationGroupByPriority(locationId))
                    .thenReturn(List.of());

            TriageService.TriageQueueInfo result = triageService.getTriageQueue(locationId);

            assertNotNull(result);
            assertEquals(locationId, result.locationId());
            assertEquals(5L, result.totalWaiting());
            assertEquals(0, result.averageWaitMinutes());
        }
    }

    @Nested
    @DisplayName("listProtocols")
    class ListProtocols {

        @Test
        @DisplayName("should list active protocols by category")
        void shouldListActiveProtocolsByCategory() {
            TriageProtocol protocol = new TriageProtocol("CHEST", "Chest Pain Protocol");
            protocol.setCategory("Cardiovascular");
            when(protocolRepository.findActiveByCategory("Cardiovascular")).thenReturn(List.of(protocol));

            List<TriageProtocol> result = triageService.listProtocols("Cardiovascular", true);

            assertEquals(1, result.size());
            assertEquals("Chest Pain Protocol", result.get(0).getName());
        }

        @Test
        @DisplayName("should list all protocols when not filtering active only")
        void shouldListAllProtocolsWhenNotFilteringActiveOnly() {
            when(protocolRepository.findByCategory("Cardiovascular")).thenReturn(List.of());

            triageService.listProtocols("Cardiovascular", false);

            verify(protocolRepository).findByCategory("Cardiovascular");
        }

        @Test
        @DisplayName("should list all protocols when no category specified")
        void shouldListAllProtocolsWhenNoCategorySpecified() {
            when(protocolRepository.findAll()).thenReturn(List.of());

            triageService.listProtocols(null, false);

            verify(protocolRepository).findAll();
        }
    }
}
