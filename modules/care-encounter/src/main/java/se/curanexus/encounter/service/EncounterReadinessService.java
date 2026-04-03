package se.curanexus.encounter.service;

import org.springframework.stereotype.Service;
import se.curanexus.encounter.api.dto.EncounterReadinessDto;
import se.curanexus.encounter.api.dto.EncounterReadinessDto.EncounterProgressDetails;
import se.curanexus.encounter.api.dto.EncounterReadinessDto.EncounterReadinessSummary;
import se.curanexus.encounter.domain.Encounter;
import se.curanexus.encounter.domain.EncounterStatus;
import se.curanexus.encounter.integration.JournalServiceClient;
import se.curanexus.encounter.integration.JournalServiceClient.NoteStatistics;
import se.curanexus.encounter.integration.TaskServiceClient;
import se.curanexus.encounter.integration.TaskServiceClient.TaskStatistics;
import se.curanexus.encounter.repository.EncounterRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service for checking encounter readiness for completion.
 * Validates that all tasks are completed and all notes are signed.
 */
@Service
public class EncounterReadinessService implements ReadinessChecker {

    private final EncounterRepository encounterRepository;
    private final TaskServiceClient taskServiceClient;
    private final JournalServiceClient journalServiceClient;

    public EncounterReadinessService(
            EncounterRepository encounterRepository,
            TaskServiceClient taskServiceClient,
            JournalServiceClient journalServiceClient) {
        this.encounterRepository = encounterRepository;
        this.taskServiceClient = taskServiceClient;
        this.journalServiceClient = journalServiceClient;
    }

    @Override
    public EncounterReadinessDto checkReadiness(UUID encounterId) {
        // Verify encounter exists
        Encounter encounter = encounterRepository.findById(encounterId)
                .orElseThrow(() -> new EncounterNotFoundException(encounterId));

        // If already finished or cancelled, not applicable
        if (encounter.getStatus() == EncounterStatus.FINISHED) {
            return EncounterReadinessDto.ready(encounterId,
                    new EncounterReadinessSummary(0, 0, 0, 0, 0, 0));
        }

        if (encounter.getStatus() == EncounterStatus.CANCELLED) {
            return EncounterReadinessDto.error(encounterId, "Encounter is cancelled");
        }

        // Get statistics from other modules
        TaskStatistics taskStats = taskServiceClient.getTaskStatistics(encounterId);
        NoteStatistics noteStats = journalServiceClient.getNoteStatistics(encounterId);

        // Build summary
        EncounterReadinessSummary summary = new EncounterReadinessSummary(
                taskStats.total(),
                taskStats.completed(),
                taskStats.pending(),
                noteStats.total(),
                noteStats.signed(),
                noteStats.unsigned()
        );

        // Build progress details
        EncounterProgressDetails progress = buildProgressDetails(taskStats, noteStats);

        // Check blockers
        List<String> blockers = new ArrayList<>();

        // Check tasks
        if (taskStats.pending() > 0) {
            blockers.add(String.format("%d pending task(s): %s",
                    taskStats.pending(),
                    String.join(", ", taskStats.pendingTaskTitles().stream().limit(3).toList())));
        }

        // Check notes
        if (noteStats.unsigned() > 0) {
            blockers.add(String.format("%d unsigned note(s): %s",
                    noteStats.unsigned(),
                    String.join(", ", noteStats.unsignedNoteTitles().stream().limit(3).toList())));
        }

        // Check encounter status - must be at least IN_PROGRESS
        if (encounter.getStatus() == EncounterStatus.PLANNED ||
            encounter.getStatus() == EncounterStatus.ARRIVED) {
            blockers.add("Encounter must be IN_PROGRESS before it can be finished");
        }

        if (blockers.isEmpty()) {
            return EncounterReadinessDto.ready(encounterId, summary, progress);
        } else {
            return EncounterReadinessDto.notReady(encounterId, blockers, summary, progress);
        }
    }

    private EncounterProgressDetails buildProgressDetails(TaskStatistics taskStats, NoteStatistics noteStats) {
        // Calculate task completion percentage
        double taskCompletionPercentage = taskStats.total() > 0
                ? (taskStats.completed() * 100.0 / taskStats.total())
                : 100.0;

        // Calculate note completion percentage
        double noteCompletionPercentage = noteStats.total() > 0
                ? (noteStats.signed() * 100.0 / noteStats.total())
                : 100.0;

        // Calculate overall completion (weighted: tasks 70%, notes 30%)
        double overallCompletionPercentage;
        if (taskStats.total() == 0 && noteStats.total() == 0) {
            overallCompletionPercentage = 100.0;
        } else if (taskStats.total() == 0) {
            overallCompletionPercentage = noteCompletionPercentage;
        } else if (noteStats.total() == 0) {
            overallCompletionPercentage = taskCompletionPercentage;
        } else {
            overallCompletionPercentage = (taskCompletionPercentage * 0.7) + (noteCompletionPercentage * 0.3);
        }

        return new EncounterProgressDetails(
                Math.round(overallCompletionPercentage * 10.0) / 10.0,
                Math.round(taskCompletionPercentage * 10.0) / 10.0,
                Math.round(noteCompletionPercentage * 10.0) / 10.0,
                taskStats.progress().blocked(),
                taskStats.progress().overdue(),
                taskStats.progress().escalated(),
                taskStats.progress().nextDueAt(),
                taskStats.progress().overdueTaskTitles(),
                taskStats.progress().blockedTaskTitles()
        );
    }

    @Override
    public void validateCanFinish(UUID encounterId) {
        EncounterReadinessDto readiness = checkReadiness(encounterId);

        if (readiness.status() != EncounterReadinessDto.ReadinessStatus.READY) {
            throw new EncounterNotReadyException(encounterId, readiness.blockers());
        }
    }
}
