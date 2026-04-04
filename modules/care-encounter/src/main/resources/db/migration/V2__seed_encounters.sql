-- Care Encounter Module Seed Data (A2)
-- Version: 2
-- Description: Sample encounters for development and testing

-- Practitioner IDs (references to authorization module)
-- Anna Karlsson (Läkare): aaaaaaaa-0001-0001-0001-000000000001
-- Erik Lindgren (Sjuksköterska): aaaaaaaa-0002-0002-0002-000000000002
-- Maria Svensson (Vårdadmin): aaaaaaaa-0003-0003-0003-000000000003

-- Unit IDs
-- Akutmottagningen: bbbbbbbb-0001-0001-0001-000000000001
-- Medicinkliniken: bbbbbbbb-0002-0002-0002-000000000002
-- Ortopedmottagningen: bbbbbbbb-0003-0003-0003-000000000003

-- Insert sample encounters with various statuses
INSERT INTO encounters (id, patient_id, status, encounter_class, encounter_type, priority, service_type, responsible_unit_id, responsible_practitioner_id, planned_start_time, actual_start_time, actual_end_time, created_at, updated_at)
VALUES
    -- Pågående besök (IN_PROGRESS)
    ('e1111111-1111-1111-1111-111111111111', '11111111-1111-1111-1111-111111111111', 'IN_PROGRESS', 'OUTPATIENT', 'ROUTINE', 'NORMAL', 'Medicin', 'bbbbbbbb-0002-0002-0002-000000000002', 'aaaaaaaa-0001-0001-0001-000000000001', NOW() - INTERVAL '30 minutes', NOW() - INTERVAL '25 minutes', NULL, NOW() - INTERVAL '1 hour', NOW()),

    ('e2222222-2222-2222-2222-222222222222', '22222222-2222-2222-2222-222222222222', 'IN_PROGRESS', 'EMERGENCY', 'EMERGENCY', 'URGENT', 'Akutmedicin', 'bbbbbbbb-0001-0001-0001-000000000001', 'aaaaaaaa-0001-0001-0001-000000000001', NOW() - INTERVAL '2 hours', NOW() - INTERVAL '1 hour 45 minutes', NULL, NOW() - INTERVAL '2 hours', NOW()),

    ('e3333333-3333-3333-3333-333333333333', '33333333-3333-3333-3333-333333333333', 'IN_PROGRESS', 'OUTPATIENT', 'FOLLOW_UP', 'NORMAL', 'Ortopedi', 'bbbbbbbb-0003-0003-0003-000000000003', 'aaaaaaaa-0001-0001-0001-000000000001', NOW() - INTERVAL '15 minutes', NOW() - INTERVAL '10 minutes', NULL, NOW() - INTERVAL '30 minutes', NOW()),

    -- Planerade besök (PLANNED)
    ('e4444444-4444-4444-4444-444444444444', '44444444-4444-4444-4444-444444444444', 'PLANNED', 'OUTPATIENT', 'ROUTINE', 'NORMAL', 'Medicin', 'bbbbbbbb-0002-0002-0002-000000000002', 'aaaaaaaa-0001-0001-0001-000000000001', NOW() + INTERVAL '1 hour', NULL, NULL, NOW() - INTERVAL '1 week', NOW()),

    ('e5555555-5555-5555-5555-555555555555', '55555555-5555-5555-5555-555555555555', 'PLANNED', 'OUTPATIENT', 'FOLLOW_UP', 'NORMAL', 'Medicin', 'bbbbbbbb-0002-0002-0002-000000000002', 'aaaaaaaa-0001-0001-0001-000000000001', NOW() + INTERVAL '2 hours', NULL, NULL, NOW() - INTERVAL '3 days', NOW()),

    ('e6666666-6666-6666-6666-666666666666', '77777777-7777-7777-7777-777777777777', 'PLANNED', 'OUTPATIENT', 'ROUTINE', 'NORMAL', 'Geriatrik', 'bbbbbbbb-0002-0002-0002-000000000002', 'aaaaaaaa-0001-0001-0001-000000000001', NOW() + INTERVAL '3 hours', NULL, NULL, NOW() - INTERVAL '2 weeks', NOW()),

    -- Avslutade besök (FINISHED) - historik
    ('e7777777-7777-7777-7777-777777777777', '11111111-1111-1111-1111-111111111111', 'FINISHED', 'OUTPATIENT', 'ROUTINE', 'NORMAL', 'Medicin', 'bbbbbbbb-0002-0002-0002-000000000002', 'aaaaaaaa-0001-0001-0001-000000000001', NOW() - INTERVAL '1 week', NOW() - INTERVAL '1 week' + INTERVAL '5 minutes', NOW() - INTERVAL '1 week' + INTERVAL '35 minutes', NOW() - INTERVAL '1 week', NOW() - INTERVAL '1 week'),

    ('e8888888-8888-8888-8888-888888888888', '22222222-2222-2222-2222-222222222222', 'FINISHED', 'EMERGENCY', 'EMERGENCY', 'IMMEDIATE', 'Akutmedicin', 'bbbbbbbb-0001-0001-0001-000000000001', 'aaaaaaaa-0001-0001-0001-000000000001', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days' + INTERVAL '4 hours', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'),

    ('e9999999-9999-9999-9999-999999999999', '33333333-3333-3333-3333-333333333333', 'FINISHED', 'OUTPATIENT', 'FOLLOW_UP', 'NORMAL', 'Ortopedi', 'bbbbbbbb-0003-0003-0003-000000000003', 'aaaaaaaa-0001-0001-0001-000000000001', NOW() - INTERVAL '2 weeks', NOW() - INTERVAL '2 weeks' + INTERVAL '10 minutes', NOW() - INTERVAL '2 weeks' + INTERVAL '40 minutes', NOW() - INTERVAL '2 weeks', NOW() - INTERVAL '2 weeks'),

    -- Triagerad patient (TRIAGED) - väntar på läkare
    ('eaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '88888888-8888-8888-8888-888888888888', 'TRIAGED', 'EMERGENCY', 'EMERGENCY', 'URGENT', 'Akutmedicin', 'bbbbbbbb-0001-0001-0001-000000000001', NULL, NOW() - INTERVAL '45 minutes', NOW() - INTERVAL '40 minutes', NULL, NOW() - INTERVAL '45 minutes', NOW()),

    -- Anländ patient (ARRIVED) - väntar på triage
    ('ebbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '66666666-6666-6666-6666-666666666666', 'ARRIVED', 'EMERGENCY', 'EMERGENCY', 'NORMAL', 'Akutmedicin', 'bbbbbbbb-0001-0001-0001-000000000001', NULL, NOW() - INTERVAL '10 minutes', NOW() - INTERVAL '5 minutes', NULL, NOW() - INTERVAL '10 minutes', NOW())

ON CONFLICT (id) DO NOTHING;

-- Insert participants (who is involved in each encounter)
INSERT INTO participants (id, encounter_id, type, practitioner_id, role, period_start, period_end)
VALUES
    -- Pågående besök - deltagare
    (gen_random_uuid(), 'e1111111-1111-1111-1111-111111111111', 'PRACTITIONER', 'aaaaaaaa-0001-0001-0001-000000000001', 'PRIMARY', NOW() - INTERVAL '25 minutes', NULL),
    (gen_random_uuid(), 'e1111111-1111-1111-1111-111111111111', 'PRACTITIONER', 'aaaaaaaa-0002-0002-0002-000000000002', 'ASSIST', NOW() - INTERVAL '20 minutes', NULL),

    (gen_random_uuid(), 'e2222222-2222-2222-2222-222222222222', 'PRACTITIONER', 'aaaaaaaa-0001-0001-0001-000000000001', 'PRIMARY', NOW() - INTERVAL '1 hour 45 minutes', NULL),
    (gen_random_uuid(), 'e2222222-2222-2222-2222-222222222222', 'PRACTITIONER', 'aaaaaaaa-0002-0002-0002-000000000002', 'TRIAGE', NOW() - INTERVAL '2 hours', NOW() - INTERVAL '1 hour 50 minutes'),

    (gen_random_uuid(), 'e3333333-3333-3333-3333-333333333333', 'PRACTITIONER', 'aaaaaaaa-0001-0001-0001-000000000001', 'PRIMARY', NOW() - INTERVAL '10 minutes', NULL),

    -- Avslutade besök - deltagare
    (gen_random_uuid(), 'e7777777-7777-7777-7777-777777777777', 'PRACTITIONER', 'aaaaaaaa-0001-0001-0001-000000000001', 'PRIMARY', NOW() - INTERVAL '1 week' + INTERVAL '5 minutes', NOW() - INTERVAL '1 week' + INTERVAL '35 minutes'),

    (gen_random_uuid(), 'e8888888-8888-8888-8888-888888888888', 'PRACTITIONER', 'aaaaaaaa-0001-0001-0001-000000000001', 'PRIMARY', NOW() - INTERVAL '3 days' + INTERVAL '30 minutes', NOW() - INTERVAL '3 days' + INTERVAL '4 hours'),
    (gen_random_uuid(), 'e8888888-8888-8888-8888-888888888888', 'PRACTITIONER', 'aaaaaaaa-0002-0002-0002-000000000002', 'TRIAGE', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days' + INTERVAL '20 minutes')

ON CONFLICT DO NOTHING;

-- Insert encounter reasons (chief complaints / sökorsaker)
INSERT INTO encounter_reasons (id, encounter_id, type, code, code_system, display_text, is_primary)
VALUES
    -- Pågående besök
    (gen_random_uuid(), 'e1111111-1111-1111-1111-111111111111', 'CHIEF_COMPLAINT', NULL, NULL, 'Trötthet och yrsel sedan 2 veckor', true),
    (gen_random_uuid(), 'e2222222-2222-2222-2222-222222222222', 'CHIEF_COMPLAINT', NULL, NULL, 'Akut bröstsmärta', true),
    (gen_random_uuid(), 'e2222222-2222-2222-2222-222222222222', 'CHIEF_COMPLAINT', NULL, NULL, 'Andnöd', false),
    (gen_random_uuid(), 'e3333333-3333-3333-3333-333333333333', 'CHIEF_COMPLAINT', NULL, NULL, 'Uppföljning efter knäoperation', true),

    -- Planerade besök
    (gen_random_uuid(), 'e4444444-4444-4444-4444-444444444444', 'CHIEF_COMPLAINT', NULL, NULL, 'Årskontroll diabetes', true),
    (gen_random_uuid(), 'e5555555-5555-5555-5555-555555555555', 'CHIEF_COMPLAINT', NULL, NULL, 'Kontroll blodtryck', true),
    (gen_random_uuid(), 'e6666666-6666-6666-6666-666666666666', 'CHIEF_COMPLAINT', NULL, NULL, 'Minnesproblem, utredning', true),

    -- Avslutade besök
    (gen_random_uuid(), 'e7777777-7777-7777-7777-777777777777', 'CHIEF_COMPLAINT', NULL, NULL, 'Huvudvärk, återbesök', true),
    (gen_random_uuid(), 'e8888888-8888-8888-8888-888888888888', 'CHIEF_COMPLAINT', NULL, NULL, 'Fall från cykel', true),
    (gen_random_uuid(), 'e8888888-8888-8888-8888-888888888888', 'ADMISSION_DIAGNOSIS', 'S72.0', 'ICD-10-SE', 'Fraktur på lårbenshalsen', true),
    (gen_random_uuid(), 'e9999999-9999-9999-9999-999999999999', 'CHIEF_COMPLAINT', NULL, NULL, 'Rehabiliteringskontroll knä', true),

    -- Väntar på triage/läkare
    (gen_random_uuid(), 'eaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'CHIEF_COMPLAINT', NULL, NULL, 'Magsmärtor och kräkningar', true),
    (gen_random_uuid(), 'ebbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'CHIEF_COMPLAINT', NULL, NULL, 'Hög feber och halsont', true)

ON CONFLICT DO NOTHING;
