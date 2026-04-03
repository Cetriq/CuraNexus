-- Task Dependencies and Due Date Enhancements
-- Version: 3
-- Description: Adds task dependencies and escalation support

-- Add dependency column to tasks
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS depends_on_task_id UUID REFERENCES tasks(id);

-- Add escalation tracking
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS escalated BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS escalated_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS escalation_level INTEGER DEFAULT 0;

-- Add template reference for traceability
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS template_id UUID REFERENCES task_templates(id);

-- Index for dependency lookups
CREATE INDEX IF NOT EXISTS idx_task_depends_on ON tasks(depends_on_task_id);
CREATE INDEX IF NOT EXISTS idx_task_escalated ON tasks(escalated) WHERE escalated = TRUE;

-- Add dependency support to task templates
ALTER TABLE task_templates ADD COLUMN IF NOT EXISTS depends_on_template VARCHAR(100);
ALTER TABLE task_templates ADD COLUMN IF NOT EXISTS due_offset_minutes INTEGER;

-- Update existing templates with due offsets (minutes from encounter start)
-- EMERGENCY tasks should have short due times
UPDATE task_templates SET due_offset_minutes = 15 WHERE name = 'emergency-triage';
UPDATE task_templates SET due_offset_minutes = 30 WHERE name = 'emergency-vitals';

-- INPATIENT tasks have longer timelines
UPDATE task_templates SET due_offset_minutes = 60 WHERE name = 'inpatient-assessment';
UPDATE task_templates SET due_offset_minutes = 120 WHERE name = 'inpatient-medication';
UPDATE task_templates SET due_offset_minutes = 1440 WHERE name = 'inpatient-discharge'; -- 24 hours

-- OUTPATIENT
UPDATE task_templates SET due_offset_minutes = 30 WHERE name = 'outpatient-history';

-- Documentation always due within 4 hours
UPDATE task_templates SET due_offset_minutes = 240 WHERE name = 'documentation-always';

-- Set up template dependencies
-- Vital signs depends on triage for emergency
UPDATE task_templates SET depends_on_template = 'emergency-triage' WHERE name = 'emergency-vitals';

-- Medication review depends on initial assessment for inpatient
UPDATE task_templates SET depends_on_template = 'inpatient-assessment' WHERE name = 'inpatient-medication';

-- Comments
COMMENT ON COLUMN tasks.depends_on_task_id IS 'Task that must be completed before this task can start';
COMMENT ON COLUMN tasks.escalated IS 'Whether this task has been escalated due to overdue status';
COMMENT ON COLUMN tasks.escalation_level IS 'Number of times this task has been escalated';
COMMENT ON COLUMN tasks.template_id IS 'Reference to the template used to create this task';
COMMENT ON COLUMN task_templates.depends_on_template IS 'Name of template that must complete before this one';
COMMENT ON COLUMN task_templates.due_offset_minutes IS 'Minutes from encounter start when task is due';
