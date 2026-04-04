-- Patient Module Seed Data (A1)
-- Version: 2
-- Description: Sample patients for development and testing

-- Insert sample patients with realistic Swedish data
INSERT INTO patients (id, personal_identity_number, given_name, family_name, middle_name, date_of_birth, gender, protected_identity, deceased, created_at, updated_at)
VALUES
    -- Active patients
    ('11111111-1111-1111-1111-111111111111', '195505151234', 'Eva', 'Eriksson', NULL, '1955-05-15', 'FEMALE', false, false, NOW(), NOW()),
    ('22222222-2222-2222-2222-222222222222', '197208235678', 'Karl', 'Persson', 'Olov', '1972-08-23', 'MALE', false, false, NOW(), NOW()),
    ('33333333-3333-3333-3333-333333333333', '198511029012', 'Anna', 'Lindberg', NULL, '1985-11-02', 'FEMALE', false, false, NOW(), NOW()),
    ('44444444-4444-4444-4444-444444444444', '199003173456', 'Johan', 'Andersson', NULL, '1990-03-17', 'MALE', false, false, NOW(), NOW()),
    ('55555555-5555-5555-5555-555555555555', '196507287890', 'Maria', 'Svensson', 'Ingrid', '1965-07-28', 'FEMALE', false, false, NOW(), NOW()),
    ('66666666-6666-6666-6666-666666666666', '200112051234', 'Erik', 'Johansson', NULL, '2001-12-05', 'MALE', false, false, NOW(), NOW()),
    ('77777777-7777-7777-7777-777777777777', '194203105678', 'Ingrid', 'Karlsson', 'Margareta', '1942-03-10', 'FEMALE', false, false, NOW(), NOW()),
    ('88888888-8888-8888-8888-888888888888', '198807229012', 'Anders', 'Nilsson', NULL, '1988-07-22', 'MALE', false, false, NOW(), NOW()),
    -- Patient with protected identity
    ('99999999-9999-9999-9999-999999999999', '197506143456', 'Skyddad', 'Person', NULL, '1975-06-14', 'OTHER', true, false, NOW(), NOW()),
    -- More patients for realistic load
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '195012017890', 'Birgitta', 'Olsson', NULL, '1950-12-01', 'FEMALE', false, false, NOW(), NOW()),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '198205111234', 'Per', 'Gustafsson', 'Lars', '1982-05-11', 'MALE', false, false, NOW(), NOW()),
    ('cccccccc-cccc-cccc-cccc-cccccccccccc', '199509305678', 'Lisa', 'Holm', NULL, '1995-09-30', 'FEMALE', false, false, NOW(), NOW())
ON CONFLICT (personal_identity_number) DO NOTHING;

-- Insert contact information
INSERT INTO contact_info (id, patient_id, type, value, contact_use, is_primary, valid_from)
VALUES
    -- Eva Eriksson
    (gen_random_uuid(), '11111111-1111-1111-1111-111111111111', 'PHONE', '070-123 45 67', 'MOBILE', true, '2020-01-01'),
    (gen_random_uuid(), '11111111-1111-1111-1111-111111111111', 'EMAIL', 'eva.eriksson@email.se', 'HOME', false, '2020-01-01'),
    -- Karl Persson
    (gen_random_uuid(), '22222222-2222-2222-2222-222222222222', 'PHONE', '073-234 56 78', 'MOBILE', true, '2019-06-15'),
    (gen_random_uuid(), '22222222-2222-2222-2222-222222222222', 'PHONE', '08-555 12 34', 'HOME', false, '2019-06-15'),
    -- Anna Lindberg
    (gen_random_uuid(), '33333333-3333-3333-3333-333333333333', 'PHONE', '076-345 67 89', 'MOBILE', true, '2021-03-10'),
    (gen_random_uuid(), '33333333-3333-3333-3333-333333333333', 'EMAIL', 'anna.lindberg@work.se', 'WORK', false, '2021-03-10'),
    -- Johan Andersson
    (gen_random_uuid(), '44444444-4444-4444-4444-444444444444', 'PHONE', '072-456 78 90', 'MOBILE', true, '2022-01-20'),
    -- Maria Svensson
    (gen_random_uuid(), '55555555-5555-5555-5555-555555555555', 'PHONE', '070-567 89 01', 'MOBILE', true, '2018-11-05'),
    (gen_random_uuid(), '55555555-5555-5555-5555-555555555555', 'EMAIL', 'maria.svensson@gmail.com', 'HOME', false, '2018-11-05'),
    -- Erik Johansson
    (gen_random_uuid(), '66666666-6666-6666-6666-666666666666', 'PHONE', '073-678 90 12', 'MOBILE', true, '2023-02-14'),
    -- Ingrid Karlsson
    (gen_random_uuid(), '77777777-7777-7777-7777-777777777777', 'PHONE', '070-789 01 23', 'MOBILE', true, '2015-07-22'),
    (gen_random_uuid(), '77777777-7777-7777-7777-777777777777', 'PHONE', '08-123 45 67', 'HOME', false, '2015-07-22'),
    -- Anders Nilsson
    (gen_random_uuid(), '88888888-8888-8888-8888-888888888888', 'PHONE', '076-890 12 34', 'MOBILE', true, '2020-09-30')
