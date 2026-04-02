-- Journal Module Schema
-- Version: 1
-- Description: Creates tables for clinical notes, diagnoses, procedures, and observations

-- Clinical Notes table
CREATE TABLE clinical_notes (
    id UUID PRIMARY KEY,
    encounter_id UUID NOT NULL,
    patient_id UUID NOT NULL,
    type VARCHAR(30) NOT NULL,
    title VARCHAR(200),
    content TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    author_id UUID NOT NULL,
    author_name VARCHAR(200) NOT NULL,
    signed_by_id UUID,
    signed_by_name VARCHAR(200),
    signed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT chk_note_status CHECK (status IN ('DRAFT', 'FINAL', 'AMENDED', 'CANCELLED')),
    CONSTRAINT chk_note_type CHECK (type IN ('ADMISSION', 'PROGRESS', 'CONSULTATION', 'DISCHARGE', 'PROCEDURE', 'OPERATIVE', 'NURSING', 'REFERRAL'))
);

CREATE INDEX idx_clinical_note_encounter ON clinical_notes(encounter_id);
CREATE INDEX idx_clinical_note_patient ON clinical_notes(patient_id);
CREATE INDEX idx_clinical_note_author ON clinical_notes(author_id);
CREATE INDEX idx_clinical_note_type ON clinical_notes(type);
CREATE INDEX idx_clinical_note_status ON clinical_notes(status);

-- Diagnoses table (ICD-10-SE)
CREATE TABLE diagnoses (
    id UUID PRIMARY KEY,
    encounter_id UUID NOT NULL,
    patient_id UUID NOT NULL,
    code VARCHAR(20) NOT NULL,
    code_system VARCHAR(50) NOT NULL DEFAULT 'ICD-10-SE',
    display_text VARCHAR(500),
    type VARCHAR(20),
    rank INTEGER,
    onset_date DATE,
    resolved_date DATE,
    recorded_at TIMESTAMP WITH TIME ZONE NOT NULL,
    recorded_by_id UUID,
    CONSTRAINT chk_diagnosis_type CHECK (type IS NULL OR type IN ('PRINCIPAL', 'SECONDARY', 'COMPLICATION', 'DIFFERENTIAL'))
);

CREATE INDEX idx_diagnosis_encounter ON diagnoses(encounter_id);
CREATE INDEX idx_diagnosis_patient ON diagnoses(patient_id);
CREATE INDEX idx_diagnosis_code ON diagnoses(code);

-- Procedures table (KVÅ)
CREATE TABLE procedures (
    id UUID PRIMARY KEY,
    encounter_id UUID NOT NULL,
    patient_id UUID NOT NULL,
    code VARCHAR(20) NOT NULL,
    code_system VARCHAR(50) NOT NULL DEFAULT 'KVÅ',
    display_text VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'PLANNED',
    performed_at TIMESTAMP,
    performed_by_id UUID,
    performed_by_name VARCHAR(200),
    body_site VARCHAR(100),
    laterality VARCHAR(20),
    outcome VARCHAR(500),
    notes VARCHAR(2000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT chk_procedure_status CHECK (status IN ('PLANNED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'))
);

CREATE INDEX idx_procedure_encounter ON procedures(encounter_id);
CREATE INDEX idx_procedure_patient ON procedures(patient_id);
CREATE INDEX idx_procedure_code ON procedures(code);

-- Observations table (vital signs, lab results, assessments)
CREATE TABLE observations (
    id UUID PRIMARY KEY,
    encounter_id UUID,
    patient_id UUID NOT NULL,
    code VARCHAR(50) NOT NULL,
    code_system VARCHAR(50),
    display_text VARCHAR(500),
    category VARCHAR(30) NOT NULL,
    value_numeric NUMERIC(18, 4),
    value_string VARCHAR(1000),
    value_boolean BOOLEAN,
    unit VARCHAR(50),
    reference_range_low NUMERIC(18, 4),
    reference_range_high NUMERIC(18, 4),
    interpretation VARCHAR(30),
    observed_at TIMESTAMP NOT NULL,
    recorded_at TIMESTAMP WITH TIME ZONE NOT NULL,
    recorded_by_id UUID,
    recorded_by_name VARCHAR(200),
    method VARCHAR(200),
    body_site VARCHAR(100),
    device VARCHAR(200),
    notes VARCHAR(1000),
    CONSTRAINT chk_observation_category CHECK (category IN ('VITAL_SIGNS', 'LABORATORY', 'IMAGING', 'ASSESSMENT', 'SOCIAL_HISTORY')),
    CONSTRAINT chk_observation_interpretation CHECK (interpretation IS NULL OR interpretation IN ('NORMAL', 'LOW', 'HIGH', 'CRITICAL_LOW', 'CRITICAL_HIGH', 'ABNORMAL', 'POSITIVE', 'NEGATIVE'))
);

CREATE INDEX idx_observation_encounter ON observations(encounter_id);
CREATE INDEX idx_observation_patient ON observations(patient_id);
CREATE INDEX idx_observation_code ON observations(code);
CREATE INDEX idx_observation_category ON observations(category);

-- Comments for documentation
COMMENT ON TABLE clinical_notes IS 'Clinical notes written during patient encounters';
COMMENT ON TABLE diagnoses IS 'Patient diagnoses using ICD-10-SE coding system';
COMMENT ON TABLE procedures IS 'Medical procedures using KVÅ (Swedish classification of health interventions)';
COMMENT ON TABLE observations IS 'Clinical observations including vital signs, lab results, and assessments';

COMMENT ON COLUMN diagnoses.code_system IS 'Default: ICD-10-SE (Swedish ICD-10)';
COMMENT ON COLUMN procedures.code_system IS 'Default: KVÅ (Klassifikation av vårdåtgärder)';
COMMENT ON COLUMN observations.category IS 'Category of observation: VITAL_SIGNS, LABORATORY, IMAGING, ASSESSMENT, SOCIAL_HISTORY';
