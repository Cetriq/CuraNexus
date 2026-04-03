package se.curanexus.journal.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.curanexus.events.DomainEventPublisher;
import se.curanexus.journal.domain.*;
import se.curanexus.journal.repository.*;
import se.curanexus.journal.service.exception.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JournalServiceTest {

    @Mock
    private ClinicalNoteRepository noteRepository;

    @Mock
    private DiagnosisRepository diagnosisRepository;

    @Mock
    private ProcedureRepository procedureRepository;

    @Mock
    private ObservationRepository observationRepository;

    @Mock
    private DomainEventPublisher eventPublisher;

    @InjectMocks
    private JournalService journalService;

    private UUID encounterId;
    private UUID patientId;
    private UUID authorId;

    @BeforeEach
    void setUp() {
        encounterId = UUID.randomUUID();
        patientId = UUID.randomUUID();
        authorId = UUID.randomUUID();
    }

    // === Clinical Note Tests ===

    @Test
    void shouldCreateNote() {
        when(noteRepository.save(any(ClinicalNote.class))).thenAnswer(i -> i.getArgument(0));

        ClinicalNote result = journalService.createNote(
                encounterId, patientId, NoteType.PROGRESS,
                authorId, "Dr. Smith", "Progress Note", "Patient is improving"
        );

        assertNotNull(result);
        assertEquals(NoteType.PROGRESS, result.getType());
        assertEquals("Progress Note", result.getTitle());
        assertEquals("Patient is improving", result.getContent());
        verify(noteRepository).save(any(ClinicalNote.class));
    }

    @Test
    void shouldGetNote() {
        UUID noteId = UUID.randomUUID();
        ClinicalNote note = new ClinicalNote(encounterId, patientId, NoteType.PROGRESS, authorId, "Dr. Smith");
        when(noteRepository.findById(noteId)).thenReturn(Optional.of(note));

        ClinicalNote result = journalService.getNote(noteId);

        assertNotNull(result);
        assertEquals(NoteType.PROGRESS, result.getType());
    }

    @Test
    void shouldThrowWhenNoteNotFound() {
        UUID noteId = UUID.randomUUID();
        when(noteRepository.findById(noteId)).thenReturn(Optional.empty());

        assertThrows(NoteNotFoundException.class, () -> journalService.getNote(noteId));
    }

    @Test
    void shouldSignNote() {
        UUID noteId = UUID.randomUUID();
        ClinicalNote note = new ClinicalNote(encounterId, patientId, NoteType.PROGRESS, authorId, "Dr. Smith");
        when(noteRepository.findById(noteId)).thenReturn(Optional.of(note));
        when(noteRepository.save(any(ClinicalNote.class))).thenAnswer(i -> i.getArgument(0));

        UUID signedById = UUID.randomUUID();
        ClinicalNote result = journalService.signNote(noteId, signedById, "Dr. Johnson");

        assertEquals(NoteStatus.FINAL, result.getStatus());
        assertEquals(signedById, result.getSignedById());
    }

    @Test
    void shouldThrowWhenSigningFinalNote() {
        UUID noteId = UUID.randomUUID();
        ClinicalNote note = new ClinicalNote(encounterId, patientId, NoteType.PROGRESS, authorId, "Dr. Smith");
        note.sign(UUID.randomUUID(), "Dr. Existing");
        when(noteRepository.findById(noteId)).thenReturn(Optional.of(note));

        assertThrows(InvalidNoteStateException.class, () ->
                journalService.signNote(noteId, UUID.randomUUID(), "Dr. New"));
    }

    // === Diagnosis Tests ===

    @Test
    void shouldCreateDiagnosis() {
        when(diagnosisRepository.save(any(Diagnosis.class))).thenAnswer(i -> i.getArgument(0));

        Diagnosis result = journalService.createDiagnosis(
                encounterId, patientId, "J06.9",
                "Acute upper respiratory infection", DiagnosisType.PRINCIPAL, authorId
        );

        assertNotNull(result);
        assertEquals("J06.9", result.getCode());
        assertEquals(DiagnosisType.PRINCIPAL, result.getType());
        verify(diagnosisRepository).save(any(Diagnosis.class));
    }

    @Test
    void shouldResolveDiagnosis() {
        UUID diagnosisId = UUID.randomUUID();
        Diagnosis diagnosis = new Diagnosis(encounterId, patientId, "J06.9");
        when(diagnosisRepository.findById(diagnosisId)).thenReturn(Optional.of(diagnosis));
        when(diagnosisRepository.save(any(Diagnosis.class))).thenAnswer(i -> i.getArgument(0));

        LocalDate resolvedDate = LocalDate.now();
        Diagnosis result = journalService.resolveDiagnosis(diagnosisId, resolvedDate);

        assertEquals(resolvedDate, result.getResolvedDate());
    }

    @Test
    void shouldThrowWhenDiagnosisNotFound() {
        UUID diagnosisId = UUID.randomUUID();
        when(diagnosisRepository.findById(diagnosisId)).thenReturn(Optional.empty());

        assertThrows(DiagnosisNotFoundException.class, () -> journalService.getDiagnosis(diagnosisId));
    }

    // === Procedure Tests ===

    @Test
    void shouldCreateProcedure() {
        when(procedureRepository.save(any(Procedure.class))).thenAnswer(i -> i.getArgument(0));

        Procedure result = journalService.createProcedure(
                encounterId, patientId, "AA123",
                "Appendectomy", "Abdomen", "Right"
        );

        assertNotNull(result);
        assertEquals("AA123", result.getCode());
        assertEquals("Abdomen", result.getBodySite());
        assertEquals(ProcedureStatus.PLANNED, result.getStatus());
        verify(procedureRepository).save(any(Procedure.class));
    }

    @Test
    void shouldStartProcedure() {
        UUID procedureId = UUID.randomUUID();
        Procedure procedure = new Procedure(encounterId, patientId, "AA123");
        when(procedureRepository.findById(procedureId)).thenReturn(Optional.of(procedure));
        when(procedureRepository.save(any(Procedure.class))).thenAnswer(i -> i.getArgument(0));

        UUID performedById = UUID.randomUUID();
        Procedure result = journalService.startProcedure(procedureId, performedById, "Dr. Surgeon");

        assertEquals(ProcedureStatus.IN_PROGRESS, result.getStatus());
        assertEquals(performedById, result.getPerformedById());
    }

    @Test
    void shouldCompleteProcedure() {
        UUID procedureId = UUID.randomUUID();
        Procedure procedure = new Procedure(encounterId, patientId, "AA123");
        procedure.start(UUID.randomUUID(), "Dr. Surgeon");
        when(procedureRepository.findById(procedureId)).thenReturn(Optional.of(procedure));
        when(procedureRepository.save(any(Procedure.class))).thenAnswer(i -> i.getArgument(0));

        Procedure result = journalService.completeProcedure(procedureId, "Successful");

        assertEquals(ProcedureStatus.COMPLETED, result.getStatus());
        assertEquals("Successful", result.getOutcome());
    }

    // === Observation Tests ===

    @Test
    void shouldCreateNumericObservation() {
        when(observationRepository.save(any(Observation.class))).thenAnswer(i -> i.getArgument(0));

        Observation result = journalService.createNumericObservation(
                patientId, encounterId, "8867-4",
                ObservationCategory.VITAL_SIGNS, new BigDecimal("72"),
                "bpm", LocalDateTime.now(), authorId, "Nurse Jane"
        );

        assertNotNull(result);
        assertEquals("8867-4", result.getCode());
        assertEquals(new BigDecimal("72"), result.getValueNumeric());
        assertEquals("bpm", result.getUnit());
        verify(observationRepository).save(any(Observation.class));
    }

    @Test
    void shouldGetVitalSignsByPatient() {
        List<Observation> observations = List.of(
                new Observation(patientId, "8867-4", ObservationCategory.VITAL_SIGNS, LocalDateTime.now())
        );
        when(observationRepository.findVitalSignsByPatient(patientId)).thenReturn(observations);

        List<Observation> result = journalService.getVitalSignsByPatient(patientId);

        assertEquals(1, result.size());
        verify(observationRepository).findVitalSignsByPatient(patientId);
    }

    @Test
    void shouldThrowWhenObservationNotFound() {
        UUID observationId = UUID.randomUUID();
        when(observationRepository.findById(observationId)).thenReturn(Optional.empty());

        assertThrows(ObservationNotFoundException.class, () -> journalService.getObservation(observationId));
    }
}
