package se.curanexus.journal.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.curanexus.journal.domain.*;
import se.curanexus.journal.repository.*;
import se.curanexus.journal.service.exception.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class JournalService {

    private final ClinicalNoteRepository noteRepository;
    private final DiagnosisRepository diagnosisRepository;
    private final ProcedureRepository procedureRepository;
    private final ObservationRepository observationRepository;

    public JournalService(ClinicalNoteRepository noteRepository,
                          DiagnosisRepository diagnosisRepository,
                          ProcedureRepository procedureRepository,
                          ObservationRepository observationRepository) {
        this.noteRepository = noteRepository;
        this.diagnosisRepository = diagnosisRepository;
        this.procedureRepository = procedureRepository;
        this.observationRepository = observationRepository;
    }

    // === Clinical Notes ===

    public ClinicalNote createNote(UUID encounterId, UUID patientId, NoteType type,
                                    UUID authorId, String authorName, String title, String content) {
        ClinicalNote note = new ClinicalNote(encounterId, patientId, type, authorId, authorName);
        note.setTitle(title);
        note.setContent(content);
        return noteRepository.save(note);
    }

    @Transactional(readOnly = true)
    public ClinicalNote getNote(UUID noteId) {
        return noteRepository.findById(noteId)
                .orElseThrow(() -> new NoteNotFoundException(noteId));
    }

    @Transactional(readOnly = true)
    public List<ClinicalNote> getNotesByEncounter(UUID encounterId) {
        return noteRepository.findByEncounterIdOrderByCreatedAtDesc(encounterId);
    }

    @Transactional(readOnly = true)
    public List<ClinicalNote> getNotesByPatient(UUID patientId) {
        return noteRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
    }

    public ClinicalNote updateNote(UUID noteId, String title, String content) {
        ClinicalNote note = getNote(noteId);
        if (!note.canEdit()) {
            throw new InvalidNoteStateException(noteId, note.getStatus(), "edit");
        }
        note.setTitle(title);
        note.setContent(content);
        return noteRepository.save(note);
    }

    public ClinicalNote signNote(UUID noteId, UUID signedById, String signedByName) {
        ClinicalNote note = getNote(noteId);
        if (!note.canSign()) {
            throw new InvalidNoteStateException(noteId, note.getStatus(), "sign");
        }
        note.sign(signedById, signedByName);
        return noteRepository.save(note);
    }

    public ClinicalNote amendNote(UUID noteId, String newContent) {
        ClinicalNote note = getNote(noteId);
        if (note.getStatus() != NoteStatus.FINAL) {
            throw new InvalidNoteStateException(noteId, note.getStatus(), "amend");
        }
        note.amend(newContent);
        return noteRepository.save(note);
    }

    public ClinicalNote cancelNote(UUID noteId) {
        ClinicalNote note = getNote(noteId);
        note.cancel();
        return noteRepository.save(note);
    }

    // === Diagnoses ===

    public Diagnosis createDiagnosis(UUID encounterId, UUID patientId, String code,
                                      String displayText, DiagnosisType type, UUID recordedById) {
        Diagnosis diagnosis = new Diagnosis(encounterId, patientId, code);
        diagnosis.setDisplayText(displayText);
        diagnosis.setType(type);
        diagnosis.setRecordedById(recordedById);
        return diagnosisRepository.save(diagnosis);
    }

    @Transactional(readOnly = true)
    public Diagnosis getDiagnosis(UUID diagnosisId) {
        return diagnosisRepository.findById(diagnosisId)
                .orElseThrow(() -> new DiagnosisNotFoundException(diagnosisId));
    }

    @Transactional(readOnly = true)
    public List<Diagnosis> getDiagnosesByEncounter(UUID encounterId) {
        return diagnosisRepository.findByEncounterIdOrderByRankAsc(encounterId);
    }

    @Transactional(readOnly = true)
    public List<Diagnosis> getDiagnosesByPatient(UUID patientId) {
        return diagnosisRepository.findByPatientIdOrderByRecordedAtDesc(patientId);
    }

    @Transactional(readOnly = true)
    public List<Diagnosis> getActiveDiagnosesByPatient(UUID patientId) {
        return diagnosisRepository.findActiveByPatient(patientId);
    }

    public Diagnosis updateDiagnosis(UUID diagnosisId, String code, String displayText,
                                      DiagnosisType type, Integer rank) {
        Diagnosis diagnosis = getDiagnosis(diagnosisId);
        diagnosis.setCode(code);
        diagnosis.setDisplayText(displayText);
        diagnosis.setType(type);
        diagnosis.setRank(rank);
        return diagnosisRepository.save(diagnosis);
    }

    public Diagnosis resolveDiagnosis(UUID diagnosisId, LocalDate resolvedDate) {
        Diagnosis diagnosis = getDiagnosis(diagnosisId);
        diagnosis.setResolvedDate(resolvedDate);
        return diagnosisRepository.save(diagnosis);
    }

    public void deleteDiagnosis(UUID diagnosisId) {
        if (!diagnosisRepository.existsById(diagnosisId)) {
            throw new DiagnosisNotFoundException(diagnosisId);
        }
        diagnosisRepository.deleteById(diagnosisId);
    }

    // === Procedures ===

    public Procedure createProcedure(UUID encounterId, UUID patientId, String code,
                                      String displayText, String bodySite, String laterality) {
        Procedure procedure = new Procedure(encounterId, patientId, code);
        procedure.setDisplayText(displayText);
        procedure.setBodySite(bodySite);
        procedure.setLaterality(laterality);
        return procedureRepository.save(procedure);
    }

    @Transactional(readOnly = true)
    public Procedure getProcedure(UUID procedureId) {
        return procedureRepository.findById(procedureId)
                .orElseThrow(() -> new ProcedureNotFoundException(procedureId));
    }

    @Transactional(readOnly = true)
    public List<Procedure> getProceduresByEncounter(UUID encounterId) {
        return procedureRepository.findByEncounterIdOrderByCreatedAtDesc(encounterId);
    }

    @Transactional(readOnly = true)
    public List<Procedure> getProceduresByPatient(UUID patientId) {
        return procedureRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
    }

    public Procedure startProcedure(UUID procedureId, UUID performedById, String performedByName) {
        Procedure procedure = getProcedure(procedureId);
        procedure.start(performedById, performedByName);
        return procedureRepository.save(procedure);
    }

    public Procedure completeProcedure(UUID procedureId, String outcome) {
        Procedure procedure = getProcedure(procedureId);
        procedure.complete(outcome);
        return procedureRepository.save(procedure);
    }

    public Procedure cancelProcedure(UUID procedureId) {
        Procedure procedure = getProcedure(procedureId);
        procedure.cancel();
        return procedureRepository.save(procedure);
    }

    public Procedure updateProcedureNotes(UUID procedureId, String notes) {
        Procedure procedure = getProcedure(procedureId);
        procedure.setNotes(notes);
        return procedureRepository.save(procedure);
    }

    // === Observations ===

    public Observation createNumericObservation(UUID patientId, UUID encounterId, String code,
                                                  ObservationCategory category, BigDecimal value,
                                                  String unit, LocalDateTime observedAt,
                                                  UUID recordedById, String recordedByName) {
        Observation observation = new Observation(patientId, code, category, observedAt);
        observation.setEncounterId(encounterId);
        observation.setNumericValue(value, unit);
        observation.setRecordedById(recordedById);
        observation.setRecordedByName(recordedByName);
        return observationRepository.save(observation);
    }

    public Observation createStringObservation(UUID patientId, UUID encounterId, String code,
                                                 ObservationCategory category, String value,
                                                 LocalDateTime observedAt,
                                                 UUID recordedById, String recordedByName) {
        Observation observation = new Observation(patientId, code, category, observedAt);
        observation.setEncounterId(encounterId);
        observation.setStringValue(value);
        observation.setRecordedById(recordedById);
        observation.setRecordedByName(recordedByName);
        return observationRepository.save(observation);
    }

    @Transactional(readOnly = true)
    public Observation getObservation(UUID observationId) {
        return observationRepository.findById(observationId)
                .orElseThrow(() -> new ObservationNotFoundException(observationId));
    }

    @Transactional(readOnly = true)
    public List<Observation> getObservationsByEncounter(UUID encounterId) {
        return observationRepository.findByEncounterIdOrderByObservedAtDesc(encounterId);
    }

    @Transactional(readOnly = true)
    public List<Observation> getObservationsByPatient(UUID patientId) {
        return observationRepository.findByPatientIdOrderByObservedAtDesc(patientId);
    }

    @Transactional(readOnly = true)
    public List<Observation> getVitalSignsByPatient(UUID patientId) {
        return observationRepository.findVitalSignsByPatient(patientId);
    }

    @Transactional(readOnly = true)
    public List<Observation> getLabResultsByPatient(UUID patientId) {
        return observationRepository.findLabResultsByPatient(patientId);
    }

    @Transactional(readOnly = true)
    public List<Observation> getObservationHistory(UUID patientId, String code) {
        return observationRepository.findPatientObservationHistory(patientId, code);
    }

    @Transactional(readOnly = true)
    public List<Observation> getCriticalObservations(UUID patientId) {
        return observationRepository.findCriticalByPatient(patientId);
    }

    public Observation updateObservationInterpretation(UUID observationId, ObservationInterpretation interpretation) {
        Observation observation = getObservation(observationId);
        observation.setInterpretation(interpretation);
        return observationRepository.save(observation);
    }

    public Observation setObservationReferenceRange(UUID observationId, BigDecimal low, BigDecimal high) {
        Observation observation = getObservation(observationId);
        observation.setReferenceRange(low, high);
        return observationRepository.save(observation);
    }
}
