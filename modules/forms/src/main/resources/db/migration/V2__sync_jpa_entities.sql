-- V2: Sync database schema with JPA entities
-- Fixes column name mismatches and adds missing columns
-- All operations are idempotent to handle cases where Hibernate ddl-auto has already created the schema

-- Rename review_notes to reviewer_notes (if old column exists)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_name = 'form_submissions' AND column_name = 'review_notes') THEN
        ALTER TABLE form_submissions RENAME COLUMN review_notes TO reviewer_notes;
    END IF;
END $$;

-- Rename calculated_score to total_score (if old column exists)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_name = 'form_submissions' AND column_name = 'calculated_score') THEN
        ALTER TABLE form_submissions RENAME COLUMN calculated_score TO total_score;
    END IF;
END $$;

-- Add score_interpretation column (exists in JPA but might be missing)
ALTER TABLE form_submissions ADD COLUMN IF NOT EXISTS score_interpretation VARCHAR(500);

-- Add reviewer_notes if it doesn't exist (in case neither old nor new column exists)
ALTER TABLE form_submissions ADD COLUMN IF NOT EXISTS reviewer_notes TEXT;

-- Add total_score if it doesn't exist
ALTER TABLE form_submissions ADD COLUMN IF NOT EXISTS total_score DOUBLE PRECISION;

-- Handle submitted_at -> completed_at migration
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_name = 'form_submissions' AND column_name = 'submitted_at') THEN
        -- Copy submitted_at to completed_at if completed_at doesn't exist
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_name = 'form_submissions' AND column_name = 'completed_at') THEN
            ALTER TABLE form_submissions ADD COLUMN completed_at TIMESTAMP WITH TIME ZONE;
            UPDATE form_submissions SET completed_at = submitted_at WHERE status = 'COMPLETED' OR status = 'REVIEWED';
        END IF;
        -- Drop submitted_at as it's now replaced by completed_at
        ALTER TABLE form_submissions DROP COLUMN IF EXISTS submitted_at;
    END IF;
END $$;

-- Add started_at if it doesn't exist
ALTER TABLE form_submissions ADD COLUMN IF NOT EXISTS started_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Add completed_at if it doesn't exist
ALTER TABLE form_submissions ADD COLUMN IF NOT EXISTS completed_at TIMESTAMP WITH TIME ZONE;

-- Remove reviewed_by_role as it doesn't exist in JPA entity
ALTER TABLE form_submissions DROP COLUMN IF EXISTS reviewed_by_role;

-- Update comments (these are always safe to run)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_name = 'form_submissions' AND column_name = 'template_code') THEN
        COMMENT ON COLUMN form_submissions.template_code IS 'Denormalized from template for query optimization - derived from template_id';
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_name = 'form_submissions' AND column_name = 'template_version') THEN
        COMMENT ON COLUMN form_submissions.template_version IS 'Denormalized from template for query optimization - derived from template_id';
    END IF;
END $$;

COMMENT ON COLUMN form_submissions.started_at IS 'When the submission was started';
COMMENT ON COLUMN form_submissions.total_score IS 'Calculated total score based on scoring formula';
COMMENT ON COLUMN form_submissions.score_interpretation IS 'Human-readable interpretation of the score';

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_name = 'form_submissions' AND column_name = 'completed_at') THEN
        COMMENT ON COLUMN form_submissions.completed_at IS 'When the submission was completed';
    END IF;
END $$;
