-- Authorization Module Schema
-- Version: 1
-- Description: Creates tables for users, roles, permissions, and care relations

-- Users table
CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(200) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    title VARCHAR(100),
    department VARCHAR(100),
    user_type VARCHAR(20) NOT NULL,
    hsa_id VARCHAR(50),
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT chk_user_type CHECK (user_type IN ('INTERNAL', 'EXTERNAL'))
);

CREATE INDEX idx_user_username ON users(username);
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_hsa_id ON users(hsa_id);
CREATE INDEX idx_user_active ON users(active);

-- Roles table
CREATE TABLE roles (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(500),
    system_role BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_role_code ON roles(code);

-- Permissions table
CREATE TABLE permissions (
    id UUID PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    resource VARCHAR(30) NOT NULL,
    action VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_permission_resource CHECK (resource IN ('PATIENT', 'ENCOUNTER', 'NOTE', 'DIAGNOSIS', 'PROCEDURE', 'OBSERVATION', 'TASK', 'USER', 'ROLE')),
    CONSTRAINT chk_permission_action CHECK (action IN ('CREATE', 'READ', 'UPDATE', 'DELETE', 'SIGN', 'APPROVE', 'DELEGATE'))
);

CREATE INDEX idx_permission_code ON permissions(code);
CREATE INDEX idx_permission_resource ON permissions(resource);

-- User-Role mapping
CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    assigned_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    valid_from TIMESTAMP WITH TIME ZONE,
    valid_until TIMESTAMP WITH TIME ZONE,
    PRIMARY KEY (user_id, role_id)
);

CREATE INDEX idx_user_roles_user ON user_roles(user_id);
CREATE INDEX idx_user_roles_role ON user_roles(role_id);

-- Role-Permission mapping
CREATE TABLE role_permissions (
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

CREATE INDEX idx_role_permissions_role ON role_permissions(role_id);
CREATE INDEX idx_role_permissions_permission ON role_permissions(permission_id);

-- Care Relations table
CREATE TABLE care_relations (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    patient_id UUID NOT NULL,
    encounter_id UUID,
    relation_type VARCHAR(30) NOT NULL,
    valid_from TIMESTAMP NOT NULL,
    valid_until TIMESTAMP,
    reason VARCHAR(500),
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    ended_at TIMESTAMP WITH TIME ZONE,
    ended_by_id UUID,
    CONSTRAINT chk_care_relation_type CHECK (relation_type IN ('PRIMARY_CARE', 'SPECIALIST', 'NURSE', 'THERAPIST', 'EMERGENCY'))
);

CREATE INDEX idx_care_relation_user ON care_relations(user_id);
CREATE INDEX idx_care_relation_patient ON care_relations(patient_id);
CREATE INDEX idx_care_relation_encounter ON care_relations(encounter_id);
CREATE INDEX idx_care_relation_active ON care_relations(active);

-- Comments
COMMENT ON TABLE users IS 'System users with authentication and profile information';
COMMENT ON TABLE roles IS 'Named collections of permissions (RBAC)';
COMMENT ON TABLE permissions IS 'Fine-grained access rights for resources and actions';
COMMENT ON TABLE user_roles IS 'Mapping between users and their assigned roles';
COMMENT ON TABLE role_permissions IS 'Mapping between roles and their permissions';
COMMENT ON TABLE care_relations IS 'Patient-provider care relationships for context-based access (ABAC)';

COMMENT ON COLUMN users.hsa_id IS 'Swedish HSA-ID for healthcare professionals';
COMMENT ON COLUMN roles.system_role IS 'True for built-in roles that cannot be deleted';
COMMENT ON COLUMN care_relations.relation_type IS 'Type of care relationship: PRIMARY_CARE, SPECIALIST, NURSE, THERAPIST, EMERGENCY';
