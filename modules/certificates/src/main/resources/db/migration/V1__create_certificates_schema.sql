-- Certificates Module Schema
-- V1: Initial schema for certificate templates and certificates

-- Certificate Templates table
CREATE TABLE certificate_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    type VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    version INTEGER NOT NULL DEFAULT 1,
    data_schema TEXT,
    render_template TEXT,
    recipient_system VARCHAR(50),
    requires_signature BOOLEAN NOT NULL DEFAULT true,
    validity_days INTEGER,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP WITH TIME ZONE
);

-- Index for template searches
CREATE INDEX idx_cert_templates_code ON certificate_templates(code);
CREATE INDEX idx_cert_templates_type ON certificate_templates(type);
CREATE INDEX idx_cert_templates_status ON certificate_templates(status);

-- Certificates table
CREATE TABLE certificates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    template_id UUID NOT NULL REFERENCES certificate_templates(id),
    certificate_number VARCHAR(50) NOT NULL UNIQUE,
    patient_id UUID NOT NULL,
    encounter_id UUID,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    data TEXT NOT NULL DEFAULT '{}',
    period_start DATE,
    period_end DATE,
    diagnosis_codes VARCHAR(200),
    diagnosis_description VARCHAR(500),
    issuer_id UUID NOT NULL,
    issuer_name VARCHAR(200),
    issuer_role VARCHAR(50),
    issuer_unit_id UUID,
    issuer_unit_name VARCHAR(200),
    signed_at TIMESTAMP WITH TIME ZONE,
    signature TEXT,
    sent_at TIMESTAMP WITH TIME ZONE,
    recipient_tracking_id VARCHAR(100),
    revocation_reason VARCHAR(500),
    revoked_at TIMESTAMP WITH TIME ZONE,
    replaces_id UUID REFERENCES certificates(id),
    replaced_by_id UUID REFERENCES certificates(id),
    rendered_pdf_ref VARCHAR(500),
    valid_until DATE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index for certificate searches
CREATE INDEX idx_certificates_number ON certificates(certificate_number);
CREATE INDEX idx_certificates_patient ON certificates(patient_id);
CREATE INDEX idx_certificates_encounter ON certificates(encounter_id);
CREATE INDEX idx_certificates_issuer ON certificates(issuer_id);
CREATE INDEX idx_certificates_status ON certificates(status);
CREATE INDEX idx_certificates_template ON certificates(template_id);
CREATE INDEX idx_certificates_signed_at ON certificates(signed_at);
CREATE INDEX idx_certificates_valid_until ON certificates(valid_until);

-- Comments for documentation
COMMENT ON TABLE certificate_templates IS 'Mallar för olika typer av intyg';
COMMENT ON TABLE certificates IS 'Utfärdade intyg för patienter';

COMMENT ON COLUMN certificate_templates.type IS 'Typ: SICK_LEAVE, FK_7804, FK_7800, DEATH_CERTIFICATE, DRIVING_LICENSE, etc.';
COMMENT ON COLUMN certificate_templates.status IS 'Status: DRAFT, ACTIVE, DEPRECATED, ARCHIVED';
COMMENT ON COLUMN certificate_templates.data_schema IS 'JSON Schema för intygsdatan';
COMMENT ON COLUMN certificate_templates.recipient_system IS 'Mottagarsystem: INTYGSTJANSTEN, FORSAKRINGSKASSAN, etc.';

COMMENT ON COLUMN certificates.status IS 'Status: DRAFT, SIGNED, SENT, REVOKED, REPLACED, ARCHIVED';
COMMENT ON COLUMN certificates.data IS 'Intygsdata som JSON enligt mallens schema';
COMMENT ON COLUMN certificates.signature IS 'Elektronisk signatur (BankID eller liknande)';

-- Insert default templates for common Swedish medical certificates
INSERT INTO certificate_templates (code, name, description, type, status, recipient_system, validity_days) VALUES
('FK7804', 'Läkarintyg för sjukpenning', 'Intyg till Försäkringskassan vid sjukskrivning', 'FK_7804', 'ACTIVE', 'FORSAKRINGSKASSAN', NULL),
('FK7800', 'Läkarutlåtande för aktivitetsersättning', 'Utlåtande för aktivitetsersättning vid nedsatt arbetsförmåga', 'FK_7800', 'ACTIVE', 'FORSAKRINGSKASSAN', NULL),
('DODSBEVIS', 'Dödsbevis', 'Intyg om dödsfall', 'DEATH_CERTIFICATE', 'ACTIVE', 'SKATTEVERKET', NULL),
('KORKORT', 'Läkarintyg för körkort', 'Medicinskt underlag för körkortstillstånd', 'DRIVING_LICENSE', 'ACTIVE', 'TRANSPORTSTYRELSEN', 365),
('HALSO', 'Hälsointyg', 'Allmänt hälsointyg', 'HEALTH', 'ACTIVE', NULL, 90),
('VACCINATION', 'Vaccinationsintyg', 'Intyg om genomförd vaccination', 'VACCINATION', 'ACTIVE', NULL, NULL);
