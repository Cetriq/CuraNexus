package se.curanexus.encounter.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.curanexus.encounter.api.dto.*;
import se.curanexus.encounter.domain.*;
import se.curanexus.encounter.repository.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class EncounterService {

    private final EncounterRepository encounterRepository;
    private final ParticipantRepository participantRepository;
    private final EncounterReasonRepository reasonRepository;

    public EncounterService(EncounterRepository encounterRepository,
                            ParticipantRepository participantRepository,
                            EncounterReasonRepository reasonRepository) {
        this.encounterRepository = encounterRepository;
        this.participantRepository = participantRepository;
        this.reasonRepository = reasonRepository;
    }

    // Encounter operations

    @Transactional(readOnly = true)
    public Page<EncounterSummaryDto> searchEncounters(
            UUID patientId,
            EncounterStatus status,
            EncounterClass encounterClass,
            UUID responsibleUnitId,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable) {

        Instant fromInstant = fromDate != null ? fromDate.atStartOfDay().toInstant(ZoneOffset.UTC) : null;
        Instant toInstant = toDate != null ? toDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC) : null;

        return encounterRepository.searchEncounters(
                        patientId, status, encounterClass, responsibleUnitId, fromInstant, toInstant, pageable)
                .map(EncounterSummaryDto::from);
    }

    @Transactional(readOnly = true)
    public Page<EncounterSummaryDto> getPatientEncounters(UUID patientId, EncounterStatus status, Pageable pageable) {
        if (status != null) {
            return encounterRepository.findByPatientIdAndStatus(patientId, status, pageable)
                    .map(EncounterSummaryDto::from);
        }
        return encounterRepository.findByPatientId(patientId, pageable)
                .map(EncounterSummaryDto::from);
    }

    @Transactional(readOnly = true)
    public EncounterDto getEncounter(UUID encounterId) {
        Encounter encounter = findEncounterOrThrow(encounterId);
        return EncounterDto.from(encounter);
    }

    public EncounterDto createEncounter(CreateEncounterRequest request) {
        Encounter encounter = new Encounter(request.patientId(), request.encounterClass());
        encounter.setType(request.type());
        encounter.setPriority(request.priority());
        encounter.setServiceType(request.serviceType());
        encounter.setResponsibleUnitId(request.responsibleUnitId());
        encounter.setResponsiblePractitionerId(request.responsiblePractitionerId());
        encounter.setPlannedStartTime(request.plannedStartTime());
        encounter.setPlannedEndTime(request.plannedEndTime());

        Encounter saved = encounterRepository.save(encounter);
        return EncounterDto.from(saved);
    }

    public EncounterDto updateEncounter(UUID encounterId, UpdateEncounterRequest request) {
        Encounter encounter = findEncounterOrThrow(encounterId);

        if (request.type() != null) {
            encounter.setType(request.type());
        }
        if (request.priority() != null) {
            encounter.setPriority(request.priority());
        }
        if (request.serviceType() != null) {
            encounter.setServiceType(request.serviceType());
        }
        if (request.responsibleUnitId() != null) {
            encounter.setResponsibleUnitId(request.responsibleUnitId());
        }
        if (request.responsiblePractitionerId() != null) {
            encounter.setResponsiblePractitionerId(request.responsiblePractitionerId());
        }
        if (request.plannedStartTime() != null) {
            encounter.setPlannedStartTime(request.plannedStartTime());
        }
        if (request.plannedEndTime() != null) {
            encounter.setPlannedEndTime(request.plannedEndTime());
        }
        if (request.actualStartTime() != null) {
            encounter.setActualStartTime(request.actualStartTime());
        }
        if (request.actualEndTime() != null) {
            encounter.setActualEndTime(request.actualEndTime());
        }

        Encounter saved = encounterRepository.save(encounter);
        return EncounterDto.from(saved);
    }

    public EncounterDto updateEncounterStatus(UUID encounterId, UpdateStatusRequest request) {
        Encounter encounter = findEncounterOrThrow(encounterId);

        if (!encounter.canTransitionTo(request.status())) {
            throw new InvalidStatusTransitionException(encounter.getStatus(), request.status());
        }

        encounter.transitionTo(request.status());
        Encounter saved = encounterRepository.save(encounter);
        return EncounterDto.from(saved);
    }

    // Participant operations

    @Transactional(readOnly = true)
    public List<ParticipantDto> getParticipants(UUID encounterId) {
        findEncounterOrThrow(encounterId);
        return participantRepository.findByEncounterId(encounterId).stream()
                .map(ParticipantDto::from)
                .toList();
    }

    public ParticipantDto addParticipant(UUID encounterId, AddParticipantRequest request) {
        Encounter encounter = findEncounterOrThrow(encounterId);

        Participant participant = new Participant(request.type());
        participant.setPractitionerId(request.practitionerId());
        participant.setRole(request.role());
        participant.setPeriodStart(request.periodStart());
        participant.setPeriodEnd(request.periodEnd());

        encounter.addParticipant(participant);
        encounterRepository.save(encounter);

        return ParticipantDto.from(participant);
    }

    public ParticipantDto updateParticipant(UUID encounterId, UUID participantId, UpdateParticipantRequest request) {
        findEncounterOrThrow(encounterId);
        Participant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new ResourceNotFoundException("Participant", participantId));

        if (request.role() != null) {
            participant.setRole(request.role());
        }
        if (request.periodStart() != null) {
            participant.setPeriodStart(request.periodStart());
        }
        if (request.periodEnd() != null) {
            participant.setPeriodEnd(request.periodEnd());
        }

        Participant saved = participantRepository.save(participant);
        return ParticipantDto.from(saved);
    }

    public void removeParticipant(UUID encounterId, UUID participantId) {
        Encounter encounter = findEncounterOrThrow(encounterId);
        Participant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new ResourceNotFoundException("Participant", participantId));

        encounter.removeParticipant(participant);
        encounterRepository.save(encounter);
    }

    // Reason operations

    @Transactional(readOnly = true)
    public List<EncounterReasonDto> getReasons(UUID encounterId) {
        findEncounterOrThrow(encounterId);
        return reasonRepository.findByEncounterId(encounterId).stream()
                .map(EncounterReasonDto::from)
                .toList();
    }

    public EncounterReasonDto addReason(UUID encounterId, AddReasonRequest request) {
        Encounter encounter = findEncounterOrThrow(encounterId);

        EncounterReason reason = new EncounterReason(request.type());
        reason.setCode(request.code());
        reason.setCodeSystem(request.codeSystem());
        reason.setDisplayText(request.displayText());
        if (request.isPrimary() != null) {
            reason.setPrimary(request.isPrimary());
        }

        encounter.addReason(reason);
        encounterRepository.save(encounter);

        return EncounterReasonDto.from(reason);
    }

    public void removeReason(UUID encounterId, UUID reasonId) {
        Encounter encounter = findEncounterOrThrow(encounterId);
        EncounterReason reason = reasonRepository.findById(reasonId)
                .orElseThrow(() -> new ResourceNotFoundException("Reason", reasonId));

        encounter.removeReason(reason);
        encounterRepository.save(encounter);
    }

    private Encounter findEncounterOrThrow(UUID encounterId) {
        return encounterRepository.findById(encounterId)
                .orElseThrow(() -> new EncounterNotFoundException(encounterId));
    }
}
