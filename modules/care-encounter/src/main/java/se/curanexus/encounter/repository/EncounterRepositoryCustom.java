package se.curanexus.encounter.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import se.curanexus.encounter.domain.Encounter;
import se.curanexus.encounter.domain.EncounterClass;
import se.curanexus.encounter.domain.EncounterStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Custom repository interface for complex Encounter queries.
 * Uses CriteriaBuilder to handle nullable parameters correctly with PostgreSQL.
 */
public interface EncounterRepositoryCustom {

    /**
     * Search encounters with optional filter parameters.
     * All parameters are optional - only non-null values will be used as filters.
     *
     * @param patientId Filter by patient ID
     * @param status Filter by encounter status
     * @param encounterClass Filter by encounter class
     * @param responsibleUnitId Filter by responsible unit
     * @param fromDate Filter encounters with planned start time >= fromDate
     * @param toDate Filter encounters with planned start time <= toDate
     * @param pageable Pagination parameters
     * @return Page of matching encounters
     */
    Page<Encounter> searchEncounters(
            UUID patientId,
            EncounterStatus status,
            EncounterClass encounterClass,
            UUID responsibleUnitId,
            Instant fromDate,
            Instant toDate,
            Pageable pageable);
}
