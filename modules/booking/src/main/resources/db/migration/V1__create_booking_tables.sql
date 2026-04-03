-- Bokningsmodul (A5) - Databastabeller
-- Skapar tabeller för bokningar, scheman, tidsluckor och väntelista

-- Scheman tabell
CREATE TABLE schedules (
    id UUID PRIMARY KEY,
    practitioner_id UUID,
    practitioner_hsa_id VARCHAR(64),
    unit_id UUID,
    unit_hsa_id VARCHAR(64),
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    service_type VARCHAR(100),
    valid_from DATE NOT NULL,
    valid_to DATE,
    default_slot_duration_minutes INTEGER NOT NULL DEFAULT 30,
    active BOOLEAN NOT NULL DEFAULT true,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_schedules_practitioner ON schedules(practitioner_id);
CREATE INDEX idx_schedules_unit ON schedules(unit_id);
CREATE INDEX idx_schedules_practitioner_hsa ON schedules(practitioner_hsa_id);
CREATE INDEX idx_schedules_unit_hsa ON schedules(unit_hsa_id);
CREATE INDEX idx_schedules_active ON schedules(active) WHERE active = true;

-- Schemaregler tabell
CREATE TABLE schedule_rules (
    id UUID PRIMARY KEY,
    schedule_id UUID NOT NULL REFERENCES schedules(id) ON DELETE CASCADE,
    day_of_week VARCHAR(10) NOT NULL, -- MONDAY, TUESDAY, etc.
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    break_start_time TIME,
    break_end_time TIME,
    slot_duration_minutes INTEGER
);

CREATE INDEX idx_schedule_rules_schedule ON schedule_rules(schedule_id);

-- Tidsluckor tabell
CREATE TABLE time_slots (
    id UUID PRIMARY KEY,
    schedule_id UUID NOT NULL REFERENCES schedules(id) ON DELETE CASCADE,
    practitioner_id UUID,
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE NOT NULL,
    duration_minutes INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE', -- AVAILABLE, BOOKED, BLOCKED
    service_type VARCHAR(100),
    appointment_type VARCHAR(30),
    overbookable BOOLEAN NOT NULL DEFAULT false,
    max_overbook INTEGER NOT NULL DEFAULT 0,
    current_bookings INTEGER NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_time_slots_schedule ON time_slots(schedule_id);
CREATE INDEX idx_time_slots_start ON time_slots(start_time);
CREATE INDEX idx_time_slots_status ON time_slots(status);
CREATE INDEX idx_time_slots_schedule_start ON time_slots(schedule_id, start_time);
CREATE INDEX idx_time_slots_available ON time_slots(status, start_time) WHERE status = 'AVAILABLE';

-- Bokningar tabell
CREATE TABLE appointments (
    id UUID PRIMARY KEY,
    booking_reference VARCHAR(20) NOT NULL UNIQUE,
    patient_id UUID NOT NULL,
    practitioner_id UUID,
    practitioner_hsa_id VARCHAR(64),
    unit_id UUID,
    unit_hsa_id VARCHAR(64),
    time_slot_id UUID REFERENCES time_slots(id) ON DELETE SET NULL,
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'BOOKED', -- BOOKED, CONFIRMED, CHECKED_IN, IN_PROGRESS, COMPLETED, CANCELLED, NO_SHOW
    appointment_type VARCHAR(30) NOT NULL DEFAULT 'IN_PERSON', -- IN_PERSON, VIDEO, PHONE, HOME_VISIT
    service_type VARCHAR(100),
    reason_text VARCHAR(500),
    reason_code VARCHAR(50),
    patient_instructions VARCHAR(1000),
    internal_notes VARCHAR(1000),
    cancellation_reason VARCHAR(500),
    reminder_sent BOOLEAN NOT NULL DEFAULT false,
    reminder_sent_at TIMESTAMP WITH TIME ZONE,
    encounter_id UUID,
    booked_by_id UUID NOT NULL,
    booked_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cancelled_by_id UUID,
    cancelled_at TIMESTAMP WITH TIME ZONE,
    cancelled_by_patient BOOLEAN,
    checked_in_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_appointments_reference ON appointments(booking_reference);
CREATE INDEX idx_appointments_patient ON appointments(patient_id);
CREATE INDEX idx_appointments_practitioner ON appointments(practitioner_id);
CREATE INDEX idx_appointments_unit ON appointments(unit_id);
CREATE INDEX idx_appointments_time_slot ON appointments(time_slot_id);
CREATE INDEX idx_appointments_start ON appointments(start_time);
CREATE INDEX idx_appointments_status ON appointments(status);
CREATE INDEX idx_appointments_encounter ON appointments(encounter_id);
CREATE INDEX idx_appointments_patient_status ON appointments(patient_id, status);
CREATE INDEX idx_appointments_practitioner_date ON appointments(practitioner_id, start_time);

-- Väntelista tabell
CREATE TABLE waitlist_entries (
    id UUID PRIMARY KEY,
    patient_id UUID NOT NULL,
    practitioner_id UUID,
    unit_id UUID,
    service_type VARCHAR(100) NOT NULL,
    reason_text TEXT,
    priority VARCHAR(20) NOT NULL DEFAULT 'ROUTINE', -- URGENT, SOON, ROUTINE
    status VARCHAR(20) NOT NULL DEFAULT 'WAITING', -- WAITING, NOTIFIED, BOOKED, CANCELLED, EXPIRED
    preferred_date_from DATE,
    preferred_date_to DATE,
    preferred_time_of_day VARCHAR(20), -- MORNING, AFTERNOON, EVENING, ANY
    booked_appointment_id UUID REFERENCES appointments(id) ON DELETE SET NULL,
    notified_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_waitlist_patient ON waitlist_entries(patient_id);
CREATE INDEX idx_waitlist_practitioner ON waitlist_entries(practitioner_id);
CREATE INDEX idx_waitlist_unit ON waitlist_entries(unit_id);
CREATE INDEX idx_waitlist_status ON waitlist_entries(status);
CREATE INDEX idx_waitlist_service ON waitlist_entries(service_type);
CREATE INDEX idx_waitlist_priority_created ON waitlist_entries(priority DESC, created_at ASC);
CREATE INDEX idx_waitlist_waiting ON waitlist_entries(status, priority, created_at) WHERE status = 'WAITING';

-- Kommentarer för dokumentation
COMMENT ON TABLE schedules IS 'Vårdgivarscheman med arbetstider';
COMMENT ON TABLE schedule_rules IS 'Regler för scheman per veckodag';
COMMENT ON TABLE time_slots IS 'Bokningsbara tidsluckor genererade från scheman';
COMMENT ON TABLE appointments IS 'Patientbokningar';
COMMENT ON TABLE waitlist_entries IS 'Väntelista för patienter som väntar på tid';

COMMENT ON COLUMN appointments.booking_reference IS 'Unik bokningsreferens i format CNB{4chars}-{YYYYMMDD}';
COMMENT ON COLUMN appointments.status IS 'Bokningsstatus: BOOKED, CONFIRMED, CHECKED_IN, IN_PROGRESS, COMPLETED, CANCELLED, NO_SHOW';
COMMENT ON COLUMN appointments.appointment_type IS 'Typ av besök: IN_PERSON, VIDEO, PHONE, HOME_VISIT';
COMMENT ON COLUMN waitlist_entries.priority IS 'Prioritet: URGENT (akut), SOON (snarast), ROUTINE (rutin)';
