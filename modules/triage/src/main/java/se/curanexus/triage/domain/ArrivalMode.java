package se.curanexus.triage.domain;

/**
 * How the patient arrived at the emergency department.
 */
public enum ArrivalMode {
    AMBULANCE,
    WALK_IN,
    POLICE,
    HELICOPTER,
    REFERRAL,
    OTHER
}
