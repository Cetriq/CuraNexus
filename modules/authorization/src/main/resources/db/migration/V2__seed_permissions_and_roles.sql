-- Authorization Module - Seed Data
-- Version: 2
-- Description: Seeds default permissions and roles

-- Patient permissions
INSERT INTO permissions (id, code, name, resource, action, created_at) VALUES
    (gen_random_uuid(), 'PATIENT_CREATE', 'Create Patient', 'PATIENT', 'CREATE', NOW()),
    (gen_random_uuid(), 'PATIENT_READ', 'Read Patient', 'PATIENT', 'READ', NOW()),
    (gen_random_uuid(), 'PATIENT_UPDATE', 'Update Patient', 'PATIENT', 'UPDATE', NOW()),
    (gen_random_uuid(), 'PATIENT_DELETE', 'Delete Patient', 'PATIENT', 'DELETE', NOW());

-- Encounter permissions
INSERT INTO permissions (id, code, name, resource, action, created_at) VALUES
    (gen_random_uuid(), 'ENCOUNTER_CREATE', 'Create Encounter', 'ENCOUNTER', 'CREATE', NOW()),
    (gen_random_uuid(), 'ENCOUNTER_READ', 'Read Encounter', 'ENCOUNTER', 'READ', NOW()),
    (gen_random_uuid(), 'ENCOUNTER_UPDATE', 'Update Encounter', 'ENCOUNTER', 'UPDATE', NOW()),
    (gen_random_uuid(), 'ENCOUNTER_DELETE', 'Delete Encounter', 'ENCOUNTER', 'DELETE', NOW());

-- Note permissions
INSERT INTO permissions (id, code, name, resource, action, created_at) VALUES
    (gen_random_uuid(), 'NOTE_CREATE', 'Create Note', 'NOTE', 'CREATE', NOW()),
    (gen_random_uuid(), 'NOTE_READ', 'Read Note', 'NOTE', 'READ', NOW()),
    (gen_random_uuid(), 'NOTE_UPDATE', 'Update Note', 'NOTE', 'UPDATE', NOW()),
    (gen_random_uuid(), 'NOTE_DELETE', 'Delete Note', 'NOTE', 'DELETE', NOW()),
    (gen_random_uuid(), 'NOTE_SIGN', 'Sign Note', 'NOTE', 'SIGN', NOW());

-- Diagnosis permissions
INSERT INTO permissions (id, code, name, resource, action, created_at) VALUES
    (gen_random_uuid(), 'DIAGNOSIS_CREATE', 'Create Diagnosis', 'DIAGNOSIS', 'CREATE', NOW()),
    (gen_random_uuid(), 'DIAGNOSIS_READ', 'Read Diagnosis', 'DIAGNOSIS', 'READ', NOW()),
    (gen_random_uuid(), 'DIAGNOSIS_UPDATE', 'Update Diagnosis', 'DIAGNOSIS', 'UPDATE', NOW()),
    (gen_random_uuid(), 'DIAGNOSIS_DELETE', 'Delete Diagnosis', 'DIAGNOSIS', 'DELETE', NOW());

-- Procedure permissions
INSERT INTO permissions (id, code, name, resource, action, created_at) VALUES
    (gen_random_uuid(), 'PROCEDURE_CREATE', 'Create Procedure', 'PROCEDURE', 'CREATE', NOW()),
    (gen_random_uuid(), 'PROCEDURE_READ', 'Read Procedure', 'PROCEDURE', 'READ', NOW()),
    (gen_random_uuid(), 'PROCEDURE_UPDATE', 'Update Procedure', 'PROCEDURE', 'UPDATE', NOW()),
    (gen_random_uuid(), 'PROCEDURE_DELETE', 'Delete Procedure', 'PROCEDURE', 'DELETE', NOW());

-- Observation permissions
INSERT INTO permissions (id, code, name, resource, action, created_at) VALUES
    (gen_random_uuid(), 'OBSERVATION_CREATE', 'Create Observation', 'OBSERVATION', 'CREATE', NOW()),
    (gen_random_uuid(), 'OBSERVATION_READ', 'Read Observation', 'OBSERVATION', 'READ', NOW()),
    (gen_random_uuid(), 'OBSERVATION_UPDATE', 'Update Observation', 'OBSERVATION', 'UPDATE', NOW());

-- Task permissions
INSERT INTO permissions (id, code, name, resource, action, created_at) VALUES
    (gen_random_uuid(), 'TASK_CREATE', 'Create Task', 'TASK', 'CREATE', NOW()),
    (gen_random_uuid(), 'TASK_READ', 'Read Task', 'TASK', 'READ', NOW()),
    (gen_random_uuid(), 'TASK_UPDATE', 'Update Task', 'TASK', 'UPDATE', NOW()),
    (gen_random_uuid(), 'TASK_DELETE', 'Delete Task', 'TASK', 'DELETE', NOW()),
    (gen_random_uuid(), 'TASK_DELEGATE', 'Delegate Task', 'TASK', 'DELEGATE', NOW());