ON CONFLICT DO NOTHING;

-- Insert related persons (next of kin)
INSERT INTO related_persons (id, patient_id, relationship, given_name, family_name, phone, email, is_emergency_contact, is_legal_guardian)
VALUES
    -- Eva Eriksson's husband
    (gen_random_uuid(), '11111111-1111-1111-1111-111111111111', 'SPOUSE', 'Lars', 'Eriksson', '070-111 22 33', 'lars.eriksson@email.se', true, false),
    -- Karl Persson's mother
    (gen_random_uuid(), '22222222-2222-2222-2222-222222222222', 'PARENT', 'Margareta', 'Persson', '070-222 33 44', NULL, true, false),
    -- Anna Lindberg's partner
    (gen_random_uuid(), '33333333-3333-3333-3333-333333333333', 'PARTNER', 'Stefan', 'Berg', '073-333 44 55', 'stefan.berg@email.se', true, false),
    -- Erik Johansson's parents (minor patient)
    (gen_random_uuid(), '66666666-6666-6666-6666-666666666666', 'PARENT', 'Karin', 'Johansson', '070-444 55 66', 'karin.johansson@email.se', true, true),
    (gen_random_uuid(), '66666666-6666-6666-6666-666666666666', 'PARENT', 'Mikael', 'Johansson', '070-555 66 77', NULL, true, true),
    -- Ingrid Karlsson's daughter
    (gen_random_uuid(), '77777777-7777-7777-7777-777777777777', 'CHILD', 'Helena', 'Karlsson', '076-666 77 88', 'helena.k@email.se', true, false)
ON CONFLICT DO NOTHING;

-- Add basic consents for NPÖ access
INSERT INTO consents (id, patient_id, type, status, given_at, given_by, valid_from, scope)
VALUES
    (gen_random_uuid(), '11111111-1111-1111-1111-111111111111', 'NPO_ACCESS', 'ACTIVE', NOW() - INTERVAL '1 year', 'Patient', CURRENT_DATE - INTERVAL '1 year', 'Tillåter åtkomst via NPÖ'),
    (gen_random_uuid(), '22222222-2222-2222-2222-222222222222', 'NPO_ACCESS', 'ACTIVE', NOW() - INTERVAL '6 months', 'Patient', CURRENT_DATE - INTERVAL '6 months', 'Tillåter åtkomst via NPÖ'),
    (gen_random_uuid(), '33333333-3333-3333-3333-333333333333', 'NPO_ACCESS', 'ACTIVE', NOW() - INTERVAL '3 months', 'Patient', CURRENT_DATE - INTERVAL '3 months', 'Tillåter åtkomst via NPÖ'),
    (gen_random_uuid(), '55555555-5555-5555-5555-555555555555', 'NPO_ACCESS', 'REVOKED', NOW() - INTERVAL '2 years', 'Patient', CURRENT_DATE - INTERVAL '2 years', 'Återkallat samtycke')
ON CONFLICT DO NOTHING;
