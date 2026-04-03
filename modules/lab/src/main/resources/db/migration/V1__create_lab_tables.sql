-- Lab Order Tables
-- CuraNexus Lab Module

-- Labbeställningar
CREATE TABLE lab_orders (
    id UUID PRIMARY KEY,
    order_reference VARCHAR(20) NOT NULL UNIQUE,
    patient_id UUID NOT NULL,
    patient_personnummer VARCHAR(12),
    patient_name VARCHAR(200),
    status VARCHAR(30) NOT NULL,
    priority VARCHAR(20) NOT NULL,

    -- Beställare
    ordering_unit_id UUID NOT NULL,
    ordering_unit_hsa_id VARCHAR(50),
    ordering_unit_name VARCHAR(200),
    ordering_practitioner_id UUID NOT NULL,
    ordering_practitioner_hsa_id VARCHAR(50),
    ordering_practitioner_name VARCHAR(200),

    -- Utförande lab
    performing_lab_id UUID,
    performing_lab_hsa_id VARCHAR(50),
    performing_lab_name VARCHAR(200),

    -- Klinisk information
    clinical_indication VARCHAR(2000),
    diagnosis_code VARCHAR(20),
    diagnosis_text VARCHAR(500),
    relevant_medication VARCHAR(1000),
    fasting_required BOOLEAN,
    lab_comment VARCHAR(1000),

    -- Kopplingar
    encounter_id UUID,
    referral_id UUID,

    -- Tidsstämplar
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    ordered_at TIMESTAMP WITH TIME ZONE,
    received_at TIMESTAMP WITH TIME ZONE,
    specimen_collected_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_lab_order_patient ON lab_orders(patient_id);
CREATE INDEX idx_lab_order_status ON lab_orders(status);
CREATE INDEX idx_lab_order_reference ON lab_orders(order_reference);
CREATE INDEX idx_lab_order_ordered_at ON lab_orders(ordered_at);
CREATE INDEX idx_lab_order_encounter ON lab_orders(encounter_id);
CREATE INDEX idx_lab_order_ordering_unit ON lab_orders(ordering_unit_id);
CREATE INDEX idx_lab_order_performing_lab ON lab_orders(performing_lab_id);

-- Beställda tester/analyser
CREATE TABLE lab_order_items (
    id UUID PRIMARY KEY,
    lab_order_id UUID NOT NULL REFERENCES lab_orders(id) ON DELETE CASCADE,
    test_code VARCHAR(50) NOT NULL,
    code_system VARCHAR(50),
    test_name VARCHAR(200) NOT NULL,
    test_description VARCHAR(500),
    specimen_type VARCHAR(30),
    item_comment VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_order_item_order ON lab_order_items(lab_order_id);
CREATE INDEX idx_order_item_test_code ON lab_order_items(test_code);

-- Provmaterial
CREATE TABLE lab_specimens (
    id UUID PRIMARY KEY,
    lab_order_id UUID NOT NULL REFERENCES lab_orders(id) ON DELETE CASCADE,
    barcode VARCHAR(50),
    specimen_type VARCHAR(30) NOT NULL,
    collection_method VARCHAR(100),
    body_site VARCHAR(100),
    quantity VARCHAR(50),
    container_type VARCHAR(50),
    collector_id UUID,
    collector_name VARCHAR(200),
    collected_at TIMESTAMP WITH TIME ZONE,
    received_at_lab TIMESTAMP WITH TIME ZONE,
    quality_status VARCHAR(50),
    specimen_comment VARCHAR(500),
    rejected BOOLEAN DEFAULT FALSE,
    rejection_reason VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_specimen_order ON lab_specimens(lab_order_id);
CREATE INDEX idx_specimen_barcode ON lab_specimens(barcode);
CREATE INDEX idx_specimen_collected_at ON lab_specimens(collected_at);

-- Analysresultat
CREATE TABLE lab_results (
    id UUID PRIMARY KEY,
    order_item_id UUID NOT NULL UNIQUE REFERENCES lab_order_items(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL,

    -- Numeriskt värde
    value_numeric DECIMAL(18, 6),
    unit VARCHAR(50),

    -- Textvärde
    value_text VARCHAR(2000),

    -- Referensvärden
    reference_low DECIMAL(18, 6),
    reference_high DECIMAL(18, 6),
    reference_range_text VARCHAR(200),

    -- Avvikelse
    abnormal_flag VARCHAR(20),
    is_critical BOOLEAN DEFAULT FALSE,

    -- Analysuppgifter
    method VARCHAR(200),
    instrument VARCHAR(200),
    performing_department VARCHAR(200),
    analyzer_id UUID,
    analyzer_name VARCHAR(200),
    reviewer_id UUID,
    reviewer_name VARCHAR(200),

    -- Kommentarer
    lab_comment VARCHAR(1000),
    internal_comment VARCHAR(1000),

    -- Tidsstämplar
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    analyzed_at TIMESTAMP WITH TIME ZONE,
    reviewed_at TIMESTAMP WITH TIME ZONE,
    resulted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_result_order_item ON lab_results(order_item_id);
CREATE INDEX idx_result_status ON lab_results(status);
CREATE INDEX idx_result_abnormal ON lab_results(abnormal_flag);
CREATE INDEX idx_result_resulted_at ON lab_results(resulted_at);
CREATE INDEX idx_result_critical ON lab_results(is_critical) WHERE is_critical = TRUE;

-- Kommentarer
COMMENT ON TABLE lab_orders IS 'Labbeställningar - beställning av laboratorieanalyser';
COMMENT ON TABLE lab_order_items IS 'Beställda tester inom en labbeställning';
COMMENT ON TABLE lab_specimens IS 'Provmaterial - fysiska prover tagna från patient';
COMMENT ON TABLE lab_results IS 'Analysresultat för beställda tester';
COMMENT ON COLUMN lab_results.abnormal_flag IS 'NORMAL, LOW, HIGH, CRITICAL_LOW, CRITICAL_HIGH, POSITIVE, NEGATIVE, ABNORMAL';
COMMENT ON COLUMN lab_orders.status IS 'DRAFT, ORDERED, RECEIVED, SPECIMEN_COLLECTED, IN_PROGRESS, PARTIAL_RESULTS, COMPLETED, CANCELLED, REJECTED';
