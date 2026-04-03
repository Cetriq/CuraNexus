-- Authorization Module - ABAC Tables
-- Version: 3
-- Description: Creates tables for Attribute-Based Access Control

-- Access Policies table
CREATE TABLE IF NOT EXISTS access_policies (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    policy_type VARCHAR(30) NOT NULL,
    resource_type VARCHAR(30),
    action_type VARCHAR(30),
    condition_expression VARCHAR(1000),
    required_role VARCHAR(50),
    required_permission VARCHAR(50),
    require_care_relation BOOLEAN DEFAULT FALSE,
    require_encounter_context BOOLEAN DEFAULT FALSE,
    allowed_user_types VARCHAR(200),
    allowed_departments VARCHAR(500),
    priority INTEGER DEFAULT 0,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_policy_resource_action ON access_policies(resource_type, action_type);
CREATE INDEX IF NOT EXISTS idx_policy_active ON access_policies(active);

-- Access Audit Log table (PDL-compliant access logging)
CREATE TABLE IF NOT EXISTS access_audit_log (
    id UUID PRIMARY KEY,
    decision_id UUID NOT NULL,
    user_id UUID NOT NULL,
    username VARCHAR(100),
    user_type VARCHAR(50),
    department VARCHAR(100),
    patient_id UUID,
    encounter_id UUID,
    resource_type VARCHAR(30),
    resource_id UUID,
    action_type VARCHAR(30),
    outcome VARCHAR(20) NOT NULL,
    granted BOOLEAN NOT NULL,
    reason VARCHAR(1000),
    emergency_access BOOLEAN DEFAULT FALSE,
    access_reason VARCHAR(500),
    client_ip VARCHAR(50),
    client_application VARCHAR(100),
    policies_evaluated VARCHAR(1000),
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_audit_user ON access_audit_log(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_patient ON access_audit_log(patient_id);
CREATE INDEX IF NOT EXISTS idx_audit_timestamp ON access_audit_log(timestamp);
CREATE INDEX IF NOT EXISTS idx_audit_outcome ON access_audit_log(outcome);
CREATE INDEX IF NOT EXISTS idx_audit_emergency ON access_audit_log(emergency_access);
CREATE INDEX IF NOT EXISTS idx_audit_encounter ON access_audit_log(encounter_id);

-- Seed default access policies
-- Policy: Require care relation for patient access
INSERT INTO access_policies (id, name, description, policy_type, resource_type, action_type, require_care_relation, priority, active, created_at)
VALUES (
    gen_random_uuid(),
    'require-patient-care-relation',
    'Requires active care relation for all patient data access',
    'REQUIRE_CONTEXT',
    'PATIENT',
    NULL,
    TRUE,
    100,
    TRUE,
    NOW()
);

-- Policy: Clinical staff can read patient data with care relation
INSERT INTO access_policies (id, name, description, policy_type, resource_type, action_type, required_permission, require_care_relation, priority, active, created_at)
VALUES (
    gen_random_uuid(),
    'clinical-patient-read',
    'Clinical staff can read patient data with care relation',
    'PERMIT',
    'PATIENT',
    'READ',
    'PATIENT_READ',
    TRUE,
    50,
    TRUE,
    NOW()
);

-- Policy: Doctors can update patient data
INSERT INTO access_policies (id, name, description, policy_type, resource_type, action_type, required_role, required_permission, require_care_relation, priority, active, created_at)
VALUES (
    gen_random_uuid(),
    'doctor-patient-update',
    'Doctors can update patient data with care relation',
    'PERMIT',
    'PATIENT',
    'UPDATE',
    'DOCTOR',
    'PATIENT_UPDATE',
    TRUE,
    50,
    TRUE,
    NOW()
);

-- Policy: Encounter context required for notes
INSERT INTO access_policies (id, name, description, policy_type, resource_type, require_encounter_context, require_care_relation, priority, active, created_at)
VALUES (
    gen_random_uuid(),
    'require-encounter-for-notes',
    'Requires encounter context for all note operations',
    'REQUIRE_CONTEXT',
    'NOTE',
    TRUE,
    TRUE,
    100,
    TRUE,
    NOW()
);

-- Policy: Clinical staff can create notes
INSERT INTO access_policies (id, name, description, policy_type, resource_type, action_type, required_permission, require_care_relation, require_encounter_context, priority, active, created_at)
VALUES (
    gen_random_uuid(),
    'clinical-note-create',
    'Clinical staff can create notes within encounter',
    'PERMIT',
    'NOTE',
    'CREATE',
    'NOTE_CREATE',
    TRUE,
    TRUE,
    50,
    TRUE,
    NOW()
);

-- Policy: Only doctors can sign notes
INSERT INTO access_policies (id, name, description, policy_type, resource_type, action_type, required_role, required_permission, require_care_relation, priority, active, created_at)
VALUES (
    gen_random_uuid(),
    'doctor-note-sign',
    'Only doctors can sign notes',
    'PERMIT',
    'NOTE',
    'SIGN',
    'DOCTOR',
    'NOTE_SIGN',
    TRUE,
    50,
    TRUE,
    NOW()
);

-- Policy: Emergency access override (nödåtkomst)
INSERT INTO access_policies (id, name, description, policy_type, resource_type, require_care_relation, priority, active, created_at)
VALUES (
    gen_random_uuid(),
    'emergency-access-override',
    'Emergency access to patient data without care relation - requires reason',
    'EMERGENCY_OVERRIDE',
    'PATIENT',
    FALSE,
    200,
    TRUE,
    NOW()
);

-- Policy: Task access requires care relation to patient
INSERT INTO access_policies (id, name, description, policy_type, resource_type, required_permission, require_care_relation, priority, active, created_at)
VALUES (
    gen_random_uuid(),
    'task-read-care-relation',
    'Task read requires care relation to associated patient',
    'PERMIT',
    'TASK',
    'TASK_READ',
    TRUE,
    50,
    TRUE,
    NOW()
);

-- Policy: Encounter read requires care relation
INSERT INTO access_policies (id, name, description, policy_type, resource_type, action_type, required_permission, require_care_relation, priority, active, created_at)
VALUES (
    gen_random_uuid(),
    'encounter-read-care-relation',
    'Encounter read requires care relation',
    'PERMIT',
    'ENCOUNTER',
    'READ',
    'ENCOUNTER_READ',
    TRUE,
    50,
    TRUE,
    NOW()
);

-- Policy: Only doctors can create diagnoses
INSERT INTO access_policies (id, name, description, policy_type, resource_type, action_type, required_role, required_permission, require_care_relation, require_encounter_context, priority, active, created_at)
VALUES (
    gen_random_uuid(),
    'doctor-diagnosis-create',
    'Only doctors can create diagnoses within encounter',
    'PERMIT',
    'DIAGNOSIS',
    'CREATE',
    'DOCTOR',
    'DIAGNOSIS_CREATE',
    TRUE,
    TRUE,
    50,
    TRUE,
    NOW()
);
