-- Medication Module Schema
-- Cura Nexus Healthcare Platform

-- Läkemedelsregister (referensdata)
CREATE TABLE medications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    npl_id VARCHAR(50) UNIQUE,
    npl_pack_id VARCHAR(50),
    atc_code VARCHAR(10),
    name VARCHAR(200) NOT NULL,
    generic_name VARCHAR(200),
    manufacturer VARCHAR(200),
    strength VARCHAR(100),
    strength_value NUMERIC(18, 4),
    strength_unit VARCHAR(20),
    dosage_form VARCHAR(30),
    route VARCHAR(30),
    package_size INTEGER,
    package_unit VARCHAR(20),
    is_narcotic BOOLEAN DEFAULT FALSE,
    narcotic_class VARCHAR(10),
    prescription_required BOOLEAN DEFAULT TRUE,
    is_substitutable BOOLEAN DEFAULT TRUE,
    abuse_potential BOOLEAN DEFAULT FALSE,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_medication_npl_id ON medications(npl_id);
CREATE INDEX idx_medication_atc_code ON medications(atc_code);
CREATE INDEX idx_medication_name ON medications(name);
CREATE INDEX idx_medication_generic_name ON medications(generic_name);
CREATE INDEX idx_medication_active ON medications(active);

-- Ordinationer/recept
CREATE TABLE prescriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id UUID NOT NULL,
    encounter_id UUID,
    medication_id UUID REFERENCES medications(id),
    medication_text VARCHAR(500),
    atc_code VARCHAR(10),
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    indication VARCHAR(500),
    route VARCHAR(30),
    dosage_instruction VARCHAR(1000),
    dose_quantity NUMERIC(18, 4),
    dose_unit VARCHAR(30),
    frequency INTEGER,
    frequency_period_hours INTEGER,
    as_needed BOOLEAN DEFAULT FALSE,
    max_dose_per_day NUMERIC(18, 4),
    start_date DATE,
    end_date DATE,
    duration_days INTEGER,
    dispense_quantity INTEGER,
    number_of_repeats INTEGER,
    substitution_not_allowed BOOLEAN DEFAULT FALSE,
    substitution_reason VARCHAR(500),
    prescriber_id UUID NOT NULL,
    prescriber_hsa_id VARCHAR(50),
    prescriber_name VARCHAR(200),
    prescriber_code VARCHAR(20),
    unit_id UUID,
    unit_hsa_id VARCHAR(50),
    pharmacy_note VARCHAR(500),
    internal_note VARCHAR(1000),
    superseded_prescription_id UUID,
    discontinuation_reason VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    activated_at TIMESTAMP WITH TIME ZONE,
    discontinued_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_prescription_patient ON prescriptions(patient_id);
CREATE INDEX idx_prescription_prescriber ON prescriptions(prescriber_id);
CREATE INDEX idx_prescription_encounter ON prescriptions(encounter_id);
CREATE INDEX idx_prescription_status ON prescriptions(status);
CREATE INDEX idx_prescription_medication ON prescriptions(medication_id);
CREATE INDEX idx_prescription_atc_code ON prescriptions(atc_code);
CREATE INDEX idx_prescription_start_date ON prescriptions(start_date);
CREATE INDEX idx_prescription_end_date ON prescriptions(end_date);

-- Läkemedelsadministrering
CREATE TABLE medication_administrations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id UUID NOT NULL,
    prescription_id UUID REFERENCES prescriptions(id),
    encounter_id UUID,
    status VARCHAR(30) NOT NULL DEFAULT 'PLANNED',
    scheduled_at TIMESTAMP WITHOUT TIME ZONE,
    administered_at TIMESTAMP WITHOUT TIME ZONE,
    dose_quantity NUMERIC(18, 4),
    dose_unit VARCHAR(30),
    route VARCHAR(30),
    body_site VARCHAR(100),
    method VARCHAR(100),
    rate_quantity NUMERIC(18, 4),
    rate_unit VARCHAR(30),
    performer_id UUID,
    performer_hsa_id VARCHAR(50),
    performer_name VARCHAR(200),
    not_given_reason VARCHAR(500),
    notes VARCHAR(1000),
    lot_number VARCHAR(50),
    expiration_date TIMESTAMP WITHOUT TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_admin_patient ON medication_administrations(patient_id);
