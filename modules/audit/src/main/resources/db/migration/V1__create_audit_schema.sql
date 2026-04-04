-- V1: Create audit schema for PDL-compliant logging
-- This schema is designed to meet Swedish healthcare regulations (Patientdatalagen)

-- Audit events table - main log of all access and actions
CREATE TABLE audit_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    timestamp TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- WHO: User information
    user_id VARCHAR(100) NOT NULL,
    user_hsa_id VARCHAR(50),
    user_name VARCHAR(200),
    user_role VARCHAR(50),
    
    -- WHAT: Action and resource
    action VARCHAR(30) NOT NULL,
    resource_type VARCHAR(30) NOT NULL,
    resource_id UUID,
    resource_description VARCHAR(500),
    
    -- Patient context
    patient_id UUID,
    patient_personnummer_hash VARCHAR(64),
    
    -- WHERE: Location context
    care_unit_id UUID,
    care_unit_name VARCHAR(200),
    care_unit_hsa_id VARCHAR(50),
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    session_id VARCHAR(100),
    
    -- WHY: Care relation context
    encounter_id UUID,
    access_reason VARCHAR(500),
    emergency_access BOOLEAN NOT NULL DEFAULT FALSE,
    consent_reference UUID,
    
    -- Result
    success BOOLEAN NOT NULL DEFAULT TRUE,
    error_message VARCHAR(1000),
    
    -- Additional context
    details TEXT,
    source_system VARCHAR(50),
    correlation_id VARCHAR(100)
);

-- Data change logs - tracks field-level changes
CREATE TABLE data_change_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    audit_event_id UUID,
    timestamp TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resource_type VARCHAR(30) NOT NULL,
    resource_id UUID NOT NULL,
    field_name VARCHAR(100) NOT NULL,
    old_value TEXT,
    new_value TEXT,
    change_type VARCHAR(20) NOT NULL
);

-- Indexes for efficient querying
CREATE INDEX idx_audit_patient ON audit_events(patient_id);
CREATE INDEX idx_audit_user ON audit_events(user_id);
CREATE INDEX idx_audit_timestamp ON audit_events(timestamp);
CREATE INDEX idx_audit_resource ON audit_events(resource_type, resource_id);
CREATE INDEX idx_audit_care_unit ON audit_events(care_unit_id);
CREATE INDEX idx_audit_emergency ON audit_events(emergency_access) WHERE emergency_access = TRUE;
CREATE INDEX idx_audit_failed ON audit_events(success) WHERE success = FALSE;
CREATE INDEX idx_audit_encounter ON audit_events(encounter_id);

CREATE INDEX idx_change_audit_event ON data_change_logs(audit_event_id);
CREATE INDEX idx_change_resource ON data_change_logs(resource_type, resource_id);
CREATE INDEX idx_change_timestamp ON data_change_logs(timestamp);

-- Comments for documentation
COMMENT ON TABLE audit_events IS 'PDL-compliant audit log for all data access and modifications';
COMMENT ON COLUMN audit_events.patient_personnummer_hash IS 'SHA-256 hash of personnummer for privacy-preserving searches';
COMMENT ON COLUMN audit_events.emergency_access IS 'True if access was via nödöppning (emergency override)';
COMMENT ON COLUMN audit_events.consent_reference IS 'Reference to consent that authorized this access';

COMMENT ON TABLE data_change_logs IS 'Field-level change tracking for data modifications';
