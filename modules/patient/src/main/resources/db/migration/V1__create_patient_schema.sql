-- Patient Module Schema (A1)
-- Version: 1
-- Description: Initial schema for patient identity and contact management

-- Patients table
CREATE TABLE patients (
    id UUID PRIMARY KEY,
    personal_identity_number VARCHAR(12) NOT NULL UNIQUE,
    given_name VARCHAR(100) NOT NULL,
    family_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),
    date_of_birth DATE,
    gender VARCHAR(20),
    protected_identity BOOLEAN NOT NULL DEFAULT FALSE,
    deceased BOOLEAN NOT NULL DEFAULT FALSE,
    deceased_date DATE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_patient_personal_identity_number ON patients(personal_identity_number);
CREATE INDEX idx_patient_name ON patients(family_name, given_name);

-- Contact information table
CREATE TABLE contact_info (
    id UUID PRIMARY KEY,
    patient_id UUID NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    type VARCHAR(20) NOT NULL,
    value VARCHAR(255) NOT NULL,
    contact_use VARCHAR(20),
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    valid_from DATE,
    valid_to DATE
);

CREATE INDEX idx_contact_patient ON contact_info(patient_id);

-- Related persons table (next of kin, guardians)
CREATE TABLE related_persons (
    id UUID PRIMARY KEY,
    patient_id UUID NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    relationship VARCHAR(20) NOT NULL,
    personal_identity_number VARCHAR(12),
    given_name VARCHAR(100) NOT NULL,
    family_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(255),
    is_emergency_contact BOOLEAN NOT NULL DEFAULT FALSE,
    is_legal_guardian BOOLEAN NOT NULL DEFAULT FALSE,
    valid_from DATE,
    valid_to DATE
);

CREATE INDEX idx_related_person_patient ON related_persons(patient_id);

-- Consents table
CREATE TABLE consents (
    id UUID PRIMARY KEY,
    patient_id UUID NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    type VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL,
    given_at TIMESTAMP WITH TIME ZONE NOT NULL,
    given_by VARCHAR(255),
    revoked_at TIMESTAMP WITH TIME ZONE,
    valid_from DATE,
    valid_to DATE,
    scope VARCHAR(500)
);

CREATE INDEX idx_consent_patient ON consents(patient_id);
CREATE INDEX idx_consent_status ON consents(status);

-- Add comment for documentation
COMMENT ON TABLE patients IS 'Core patient identity table for module A1';
COMMENT ON COLUMN patients.personal_identity_number IS 'Swedish personnummer (12 digits)';
COMMENT ON COLUMN patients.protected_identity IS 'Indicates skyddade personuppgifter';
