-- Task Module Schema
-- Version: 1
-- Description: Creates tables for tasks, reminders, delegations, and watches

-- Tasks table
CREATE TABLE tasks (
    id UUID PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(2000),
    category VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    priority VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    patient_id UUID,
    encounter_id UUID,
    assignee_id UUID,
    created_by_id UUID NOT NULL,
    source_type VARCHAR(50),
    source_id UUID,
    due_at TIMESTAMP,
    started_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    completion_note VARCHAR(1000),
    outcome VARCHAR(200),
    cancel_reason VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT chk_task_status CHECK (status IN ('PENDING', 'ASSIGNED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'BLOCKED')),
    CONSTRAINT chk_task_priority CHECK (priority IN ('LOW', 'NORMAL', 'HIGH', 'URGENT')),
    CONSTRAINT chk_task_category CHECK (category IN ('CLINICAL', 'ADMINISTRATIVE', 'DOCUMENTATION', 'FOLLOW_UP', 'REFERRAL', 'LAB', 'IMAGING', 'MEDICATION'))
);

CREATE INDEX idx_task_assignee ON tasks(assignee_id);
CREATE INDEX idx_task_patient ON tasks(patient_id);
CREATE INDEX idx_task_encounter ON tasks(encounter_id);
CREATE INDEX idx_task_status ON tasks(status);
CREATE INDEX idx_task_due ON tasks(due_at);
CREATE INDEX idx_task_created_by ON tasks(created_by_id);

-- Reminders table
CREATE TABLE reminders (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    message VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    remind_at TIMESTAMP NOT NULL,
    patient_id UUID,
    encounter_id UUID,
    task_id UUID REFERENCES tasks(id),
    recurring BOOLEAN DEFAULT FALSE,
    recurrence_pattern VARCHAR(100),
    acknowledged_at TIMESTAMP WITH TIME ZONE,
    snoozed_until TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_reminder_status CHECK (status IN ('PENDING', 'TRIGGERED', 'ACKNOWLEDGED', 'SNOOZED', 'CANCELLED'))
);

CREATE INDEX idx_reminder_user ON reminders(user_id);
CREATE INDEX idx_reminder_remind_at ON reminders(remind_at);
CREATE INDEX idx_reminder_status ON reminders(status);
CREATE INDEX idx_reminder_task ON reminders(task_id);

-- Delegations table
CREATE TABLE delegations (
    id UUID PRIMARY KEY,
    from_user_id UUID NOT NULL,
    to_user_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    valid_from TIMESTAMP NOT NULL,
    valid_until TIMESTAMP NOT NULL,
    scope VARCHAR(200),
    note VARCHAR(500),
    revoked_at TIMESTAMP WITH TIME ZONE,
    revoked_by_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_delegation_status CHECK (status IN ('ACTIVE', 'EXPIRED', 'REVOKED')),
    CONSTRAINT chk_delegation_dates CHECK (valid_until > valid_from)
);

CREATE INDEX idx_delegation_from ON delegations(from_user_id);
CREATE INDEX idx_delegation_to ON delegations(to_user_id);
CREATE INDEX idx_delegation_status ON delegations(status);
CREATE INDEX idx_delegation_valid ON delegations(valid_from, valid_until);

-- Watches table
CREATE TABLE watches (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    watch_type VARCHAR(30) NOT NULL,
    target_id UUID NOT NULL,
    notify_on_change BOOLEAN DEFAULT TRUE,
    note VARCHAR(500),
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    last_notified_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT chk_watch_type CHECK (watch_type IN ('PATIENT', 'ENCOUNTER', 'LAB_RESULT', 'REFERRAL', 'TASK'))
);

CREATE INDEX idx_watch_user ON watches(user_id);
CREATE INDEX idx_watch_target ON watches(watch_type, target_id);
CREATE INDEX idx_watch_active ON watches(active);

-- Comments for documentation
COMMENT ON TABLE tasks IS 'Work items assigned to users, linked to patients and encounters';
COMMENT ON TABLE reminders IS 'Time-based notifications for users';
COMMENT ON TABLE delegations IS 'Temporary transfer of responsibilities between users';
COMMENT ON TABLE watches IS 'User subscriptions to monitor entities for changes';

COMMENT ON COLUMN tasks.source_type IS 'Type of entity that triggered this task (e.g., REFERRAL, LAB_ORDER)';
COMMENT ON COLUMN tasks.source_id IS 'ID of the entity that triggered this task';
COMMENT ON COLUMN reminders.recurrence_pattern IS 'Cron-like pattern for recurring reminders';
COMMENT ON COLUMN delegations.scope IS 'Scope of delegation (e.g., all tasks, specific category)';
