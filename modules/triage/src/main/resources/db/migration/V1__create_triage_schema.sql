-- Triage Module Schema
-- A4 Triage and Assessment Module for Cura Nexus
-- Based on RETTS (Rapid Emergency Triage and Treatment System)

-- Triage assessments
CREATE TABLE triage_assessments (
    id UUID PRIMARY KEY,
    patient_id UUID NOT NULL,
    encounter_id UUID NOT NULL,
    triage_nurse_id UUID NOT NULL,
    triage_nurse_name VARCHAR(200),
    chief_complaint VARCHAR(500) NOT NULL,
    priority VARCHAR(20),
    care_level VARCHAR(20),
    disposition VARCHAR(30),
    status VARCHAR(25) NOT NULL,
    arrival_mode VARCHAR(20),
    location_id UUID,
    notes TEXT,
    arrival_time TIMESTAMP WITH TIME ZONE NOT NULL,
    triage_start_time TIMESTAMP WITH TIME ZONE,
    triage_end_time TIMESTAMP WITH TIME ZONE,
    recommended_protocol_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_triage_patient ON triage_assessments(patient_id);
CREATE INDEX idx_triage_encounter ON triage_assessments(encounter_id);
CREATE INDEX idx_triage_priority ON triage_assessments(priority);
CREATE INDEX idx_triage_status ON triage_assessments(status);
CREATE INDEX idx_triage_arrival ON triage_assessments(arrival_time);
CREATE INDEX idx_triage_location ON triage_assessments(location_id);

-- Symptoms recorded during triage
CREATE TABLE symptoms (
    id UUID PRIMARY KEY,
    assessment_id UUID NOT NULL REFERENCES triage_assessments(id) ON DELETE CASCADE,
    symptom_code VARCHAR(20) NOT NULL,
    description VARCHAR(500) NOT NULL,
    onset TIMESTAMP WITH TIME ZONE,
    duration VARCHAR(50),
    severity VARCHAR(20),
    body_location VARCHAR(100),
    is_chief_complaint BOOLEAN NOT NULL DEFAULT FALSE,
    recorded_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_symptom_assessment ON symptoms(assessment_id);
CREATE INDEX idx_symptom_code ON symptoms(symptom_code);

-- Vital signs recorded during triage
CREATE TABLE vital_signs (
    id UUID PRIMARY KEY,
    assessment_id UUID NOT NULL UNIQUE REFERENCES triage_assessments(id) ON DELETE CASCADE,
    blood_pressure_systolic INTEGER,
    blood_pressure_diastolic INTEGER,
    heart_rate INTEGER,
    respiratory_rate INTEGER,
    temperature DOUBLE PRECISION,
    oxygen_saturation INTEGER,
    pain_level INTEGER,
    consciousness_level VARCHAR(15),
    glucose_level DOUBLE PRECISION,
    recorded_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    recorded_by UUID
);

CREATE INDEX idx_vital_signs_assessment ON vital_signs(assessment_id);

-- Escalation history
CREATE TABLE escalation_records (
    id UUID PRIMARY KEY,
    assessment_id UUID NOT NULL REFERENCES triage_assessments(id) ON DELETE CASCADE,
    previous_priority VARCHAR(20),
    new_priority VARCHAR(20) NOT NULL,
    reason VARCHAR(500),
    escalated_by UUID NOT NULL,
    escalated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_escalation_assessment ON escalation_records(assessment_id);

-- Triage protocols (RETTS-based)
CREATE TABLE triage_protocols (
    id UUID PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    category VARCHAR(100),
    version VARCHAR(20),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_protocol_code ON triage_protocols(code);
CREATE INDEX idx_protocol_category ON triage_protocols(category);
CREATE INDEX idx_protocol_active ON triage_protocols(active);

-- Protocol steps
CREATE TABLE protocol_steps (
    id UUID PRIMARY KEY,
    protocol_id UUID NOT NULL REFERENCES triage_protocols(id) ON DELETE CASCADE,
    step_order INTEGER NOT NULL,
    instruction TEXT NOT NULL,
    assessment_criteria TEXT
);

CREATE INDEX idx_step_protocol ON protocol_steps(protocol_id);

-- Protocol step actions
CREATE TABLE step_actions (
    step_id UUID NOT NULL REFERENCES protocol_steps(id) ON DELETE CASCADE,
    action VARCHAR(500) NOT NULL
);

-- Protocol red flags
CREATE TABLE protocol_red_flags (
    protocol_id UUID NOT NULL REFERENCES triage_protocols(id) ON DELETE CASCADE,
    red_flag VARCHAR(500) NOT NULL
);

-- Add constraints for enum values
ALTER TABLE triage_assessments
    ADD CONSTRAINT chk_priority CHECK (priority IN ('IMMEDIATE', 'EMERGENT', 'URGENT', 'LESS_URGENT', 'NON_URGENT')),
    ADD CONSTRAINT chk_care_level CHECK (care_level IN ('INTENSIVE_CARE', 'EMERGENCY_CARE', 'INPATIENT_CARE', 'OUTPATIENT_CARE', 'PRIMARY_CARE', 'SELF_CARE')),
    ADD CONSTRAINT chk_disposition CHECK (disposition IN ('ADMIT', 'OBSERVE', 'TREAT_AND_RELEASE', 'REFER', 'DISCHARGE', 'TRANSFER', 'LEFT_WITHOUT_BEING_SEEN')),
    ADD CONSTRAINT chk_status CHECK (status IN ('IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'REASSESSMENT_NEEDED')),
    ADD CONSTRAINT chk_arrival_mode CHECK (arrival_mode IN ('AMBULANCE', 'WALK_IN', 'POLICE', 'HELICOPTER', 'REFERRAL', 'OTHER'));

ALTER TABLE symptoms
    ADD CONSTRAINT chk_severity CHECK (severity IN ('MILD', 'MODERATE', 'SEVERE', 'CRITICAL'));

ALTER TABLE vital_signs
    ADD CONSTRAINT chk_consciousness CHECK (consciousness_level IN ('ALERT', 'VERBAL', 'PAIN', 'UNRESPONSIVE')),
    ADD CONSTRAINT chk_bp_systolic CHECK (blood_pressure_systolic IS NULL OR (blood_pressure_systolic >= 0 AND blood_pressure_systolic <= 300)),
    ADD CONSTRAINT chk_bp_diastolic CHECK (blood_pressure_diastolic IS NULL OR (blood_pressure_diastolic >= 0 AND blood_pressure_diastolic <= 200)),
    ADD CONSTRAINT chk_heart_rate CHECK (heart_rate IS NULL OR (heart_rate >= 0 AND heart_rate <= 300)),
    ADD CONSTRAINT chk_resp_rate CHECK (respiratory_rate IS NULL OR (respiratory_rate >= 0 AND respiratory_rate <= 60)),
    ADD CONSTRAINT chk_temp CHECK (temperature IS NULL OR (temperature >= 30.0 AND temperature <= 45.0)),
    ADD CONSTRAINT chk_spo2 CHECK (oxygen_saturation IS NULL OR (oxygen_saturation >= 0 AND oxygen_saturation <= 100)),
    ADD CONSTRAINT chk_pain CHECK (pain_level IS NULL OR (pain_level >= 0 AND pain_level <= 10));

ALTER TABLE escalation_records
    ADD CONSTRAINT chk_prev_priority CHECK (previous_priority IS NULL OR previous_priority IN ('IMMEDIATE', 'EMERGENT', 'URGENT', 'LESS_URGENT', 'NON_URGENT')),
    ADD CONSTRAINT chk_new_priority CHECK (new_priority IN ('IMMEDIATE', 'EMERGENT', 'URGENT', 'LESS_URGENT', 'NON_URGENT'));
