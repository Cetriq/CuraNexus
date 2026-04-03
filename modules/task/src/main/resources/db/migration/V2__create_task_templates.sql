-- Task Templates Schema
-- Version: 2
-- Description: Creates tables for configurable task templates

-- Task templates table
CREATE TABLE IF NOT EXISTS task_templates (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(2000),
    category VARCHAR(30) NOT NULL,
    default_priority VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    trigger_type VARCHAR(50) NOT NULL,
    trigger_value VARCHAR(100),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT chk_template_priority CHECK (default_priority IN ('LOW', 'NORMAL', 'HIGH', 'URGENT')),
    CONSTRAINT chk_template_category CHECK (category IN ('CLINICAL', 'ADMINISTRATIVE', 'DOCUMENTATION', 'FOLLOW_UP', 'REFERRAL', 'LAB', 'IMAGING', 'MEDICATION')),
    CONSTRAINT chk_template_trigger_type CHECK (trigger_type IN ('ENCOUNTER_CLASS', 'ENCOUNTER_TYPE', 'ALWAYS'))
);

CREATE INDEX IF NOT EXISTS idx_template_trigger ON task_templates(trigger_type, trigger_value);
CREATE INDEX IF NOT EXISTS idx_template_active ON task_templates(active);

-- Insert default templates based on existing hardcoded logic

-- ALWAYS templates (for all encounters)
INSERT INTO task_templates (id, name, title, category, default_priority, trigger_type, trigger_value, sort_order, active, created_at)
VALUES
    ('10000000-0000-0000-0000-000000000001', 'documentation-always', 'Complete encounter documentation', 'DOCUMENTATION', 'NORMAL', 'ALWAYS', NULL, 100, TRUE, NOW())
ON CONFLICT (name) DO NOTHING;

-- INPATIENT templates
INSERT INTO task_templates (id, name, title, category, default_priority, trigger_type, trigger_value, sort_order, active, created_at)
VALUES
    ('10000000-0000-0000-0000-000000000002', 'inpatient-assessment', 'Perform initial assessment', 'CLINICAL', 'HIGH', 'ENCOUNTER_CLASS', 'INPATIENT', 10, TRUE, NOW()),
    ('10000000-0000-0000-0000-000000000003', 'inpatient-medication', 'Review medication list', 'CLINICAL', 'HIGH', 'ENCOUNTER_CLASS', 'INPATIENT', 20, TRUE, NOW()),
    ('10000000-0000-0000-0000-000000000004', 'inpatient-discharge', 'Plan discharge', 'ADMINISTRATIVE', 'LOW', 'ENCOUNTER_CLASS', 'INPATIENT', 30, TRUE, NOW())
ON CONFLICT (name) DO NOTHING;

-- EMERGENCY templates
INSERT INTO task_templates (id, name, title, category, default_priority, trigger_type, trigger_value, sort_order, active, created_at)
VALUES
    ('10000000-0000-0000-0000-000000000005', 'emergency-triage', 'Triage patient', 'CLINICAL', 'URGENT', 'ENCOUNTER_CLASS', 'EMERGENCY', 10, TRUE, NOW()),
    ('10000000-0000-0000-0000-000000000006', 'emergency-vitals', 'Record vital signs', 'CLINICAL', 'HIGH', 'ENCOUNTER_CLASS', 'EMERGENCY', 20, TRUE, NOW())
ON CONFLICT (name) DO NOTHING;

-- OUTPATIENT templates
INSERT INTO task_templates (id, name, title, category, default_priority, trigger_type, trigger_value, sort_order, active, created_at)
VALUES
    ('10000000-0000-0000-0000-000000000007', 'outpatient-history', 'Review patient history', 'CLINICAL', 'NORMAL', 'ENCOUNTER_CLASS', 'OUTPATIENT', 10, TRUE, NOW())
ON CONFLICT (name) DO NOTHING;

-- Comments
COMMENT ON TABLE task_templates IS 'Templates for automatic task creation based on triggers';
COMMENT ON COLUMN task_templates.trigger_type IS 'Type of trigger: ENCOUNTER_CLASS, ENCOUNTER_TYPE, ALWAYS';
COMMENT ON COLUMN task_templates.trigger_value IS 'Value to match for trigger (e.g., INPATIENT, EMERGENCY)';
COMMENT ON COLUMN task_templates.sort_order IS 'Order in which tasks should be created (lower = first)';
