package se.curanexus.journal.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.curanexus.journal.api.dto.NoteSummaryResponse;
import se.curanexus.journal.service.JournalService;

import java.util.UUID;

/**
 * Controller for encounter-related note endpoints.
 * Provides endpoints that match the expected URL pattern for cross-module communication.
 */
@RestController
@RequestMapping("/api/v1/encounters")
public class EncounterNotesController {

    private final JournalService journalService;

    public EncounterNotesController(JournalService journalService) {
        this.journalService = journalService;
    }

    @GetMapping("/{encounterId}/notes/summary")
    public ResponseEntity<NoteSummaryResponse> getNoteSummary(@PathVariable UUID encounterId) {
        JournalService.NoteSummary summary = journalService.getNoteSummaryByEncounter(encounterId);
        return ResponseEntity.ok(new NoteSummaryResponse(
                summary.total(),
                summary.signed(),
                summary.unsigned(),
                summary.unsignedNoteTitles()
        ));
    }
}
