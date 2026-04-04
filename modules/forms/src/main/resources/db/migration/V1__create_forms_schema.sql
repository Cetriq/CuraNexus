-- Forms Module Schema
-- V1: Initial schema for form templates, fields, submissions and answers

-- Form Templates table
CREATE TABLE form_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL,
    version INTEGER NOT NULL DEFAULT 1,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    category VARCHAR(100),
    estimated_duration_minutes INTEGER,
    instructions VARCHAR(2000),
    scoring_formula VARCHAR(1000),
    owner_unit_id UUID,
    created_by UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP WITH TIME ZONE,
    deprecated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uk_form_template_code_version UNIQUE (code, version)
);

-- Index for template searches
CREATE INDEX idx_form_templates_code ON form_templates(code);
CREATE INDEX idx_form_templates_type ON form_templates(type);
CREATE INDEX idx_form_templates_status ON form_templates(status);
CREATE INDEX idx_form_templates_category ON form_templates(category);
CREATE INDEX idx_form_templates_owner_unit ON form_templates(owner_unit_id);

-- Form Fields table
CREATE TABLE form_fields (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    template_id UUID NOT NULL REFERENCES form_templates(id) ON DELETE CASCADE,
    field_key VARCHAR(100) NOT NULL,
    field_type VARCHAR(50) NOT NULL,
    label VARCHAR(500) NOT NULL,
    description VARCHAR(1000),
    placeholder VARCHAR(200),
    help_text VARCHAR(500),
    sort_order INTEGER NOT NULL DEFAULT 0,
    required BOOLEAN NOT NULL DEFAULT false,
    read_only BOOLEAN NOT NULL DEFAULT false,
    hidden BOOLEAN NOT NULL DEFAULT false,
    default_value VARCHAR(500),
    options TEXT,
    validation_rules TEXT,
    conditional_rules TEXT,
    min_value INTEGER,
    max_value INTEGER,
    step_value INTEGER,
    scale_labels VARCHAR(500),
    code_system VARCHAR(50),
    code VARCHAR(50),
    unit VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_form_field_template_key UNIQUE (template_id, field_key)
);

-- Index for field lookups
CREATE INDEX idx_form_fields_template ON form_fields(template_id);
CREATE INDEX idx_form_fields_key ON form_fields(field_key);

-- Form Submissions table
CREATE TABLE form_submissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    template_id UUID NOT NULL REFERENCES form_templates(id),
    template_code VARCHAR(50) NOT NULL,
    template_version INTEGER NOT NULL,
    patient_id UUID NOT NULL,
    encounter_id UUID,
    status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    submitted_by UUID,
    submitted_by_role VARCHAR(50),
    submitted_at TIMESTAMP WITH TIME ZONE,
    reviewed_by UUID,
    reviewed_by_role VARCHAR(50),
    reviewed_at TIMESTAMP WITH TIME ZONE,
    review_notes VARCHAR(2000),
    calculated_score DOUBLE PRECISION,
    source VARCHAR(50),
    ip_address VARCHAR(50),
    expires_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index for submission searches
CREATE INDEX idx_form_submissions_template ON form_submissions(template_id);
CREATE INDEX idx_form_submissions_patient ON form_submissions(patient_id);
CREATE INDEX idx_form_submissions_encounter ON form_submissions(encounter_id);
CREATE INDEX idx_form_submissions_status ON form_submissions(status);
CREATE INDEX idx_form_submissions_template_code ON form_submissions(template_code);
CREATE INDEX idx_form_submissions_submitted_at ON form_submissions(submitted_at);

-- Form Answers table
CREATE TABLE form_answers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    submission_id UUID NOT NULL REFERENCES form_submissions(id) ON DELETE CASCADE,
    field_key VARCHAR(100) NOT NULL,
    field_type VARCHAR(50) NOT NULL,
    value_text TEXT,
    value_number DOUBLE PRECISION,
    value_boolean BOOLEAN,
    value_datetime TIMESTAMP WITH TIME ZONE,
    value_array TEXT,
    file_reference VARCHAR(500),
    code_system VARCHAR(50),
    code VARCHAR(50),
    code_display VARCHAR(200),
    answered_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_form_answer_submission_field UNIQUE (submission_id, field_key)
);

-- Index for answer lookups
CREATE INDEX idx_form_answers_submission ON form_answers(submission_id);
CREATE INDEX idx_form_answers_field_key ON form_answers(field_key);

-- Comments for documentation
COMMENT ON TABLE form_templates IS 'Formulärmallar med versionering';
COMMENT ON TABLE form_fields IS 'Fält i formulärmallar';
COMMENT ON TABLE form_submissions IS 'Patientinlämningar av formulär';
COMMENT ON TABLE form_answers IS 'Svar på enskilda fält i inlämningar';

COMMENT ON COLUMN form_templates.type IS 'Typ av formulär: ANAMNESIS, SCREENING, PROM, PREM, CONSENT, etc.';
COMMENT ON COLUMN form_templates.status IS 'Status: DRAFT, ACTIVE, DEPRECATED, ARCHIVED';
COMMENT ON COLUMN form_templates.scoring_formula IS 'Formel för poängberäkning, t.ex. SUM(field1, field2)';

COMMENT ON COLUMN form_fields.field_type IS 'Fälttyp: TEXT, NUMBER, DATE, BOOLEAN, SINGLE_CHOICE, MULTIPLE_CHOICE, SCALE, VAS, etc.';
COMMENT ON COLUMN form_fields.options IS 'JSON-array med svarsalternativ för choice-fält';
COMMENT ON COLUMN form_fields.validation_rules IS 'JSON med valideringsregler';
COMMENT ON COLUMN form_fields.conditional_rules IS 'JSON med villkor för när fältet visas';

COMMENT ON COLUMN form_submissions.status IS 'Status: IN_PROGRESS, COMPLETED, REVIEWED, CANCELLED, EXPIRED';
COMMENT ON COLUMN form_submissions.source IS 'Källa: WEB, MOBILE, KIOSK, IMPORT';
