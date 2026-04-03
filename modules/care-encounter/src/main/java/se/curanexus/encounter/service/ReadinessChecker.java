package se.curanexus.encounter.service;

import se.curanexus.encounter.api.dto.EncounterReadinessDto;

import java.util.UUID;

/**
 * Interface for checking encounter readiness for completion.
 */
public interface ReadinessChecker {

    /**
     * Check if an encounter is ready to be marked as FINISHED.
     */
    EncounterReadinessDto checkReadiness(UUID encounterId);

    /**
     * Validate that encounter can be finished.
     * Throws exception if not ready.
     */
    void validateCanFinish(UUID encounterId);
}
