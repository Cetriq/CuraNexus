package se.curanexus.triage.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.curanexus.triage.domain.*;
import se.curanexus.triage.repository.*;
import se.curanexus.triage.service.exception.*;

import java.time.Instant;
import java.util.*;

@Service
@Transactional
public class TriageService {

    private final TriageAssessmentRepository assessmentRepository;
    private final SymptomRepository symptomRepository;
    private final VitalSignsRepository vitalSignsRepository;
    private final TriageProtocolRepository protocolRepository;

    public TriageService(
            TriageAssessmentRepository assessmentRepository,
            SymptomRepository symptomRepository,
            VitalSignsRepository vitalSignsRepository,
            TriageProtocolRepository protocolRepository) {
        this.assessmentRepository = assessmentRepository;
        this.symptomRepository = symptomRepository;
        this.vitalSignsRepository = vitalSignsRepository;
        this.protocolRepository = protocolRepository;
    }

    // ========== Assessment Operations ==========

    public TriageAssessment createAssessment(UUID patientId, UUID encounterId, UUID triageNurseId,
                                              String chiefComplaint, ArrivalMode arrivalMode, UUID locationId) {
        // Check if assessment already exists for encounter
        if (assessmentRepository.findByEncounterId(encounterId).isPresent()) {
            throw new AssessmentAlreadyExistsException(encounterId);
        }

        TriageAssessment assessment = new TriageAssessment(patientId, encounterId, triageNurseId, chiefComplaint);
        assessment.setArrivalMode(arrivalMode);
        assessment.setLocationId(locationId);

        return assessmentRepository.save(assessment);
    }

    @Transactional(readOnly = true)
    public Optional<TriageAssessment> getAssessment(UUID assessmentId) {
        return assessmentRepository.findById(assessmentId);
    }

    @Transactional(readOnly = true)
    public Optional<TriageAssessment> getAssessmentByEncounter(UUID encounterId) {
        return assessmentRepository.findByEncounterId(encounterId);
    }

    public TriageAssessment updateAssessment(UUID assessmentId, String chiefComplaint, String notes,
                                              TriagePriority priority, CareLevel careLevel) {
        TriageAssessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new AssessmentNotFoundException(assessmentId));

        if (assessment.getStatus() == AssessmentStatus.COMPLETED) {
            throw new AssessmentAlreadyCompletedException(assessmentId);
        }

        if (chiefComplaint != null) {
            assessment.setChiefComplaint(chiefComplaint);
        }
        if (notes != null) {
            assessment.setNotes(notes);
        }
        if (priority != null) {
            assessment.setPriority(priority);
        }
        if (careLevel != null) {
            assessment.setCareLevel(careLevel);
        }

