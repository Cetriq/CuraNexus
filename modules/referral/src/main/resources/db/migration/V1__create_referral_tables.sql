-- Referral Module Schema
-- Cura Nexus Healthcare Platform

-- Remisser
CREATE TABLE referrals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    referral_reference VARCHAR(20) NOT NULL UNIQUE,
    patient_id UUID NOT NULL,
    patient_personnummer VARCHAR(12),
    patient_name VARCHAR(200),
    referral_type VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    priority VARCHAR(20) NOT NULL DEFAULT 'ROUTINE',

    -- Avsändare
    sender_unit_id UUID NOT NULL,
    sender_unit_hsa_id VARCHAR(50),
    sender_unit_name VARCHAR(200),
    sender_practitioner_id UUID NOT NULL,
    sender_practitioner_hsa_id VARCHAR(50),
    sender_practitioner_name VARCHAR(200),

    -- Mottagare
    receiver_unit_id UUID,
    receiver_unit_hsa_id VARCHAR(50),
    receiver_unit_name VARCHAR(200),
    requested_specialty VARCHAR(100),

    -- Remissinnehåll
    reason VARCHAR(2000) NOT NULL,
    diagnosis_code VARCHAR(20),
    diagnosis_text VARCHAR(500),
    clinical_history VARCHAR(4000),
    current_status VARCHAR(2000),
    examinations_done VARCHAR(2000),
    current_medication VARCHAR(2000),
    allergies VARCHAR(1000),
    additional_info VARCHAR(2000),

    -- Bilagor
    attachment_ids VARCHAR(1000),

    -- Kopplingar
    source_encounter_id UUID,
    resulting_encounter_id UUID,
    requested_date DATE,
    valid_until DATE,

    -- Tidsstämplar
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    sent_at TIMESTAMP WITH TIME ZONE,
    received_at TIMESTAMP WITH TIME ZONE,
    assessed_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,

    -- Bedömning
    assessor_id UUID,
    assessor_name VARCHAR(200)
);

CREATE INDEX idx_referral_patient ON referrals(patient_id);
CREATE INDEX idx_referral_status ON referrals(status);
CREATE INDEX idx_referral_sender_unit ON referrals(sender_unit_id);
CREATE INDEX idx_referral_receiver_unit ON referrals(receiver_unit_id);
CREATE INDEX idx_referral_reference ON referrals(referral_reference);
CREATE INDEX idx_referral_priority ON referrals(priority);
CREATE INDEX idx_referral_type ON referrals(referral_type);
CREATE INDEX idx_referral_created ON referrals(created_at);
CREATE INDEX idx_referral_sent ON referrals(sent_at);
CREATE INDEX idx_referral_received ON referrals(received_at);

-- Remissvar/meddelanden
CREATE TABLE referral_responses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    referral_id UUID NOT NULL REFERENCES referrals(id) ON DELETE CASCADE,
    response_type VARCHAR(30) NOT NULL,

    -- Svarande
    responder_unit_id UUID,
    responder_unit_name VARCHAR(200),
    responder_id UUID NOT NULL,
    responder_hsa_id VARCHAR(50),
    responder_name VARCHAR(200),

    -- Svar
    response_text VARCHAR(4000),
    assessed_priority VARCHAR(20),
    planned_date DATE,
    rejection_reason VARCHAR(1000),
    requested_information VARCHAR(2000),

    -- Vidareskickning
    forwarded_to_unit_id UUID,
    forwarded_to_unit_name VARCHAR(200),
    forward_reason VARCHAR(1000),

    -- Bilagor
    attachment_ids VARCHAR(1000),

    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_response_referral ON referral_responses(referral_id);
CREATE INDEX idx_response_type ON referral_responses(response_type);
CREATE INDEX idx_response_created ON referral_responses(created_at);
CREATE INDEX idx_response_responder ON referral_responses(responder_id);

-- Kommentarer
COMMENT ON TABLE referrals IS 'Remisser mellan vårdenheter';
COMMENT ON TABLE referral_responses IS 'Svar och meddelanden på remisser';

COMMENT ON COLUMN referrals.referral_reference IS 'Unikt remissreferensnummer (REM-YYYYMMDD-XXXX)';
COMMENT ON COLUMN referrals.status IS 'DRAFT, SENT, RECEIVED, UNDER_ASSESSMENT, ACCEPTED, REJECTED, PENDING_INFORMATION, FORWARDED, COMPLETED, CANCELLED, EXPIRED';
COMMENT ON COLUMN referrals.priority IS 'IMMEDIATE, URGENT, SEMI_URGENT, ROUTINE, ELECTIVE';
COMMENT ON COLUMN referrals.referral_type IS 'CONSULTATION, TREATMENT, INVESTIGATION, RADIOLOGY, LABORATORY, PHYSIOTHERAPY, SPECIALIST, INTERNAL, SELF_REFERRAL';
COMMENT ON COLUMN referral_responses.response_type IS 'ACCEPTANCE, REJECTION, INFORMATION_REQUEST, INFORMATION_PROVIDED, FORWARDED, PRELIMINARY_ASSESSMENT, FINAL_RESPONSE, NOTE';