CREATE INDEX idx_admin_prescription ON medication_administrations(prescription_id);
CREATE INDEX idx_admin_encounter ON medication_administrations(encounter_id);
CREATE INDEX idx_admin_administered_at ON medication_administrations(administered_at);
CREATE INDEX idx_admin_scheduled_at ON medication_administrations(scheduled_at);
CREATE INDEX idx_admin_status ON medication_administrations(status);
CREATE INDEX idx_admin_performer ON medication_administrations(performer_id);

-- Läkemedelsallergier
CREATE TABLE drug_allergies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id UUID NOT NULL,
    medication_id UUID,
    atc_code VARCHAR(10),
    substance_name VARCHAR(200) NOT NULL,
    reaction_type VARCHAR(30) NOT NULL,
    severity VARCHAR(20),
    reaction_description VARCHAR(1000),
    onset_date DATE,
    verified BOOLEAN DEFAULT FALSE,
    verified_by_id UUID,
    verified_at TIMESTAMP WITH TIME ZONE,
    source VARCHAR(50),
    active BOOLEAN DEFAULT TRUE,
    recorded_by_id UUID,
    recorded_by_name VARCHAR(200),
    notes VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_allergy_patient ON drug_allergies(patient_id);
CREATE INDEX idx_allergy_atc ON drug_allergies(atc_code);
CREATE INDEX idx_allergy_active ON drug_allergies(active);
CREATE INDEX idx_allergy_substance ON drug_allergies(substance_name);

-- Läkemedelsinteraktioner (referensdata)
CREATE TABLE medication_interactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    medication_id_1 UUID,
    medication_id_2 UUID,
    atc_code_1 VARCHAR(10),
    atc_code_2 VARCHAR(10),
    severity VARCHAR(20) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    clinical_effect VARCHAR(1000),
    recommendation VARCHAR(1000),
    evidence_level VARCHAR(5),
    source VARCHAR(50),
    active BOOLEAN DEFAULT TRUE
);

CREATE INDEX idx_interaction_med1 ON medication_interactions(medication_id_1);
CREATE INDEX idx_interaction_med2 ON medication_interactions(medication_id_2);
CREATE INDEX idx_interaction_atc1 ON medication_interactions(atc_code_1);
CREATE INDEX idx_interaction_atc2 ON medication_interactions(atc_code_2);
CREATE INDEX idx_interaction_severity ON medication_interactions(severity);
CREATE INDEX idx_interaction_active ON medication_interactions(active);

-- Kommentarer
COMMENT ON TABLE medications IS 'Läkemedelsregister - referensdata från NPL/FASS';
COMMENT ON TABLE prescriptions IS 'Ordinationer och recept';
COMMENT ON TABLE medication_administrations IS 'Registrering av läkemedelsadministrering';
COMMENT ON TABLE drug_allergies IS 'Patienters läkemedelsallergier och överkänslighet';
COMMENT ON TABLE medication_interactions IS 'Läkemedelsinteraktioner - referensdata';

COMMENT ON COLUMN medications.npl_id IS 'Nationellt Produkt Register ID';
COMMENT ON COLUMN medications.atc_code IS 'Anatomical Therapeutic Chemical klassifikation';
COMMENT ON COLUMN medications.narcotic_class IS 'Narkotikaklass (II-V) enligt svenska regler';
COMMENT ON COLUMN prescriptions.status IS 'DRAFT, ACTIVE, ON_HOLD, COMPLETED, CANCELLED, SUPERSEDED, ENTERED_IN_ERROR';
COMMENT ON COLUMN prescriptions.as_needed IS 'PRN - pro re nata (vid behov)';
COMMENT ON COLUMN medication_administrations.status IS 'PLANNED, IN_PROGRESS, COMPLETED, NOT_DONE, STOPPED, ENTERED_IN_ERROR';
COMMENT ON COLUMN drug_allergies.reaction_type IS 'ALLERGY, INTOLERANCE, SIDE_EFFECT, UNKNOWN';
COMMENT ON COLUMN drug_allergies.severity IS 'MILD, MODERATE, SEVERE, LIFE_THREATENING';
COMMENT ON COLUMN medication_interactions.severity IS 'CONTRAINDICATED, SEVERE, MODERATE, MINOR, INFORMATIONAL';
