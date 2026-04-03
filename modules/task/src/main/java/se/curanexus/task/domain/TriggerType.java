package se.curanexus.task.domain;

/**
 * Types of triggers that can activate a task template.
 */
public enum TriggerType {
    /**
     * Triggered based on encounter class (INPATIENT, OUTPATIENT, EMERGENCY, etc.)
     */
    ENCOUNTER_CLASS,

    /**
     * Triggered based on encounter type (specific encounter type codes)
     */
    ENCOUNTER_TYPE,

    /**
     * Always triggered regardless of encounter attributes
     */
    ALWAYS
}
