-- Consent module schema for CuraNexus
-- Implements patient consent management according to Swedish healthcare regulations

-- Consents table
CREATE TABLE consents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id UUID NOT NULL,
    type VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    description VARCHAR(1000),
    scope VARCHAR(500),
    managing_unit_id UUID,
    managing_unit_name VARCHAR(200),
    given_at TIMESTAMP WITH TIME ZONE,
    given_by UUID,
    given_by_name VARCHAR(200),
    representative_relation VARCHAR(100),
    collection_method VARCHAR(50),
    valid_from DATE,
    valid_until DATE,
    withdrawn_at TIMESTAMP WITH TIME ZONE,
    withdrawal_reason VARCHAR(500),
    recorded_by UUID,
    recorded_by_name VARCHAR(200),
    document_reference VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Access blocks table (spärr)
CREATE TABLE access_blocks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id UUID NOT NULL,
    block_type VARCHAR(30) NOT NULL,
    blocked_unit_id UUID,
    blocked_unit_name VARCHAR(200),
    blocked_practitioner_id UUID,
    blocked_practitioner_name VARCHAR(200),
    blocked_data_category VARCHAR(100),
    reason VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    valid_from DATE,
    valid_until DATE,
    requested_by UUID,
    requested_by_name VARCHAR(200),
    requested_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deactivated_at TIMESTAMP WITH TIME ZONE,
    deactivated_by UUID,
    deactivation_reason VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for consents
CREATE INDEX idx_consents_patient_id ON consents(patient_id);
CREATE INDEX idx_consents_type ON consents(type);
CREATE INDEX idx_consents_status ON consents(status);
CREATE INDEX idx_consents_patient_type ON consents(patient_id, type);
CREATE INDEX idx_consents_patient_status ON consents(patient_id, status);
CREATE INDEX idx_consents_managing_unit ON consents(managing_unit_id);
CREATE INDEX idx_consents_valid_dates ON consents(valid_from, valid_until) WHERE status = 'ACTIVE';

-- Indexes for access blocks
CREATE INDEX idx_access_blocks_patient_id ON access_blocks(patient_id);
CREATE INDEX idx_access_blocks_block_type ON access_blocks(block_type);
CREATE INDEX idx_access_blocks_active ON access_blocks(active);
CREATE INDEX idx_access_blocks_patient_active ON access_blocks(patient_id, active);
CREATE INDEX idx_access_blocks_blocked_unit ON access_blocks(blocked_unit_id) WHERE active = TRUE;
CREATE INDEX idx_access_blocks_blocked_practitioner ON access_blocks(blocked_practitioner_id) WHERE active = TRUE;
CREATE INDEX idx_access_blocks_valid_dates ON access_blocks(valid_from, valid_until) WHERE active = TRUE;

-- Comments
COMMENT ON TABLE consents IS 'Patient consent records for various healthcare purposes';
COMMENT ON COLUMN consents.type IS 'Type of consent: TREATMENT, DATA_SHARING, RESEARCH, etc.';
COMMENT ON COLUMN consents.status IS 'Consent status: ACTIVE, WITHDRAWN, EXPIRED, PENDING, REJECTED';
COMMENT ON COLUMN consents.scope IS 'Specific scope or context for this consent';
COMMENT ON COLUMN consents.representative_relation IS 'If given by representative, their relation to patient';
COMMENT ON COLUMN consents.collection_method IS 'How consent was collected: VERBAL, WRITTEN, ELECTRONIC';

COMMENT ON TABLE access_blocks IS 'Patient-initiated access restrictions (spärr) per PDL';
COMMENT ON COLUMN access_blocks.block_type IS 'Type of block: UNIT, PRACTITIONER, DATA_CATEGORY, EXTERNAL_UNITS';
COMMENT ON COLUMN access_blocks.blocked_unit_id IS 'ID of blocked healthcare unit';
COMMENT ON COLUMN access_blocks.blocked_practitioner_id IS 'ID of blocked practitioner';
COMMENT ON COLUMN access_blocks.blocked_data_category IS 'Category of data to block';
