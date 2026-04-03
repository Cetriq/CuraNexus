-- Event Store Schema for D4 Notification Module
-- Stores all domain events for audit trail and event replay

CREATE TABLE stored_events (
    id UUID PRIMARY KEY,
    event_id UUID NOT NULL UNIQUE,
    aggregate_id UUID NOT NULL,
    aggregate_type VARCHAR(50) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    payload TEXT NOT NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    stored_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    processed BOOLEAN NOT NULL DEFAULT FALSE,
    processed_at TIMESTAMP WITH TIME ZONE
);

-- Index for querying events by aggregate
CREATE INDEX idx_stored_event_aggregate ON stored_events (aggregate_id, aggregate_type);

-- Index for querying events by type
CREATE INDEX idx_stored_event_type ON stored_events (event_type);

-- Index for querying events by time
CREATE INDEX idx_stored_event_occurred ON stored_events (occurred_at);

-- Index for finding unprocessed events
CREATE INDEX idx_stored_event_processed ON stored_events (processed) WHERE processed = FALSE;

-- Comment on table
COMMENT ON TABLE stored_events IS 'Event store for domain events, supporting audit trail and event replay';
