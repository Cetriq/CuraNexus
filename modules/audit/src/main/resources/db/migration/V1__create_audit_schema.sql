-- Audit Module Schema
-- Version: 1
-- Description: Creates tables for audit events, access logs, change logs, and security events

-- Audit Events table (general audit trail)
CREATE TABLE audit_events (
    id UUID PRIMARY KEY,
    event_type VARCHAR(30) NOT NULL,
    user_id UUID NOT NULL,
    username VARCHAR(100),
    resource_type VARCHAR(30) NOT NULL,
    resource_id UUID,
    patient_id UUID,
    encounter_id UUID,
    action VARCHAR(100),
    details TEXT,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    care_relation_id UUID,
    reason VARCHAR(500),
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_audit_event_type CHECK (event_type IN ('ACCESS', 'CREATE', 'READ', 'UPDATE', 'DELETE', 'SIGN', 'EXPORT', 'PRINT', 'LOGIN', 'LOGOUT', 'LOGIN_FAILED', 'PERMISSION_DENIED', 'EMERGENCY_ACCESS')),
    CONSTRAINT chk_audit_resource_type CHECK (resource_type IN ('PATIENT', 'ENCOUNTER', 'NOTE', 'DIAGNOSIS', 'PROCEDURE', 'OBSERVATION', 'TASK', 'USER', 'ROLE'))
);

CREATE INDEX idx_audit_event_user ON audit_events(user_id);
CREATE INDEX idx_audit_event_patient ON audit_events(patient_id);
CREATE INDEX idx_audit_event_resource ON audit_events(resource_type, resource_id);
CREATE INDEX idx_audit_event_timestamp ON audit_events(timestamp);
CREATE INDEX idx_audit_event_type ON audit_events(event_type);

-- Access Logs table (PDL-compliant patient data access tracking)
CREATE TABLE access_logs (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    username VARCHAR(100),
    patient_id UUID NOT NULL,
    resource_type VARCHAR(30) NOT NULL,
    resource_id UUID,
    access_type VARCHAR(20) NOT NULL,
    care_relation_id UUID,
    care_relation_type VARCHAR(30),
    reason VARCHAR(500),
    ip_address VARCHAR(45),
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_access_resource_type CHECK (resource_type IN ('PATIENT', 'ENCOUNTER', 'NOTE', 'DIAGNOSIS', 'PROCEDURE', 'OBSERVATION', 'TASK', 'USER', 'ROLE')),
    CONSTRAINT chk_access_type CHECK (access_type IN ('READ', 'WRITE', 'EXPORT', 'PRINT'))
);

CREATE INDEX idx_access_log_user ON access_logs(user_id);
CREATE INDEX idx_access_log_patient ON access_logs(patient_id);
CREATE INDEX idx_access_log_timestamp ON access_logs(timestamp);
CREATE INDEX idx_access_log_resource ON access_logs(resource_type, resource_id);

-- Change Logs table (data modification tracking)
CREATE TABLE change_logs (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    username VARCHAR(100),
    resource_type VARCHAR(30) NOT NULL,
    resource_id UUID NOT NULL,
    patient_id UUID,
    change_type VARCHAR(20) NOT NULL,
    field_name VARCHAR(100),
    old_value TEXT,
    new_value TEXT,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_change_resource_type CHECK (resource_type IN ('PATIENT', 'ENCOUNTER', 'NOTE', 'DIAGNOSIS', 'PROCEDURE', 'OBSERVATION', 'TASK', 'USER', 'ROLE')),
    CONSTRAINT chk_change_type CHECK (change_type IN ('CREATE', 'UPDATE', 'DELETE', 'STATUS_CHANGE', 'SIGN'))
);

CREATE INDEX idx_change_log_user ON change_logs(user_id);
CREATE INDEX idx_change_log_resource ON change_logs(resource_type, resource_id);
CREATE INDEX idx_change_log_timestamp ON change_logs(timestamp);
CREATE INDEX idx_change_log_patient ON change_logs(patient_id);

-- Security Events table (authentication and authorization events)
CREATE TABLE security_events (
    id UUID PRIMARY KEY,
    user_id UUID,
    username VARCHAR(100),
    event_type VARCHAR(30) NOT NULL,
    success BOOLEAN NOT NULL,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    details TEXT,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_security_event_type CHECK (event_type IN ('LOGIN', 'LOGOUT', 'LOGIN_FAILED', 'PASSWORD_CHANGE', 'PERMISSION_DENIED', 'ROLE_ASSIGNED', 'ROLE_REMOVED', 'EMERGENCY_ACCESS', 'CONSENT_OVERRIDE'))
);

CREATE INDEX idx_security_event_user ON security_events(user_id);
CREATE INDEX idx_security_event_type ON security_events(event_type);
CREATE INDEX idx_security_event_timestamp ON security_events(timestamp);
CREATE INDEX idx_security_event_success ON security_events(success);

-- Comments
COMMENT ON TABLE audit_events IS 'General audit trail for all system events';
COMMENT ON TABLE access_logs IS 'PDL-compliant patient data access logging';
COMMENT ON TABLE change_logs IS 'Data modification tracking with before/after values';
COMMENT ON TABLE security_events IS 'Authentication and authorization event logging';

COMMENT ON COLUMN access_logs.care_relation_id IS 'Reference to the care relation that authorized this access';
COMMENT ON COLUMN access_logs.care_relation_type IS 'Type of care relation (PRIMARY_CARE, SPECIALIST, etc.)';
COMMENT ON COLUMN change_logs.old_value IS 'Value before the change (may be masked for sensitive fields)';
COMMENT ON COLUMN change_logs.new_value IS 'Value after the change (may be masked for sensitive fields)';
