package se.curanexus.encounter.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.curanexus.encounter.api.dto.*;
import se.curanexus.encounter.domain.*;
import se.curanexus.encounter.repository.*;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EncounterServiceTest {

    @Mock
    private EncounterRepository encounterRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private EncounterReasonRepository reasonRepository;

    private EncounterService encounterService;

    @BeforeEach
    void setUp() {
        encounterService = new EncounterService(
                encounterRepository,
                participantRepository,
                reasonRepository
        );
    }

    @Test
    void createEncounter_shouldCreateNewEncounter() {
        // Given
        UUID patientId = UUID.randomUUID();
        CreateEncounterRequest request = new CreateEncounterRequest(
                patientId,
                EncounterClass.OUTPATIENT,
                EncounterType.INITIAL,
                EncounterPriority.NORMAL,
                "Cardiology",
                null,
                null,
                null,
                null
        );

        when(encounterRepository.save(any(Encounter.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        EncounterDto result = encounterService.createEncounter(request);

        // Then
        assertThat(result.patientId()).isEqualTo(patientId);
        assertThat(result.encounterClass()).isEqualTo(EncounterClass.OUTPATIENT);
        assertThat(result.status()).isEqualTo(EncounterStatus.PLANNED);
        verify(encounterRepository).save(any(Encounter.class));
    }

    @Test
    void getEncounter_shouldReturnEncounter() {
        // Given
        UUID encounterId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        Encounter encounter = new Encounter(patientId, EncounterClass.OUTPATIENT);

        when(encounterRepository.findById(encounterId)).thenReturn(Optional.of(encounter));

        // When
        EncounterDto result = encounterService.getEncounter(encounterId);

        // Then
        assertThat(result.patientId()).isEqualTo(patientId);
        assertThat(result.encounterClass()).isEqualTo(EncounterClass.OUTPATIENT);
    }

    @Test
    void getEncounter_shouldThrowWhenNotFound() {
        // Given
        UUID encounterId = UUID.randomUUID();
        when(encounterRepository.findById(encounterId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> encounterService.getEncounter(encounterId))
                .isInstanceOf(EncounterNotFoundException.class);
    }

    @Test
    void updateEncounterStatus_shouldUpdateStatus() {
        // Given
        UUID encounterId = UUID.randomUUID();
        Encounter encounter = new Encounter(UUID.randomUUID(), EncounterClass.OUTPATIENT);
        UpdateStatusRequest request = new UpdateStatusRequest(EncounterStatus.ARRIVED, null);

        when(encounterRepository.findById(encounterId)).thenReturn(Optional.of(encounter));
        when(encounterRepository.save(any(Encounter.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        EncounterDto result = encounterService.updateEncounterStatus(encounterId, request);

        // Then
        assertThat(result.status()).isEqualTo(EncounterStatus.ARRIVED);
    }

    @Test
    void updateEncounterStatus_shouldThrowForInvalidTransition() {
        // Given
        UUID encounterId = UUID.randomUUID();
        Encounter encounter = new Encounter(UUID.randomUUID(), EncounterClass.OUTPATIENT);
        UpdateStatusRequest request = new UpdateStatusRequest(EncounterStatus.FINISHED, null);

        when(encounterRepository.findById(encounterId)).thenReturn(Optional.of(encounter));

        // When & Then
        assertThatThrownBy(() -> encounterService.updateEncounterStatus(encounterId, request))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    void addParticipant_shouldAddParticipant() {
        // Given
        UUID encounterId = UUID.randomUUID();
        UUID practitionerId = UUID.randomUUID();
        Encounter encounter = new Encounter(UUID.randomUUID(), EncounterClass.OUTPATIENT);
        AddParticipantRequest request = new AddParticipantRequest(
                ParticipantType.PRACTITIONER,
                practitionerId,
                ParticipantRole.PRIMARY,
                null,
                null
        );

        when(encounterRepository.findById(encounterId)).thenReturn(Optional.of(encounter));
        when(encounterRepository.save(any(Encounter.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ParticipantDto result = encounterService.addParticipant(encounterId, request);

        // Then
        assertThat(result.type()).isEqualTo(ParticipantType.PRACTITIONER);
        assertThat(result.practitionerId()).isEqualTo(practitionerId);
        assertThat(result.role()).isEqualTo(ParticipantRole.PRIMARY);
    }

    @Test
    void addReason_shouldAddReason() {
        // Given
        UUID encounterId = UUID.randomUUID();
        Encounter encounter = new Encounter(UUID.randomUUID(), EncounterClass.OUTPATIENT);
        AddReasonRequest request = new AddReasonRequest(
                ReasonType.CHIEF_COMPLAINT,
                "J06.9",
                "ICD-10-SE",
                "Upper respiratory infection",
                true
        );

        when(encounterRepository.findById(encounterId)).thenReturn(Optional.of(encounter));
        when(encounterRepository.save(any(Encounter.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        EncounterReasonDto result = encounterService.addReason(encounterId, request);

        // Then
        assertThat(result.type()).isEqualTo(ReasonType.CHIEF_COMPLAINT);
        assertThat(result.code()).isEqualTo("J06.9");
        assertThat(result.codeSystem()).isEqualTo("ICD-10-SE");
        assertThat(result.isPrimary()).isTrue();
    }

    @Test
    void updateEncounter_shouldUpdateFields() {
        // Given
        UUID encounterId = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();
        Encounter encounter = new Encounter(UUID.randomUUID(), EncounterClass.OUTPATIENT);
        UpdateEncounterRequest request = new UpdateEncounterRequest(
                EncounterType.FOLLOW_UP,
                EncounterPriority.URGENT,
                "Neurology",
                unitId,
                null,
                null,
                null,
                null,
                null
        );

        when(encounterRepository.findById(encounterId)).thenReturn(Optional.of(encounter));
        when(encounterRepository.save(any(Encounter.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        EncounterDto result = encounterService.updateEncounter(encounterId, request);

        // Then
        assertThat(result.type()).isEqualTo(EncounterType.FOLLOW_UP);
        assertThat(result.priority()).isEqualTo(EncounterPriority.URGENT);
        assertThat(result.serviceType()).isEqualTo("Neurology");
        assertThat(result.responsibleUnitId()).isEqualTo(unitId);
    }
}