        return assessmentRepository.save(assessment);
    }

    public TriageAssessment completeAssessment(UUID assessmentId, TriagePriority priority,
                                                CareLevel careLevel, Disposition disposition,
                                                String notes, UUID protocolId) {
        TriageAssessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new AssessmentNotFoundException(assessmentId));

        if (assessment.getStatus() == AssessmentStatus.COMPLETED) {
            throw new AssessmentAlreadyCompletedException(assessmentId);
        }

        assessment.complete(priority, careLevel, disposition);
        if (notes != null) {
            assessment.setNotes(notes);
        }
        if (protocolId != null) {
            assessment.setRecommendedProtocolId(protocolId);
        }

        return assessmentRepository.save(assessment);
    }

    public TriageAssessment escalatePriority(UUID assessmentId, TriagePriority newPriority,
                                              String reason, UUID escalatedBy) {
        TriageAssessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new AssessmentNotFoundException(assessmentId));

        if (assessment.getStatus() == AssessmentStatus.COMPLETED) {
            throw new AssessmentAlreadyCompletedException(assessmentId);
        }

        assessment.escalate(newPriority, reason, escalatedBy);
        return assessmentRepository.save(assessment);
    }

    @Transactional(readOnly = true)
    public Page<TriageAssessment> searchAssessments(UUID patientId, UUID encounterId,
                                                     TriagePriority priority, AssessmentStatus status,
                                                     Instant fromDate, Instant toDate, Pageable pageable) {
        return assessmentRepository.searchAssessments(patientId, encounterId, priority, status, fromDate, toDate, pageable);
    }

    // ========== Symptom Operations ==========

    public Symptom addSymptom(UUID assessmentId, String symptomCode, String description,
                               Instant onset, String duration, Severity severity,
                               String bodyLocation, boolean isChiefComplaint) {
        TriageAssessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new AssessmentNotFoundException(assessmentId));

        Symptom symptom = new Symptom(symptomCode, description);
        symptom.setOnset(onset);
        symptom.setDuration(duration);
        symptom.setSeverity(severity);
        symptom.setBodyLocation(bodyLocation);
        symptom.setChiefComplaint(isChiefComplaint);

        assessment.addSymptom(symptom);
        assessmentRepository.save(assessment);

        return symptom;
    }

    @Transactional(readOnly = true)
    public List<Symptom> getSymptoms(UUID assessmentId) {
        return symptomRepository.findByAssessmentIdOrderByRecordedAtAsc(assessmentId);
    }

    // ========== Vital Signs Operations ==========

    public VitalSigns recordVitalSigns(UUID assessmentId, UUID recordedBy,
                                        Integer bpSystolic, Integer bpDiastolic,
                                        Integer heartRate, Integer respRate,
                                        Double temperature, Integer spo2,
                                        Integer painLevel, ConsciousnessLevel consciousness,
                                        Double glucose) {
        TriageAssessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new AssessmentNotFoundException(assessmentId));

        VitalSigns vitalSigns = new VitalSigns(recordedBy);
        vitalSigns.setBloodPressureSystolic(bpSystolic);
        vitalSigns.setBloodPressureDiastolic(bpDiastolic);
        vitalSigns.setHeartRate(heartRate);
        vitalSigns.setRespiratoryRate(respRate);
        vitalSigns.setTemperature(temperature);
        vitalSigns.setOxygenSaturation(spo2);
        vitalSigns.setPainLevel(painLevel);
        vitalSigns.setConsciousnessLevel(consciousness);
        vitalSigns.setGlucoseLevel(glucose);

        assessment.setVitalSigns(vitalSigns);
        assessmentRepository.save(assessment);

        return vitalSigns;
    }

    @Transactional(readOnly = true)
    public Optional<VitalSigns> getVitalSigns(UUID assessmentId) {
        return vitalSignsRepository.findByAssessmentId(assessmentId);
    }

    // ========== Queue Operations ==========

    @Transactional(readOnly = true)
    public TriageQueueInfo getTriageQueue(UUID locationId) {
        List<TriageAssessment> patients = assessmentRepository.findActiveByLocationOrderByPriority(locationId);
        long totalWaiting = assessmentRepository.countWaitingByLocation(locationId);

        // Calculate average wait time in Java to avoid PostgreSQL-specific EXTRACT(EPOCH...)
        int avgWait = calculateAverageWaitMinutes(assessmentRepository.findInProgressByLocation(locationId));

        Map<TriagePriority, Long> byPriority = new EnumMap<>(TriagePriority.class);
        for (Object[] row : assessmentRepository.countWaitingByLocationGroupByPriority(locationId)) {
            TriagePriority priority = (TriagePriority) row[0];
            if (priority != null) {
                byPriority.put(priority, (Long) row[1]);
            }
        }

        return new TriageQueueInfo(
                locationId,
                totalWaiting,
                byPriority,
                avgWait,
                patients
        );
    }

    private int calculateAverageWaitMinutes(List<TriageAssessment> assessments) {
        if (assessments.isEmpty()) {
            return 0;
        }
        Instant now = Instant.now();
        long totalMinutes = assessments.stream()
                .mapToLong(a -> {
                    Instant endTime = a.getTriageEndTime() != null ? a.getTriageEndTime() : now;
                    return java.time.Duration.between(a.getArrivalTime(), endTime).toMinutes();
                })
                .sum();
        return (int) (totalMinutes / assessments.size());
    }

    public record TriageQueueInfo(
            UUID locationId,
            long totalWaiting,
            Map<TriagePriority, Long> byPriority,
            int averageWaitMinutes,
            List<TriageAssessment> patients
    ) {}

    // ========== Protocol Operations ==========

    @Transactional(readOnly = true)
    public List<TriageProtocol> listProtocols(String category, boolean activeOnly) {
        if (activeOnly) {
            return protocolRepository.findActiveByCategory(category);
        }
        return category != null ? protocolRepository.findByCategory(category) : protocolRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<TriageProtocol> getProtocol(UUID protocolId) {
        return protocolRepository.findById(protocolId);
    }

    @Transactional(readOnly = true)
    public Optional<TriageProtocol> getProtocolByCode(String code) {
        return protocolRepository.findByCode(code);
    }
}