-- User management permissions
INSERT INTO permissions (id, code, name, resource, action, created_at) VALUES
    (gen_random_uuid(), 'USER_CREATE', 'Create User', 'USER', 'CREATE', NOW()),
    (gen_random_uuid(), 'USER_READ', 'Read User', 'USER', 'READ', NOW()),
    (gen_random_uuid(), 'USER_UPDATE', 'Update User', 'USER', 'UPDATE', NOW()),
    (gen_random_uuid(), 'USER_DELETE', 'Delete User', 'USER', 'DELETE', NOW());

-- Role management permissions
INSERT INTO permissions (id, code, name, resource, action, created_at) VALUES
    (gen_random_uuid(), 'ROLE_CREATE', 'Create Role', 'ROLE', 'CREATE', NOW()),
    (gen_random_uuid(), 'ROLE_READ', 'Read Role', 'ROLE', 'READ', NOW()),
    (gen_random_uuid(), 'ROLE_UPDATE', 'Update Role', 'ROLE', 'UPDATE', NOW()),
    (gen_random_uuid(), 'ROLE_DELETE', 'Delete Role', 'ROLE', 'DELETE', NOW());

-- Default roles
INSERT INTO roles (id, name, code, description, system_role, created_at) VALUES
    (gen_random_uuid(), 'System Administrator', 'ADMIN', 'Full system access', TRUE, NOW()),
    (gen_random_uuid(), 'Doctor', 'DOCTOR', 'Licensed physician with full clinical access', TRUE, NOW()),
    (gen_random_uuid(), 'Nurse', 'NURSE', 'Registered nurse with clinical access', TRUE, NOW()),
    (gen_random_uuid(), 'Medical Secretary', 'SECRETARY', 'Administrative staff with limited clinical access', TRUE, NOW()),
    (gen_random_uuid(), 'Lab Technician', 'LAB_TECH', 'Laboratory personnel', TRUE, NOW()),
    (gen_random_uuid(), 'Receptionist', 'RECEPTIONIST', 'Front desk staff with patient scheduling access', TRUE, NOW());

-- Assign permissions to ADMIN role (all permissions)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p WHERE r.code = 'ADMIN';

-- Assign clinical permissions to DOCTOR role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.code = 'DOCTOR'
AND p.code IN (
    'PATIENT_READ', 'PATIENT_UPDATE',
    'ENCOUNTER_CREATE', 'ENCOUNTER_READ', 'ENCOUNTER_UPDATE',
    'NOTE_CREATE', 'NOTE_READ', 'NOTE_UPDATE', 'NOTE_SIGN',
    'DIAGNOSIS_CREATE', 'DIAGNOSIS_READ', 'DIAGNOSIS_UPDATE', 'DIAGNOSIS_DELETE',
    'PROCEDURE_CREATE', 'PROCEDURE_READ', 'PROCEDURE_UPDATE',
    'OBSERVATION_CREATE', 'OBSERVATION_READ', 'OBSERVATION_UPDATE',
    'TASK_CREATE', 'TASK_READ', 'TASK_UPDATE', 'TASK_DELEGATE'
);

-- Assign clinical permissions to NURSE role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.code = 'NURSE'
AND p.code IN (
    'PATIENT_READ',
    'ENCOUNTER_READ', 'ENCOUNTER_UPDATE',
    'NOTE_CREATE', 'NOTE_READ', 'NOTE_UPDATE',
    'DIAGNOSIS_READ',
    'PROCEDURE_READ',
    'OBSERVATION_CREATE', 'OBSERVATION_READ', 'OBSERVATION_UPDATE',
    'TASK_CREATE', 'TASK_READ', 'TASK_UPDATE'
);

-- Assign administrative permissions to SECRETARY role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.code = 'SECRETARY'
AND p.code IN (
    'PATIENT_CREATE', 'PATIENT_READ', 'PATIENT_UPDATE',
    'ENCOUNTER_CREATE', 'ENCOUNTER_READ',
    'TASK_CREATE', 'TASK_READ', 'TASK_UPDATE'
);

-- Assign lab permissions to LAB_TECH role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.code = 'LAB_TECH'
AND p.code IN (
    'PATIENT_READ',
    'OBSERVATION_CREATE', 'OBSERVATION_READ', 'OBSERVATION_UPDATE',
    'TASK_READ', 'TASK_UPDATE'
);

-- Assign reception permissions to RECEPTIONIST role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.code = 'RECEPTIONIST'
AND p.code IN (
    'PATIENT_CREATE', 'PATIENT_READ',
    'ENCOUNTER_CREATE', 'ENCOUNTER_READ',
    'TASK_READ'
);
