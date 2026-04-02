-- Care Encounter Module Schema (A2)
-- Version: 1
-- Description: Initial schema for care encounter and case management

-- Encounters table (vårdkontakter)
CREATE TABLE encounters (
    id UUID PRIMARY KEY,
    patient_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    encounter_class VARCHAR(20) NOT NULL,
    encounter_type VARCHAR(20),
    priority VARCHAR(20),
    service_type VARCHAR(100),
    responsible_unit_id UUID,
    responsible_practitioner_id UUID,
    planned_start_time TIMESTAMP WITH TIME ZONE,
    planned_end_time TIMESTAMP WITH TIME ZONE,
    actual_start_time TIMESTAMP WITH TIME ZONE,
    actual_end_time TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_encounter_patient ON encounters(patient_id);
CREATE INDEX idx_encounter_status ON encounters(status);
CREATE INDEX idx_encounter_class ON encounters(encounter_class);
CREATE INDEX idx_encounter_responsible_unit ON encounters(responsible_unit_id);
CREATE INDEX idx_encounter_planned_start ON encounters(planned_start_time);

-- Participants table
CREATE TABLE participants (
    id UUID PRIMARY KEY,
    encounter_id UUID NOT NULL REFERENCES encounters(id) ON DELETE CASCADE,
    type VARCHAR(20) NOT NULL,
    practitioner_id UUID,
    role VARCHAR(20),
    period_start TIMESTAMP WITH TIME ZONE,
    period_end TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_participant_encounter ON participants(encounter_id);
CREATE INDEX idx_participant_practitioner ON participants(practitioner_id);

-- Encounter reasons table
CREATE TABLE encounter_reasons (
    id UUID PRIMARY KEY,
    encounter_id UUID NOT NULL REFERENCES encounters(id) ON DELETE CASCADE,
    type VARCHAR(30) NOT NULL,
    code VARCHAR(20),
    code_system VARCHAR(50),
    display_text VARCHAR(500),
    is_primary BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_reason_encounter ON encounter_reasons(encounter_id);
CREATE INDEX idx_reason_code ON encounter_reasons(code, code_system);

-- Add comments for documentation
COMMENT ON TABLE encounters IS 'Core encounter table for module A2 - the system hub';
COMMENT ON COLUMN encounters.patient_id IS 'Reference to patient in A1 module';
COMMENT ON COLUMN encounters.status IS 'Encounter lifecycle: PLANNED, ARRIVED, TRIAGED, IN_PROGRESS, ON_HOLD, FINISHED, CANCELLED';
COMMENT ON COLUMN encounters.encounter_class IS 'INPATIENT, OUTPATIENT, EMERGENCY, HOME_VISIT, VIRTUAL';
