-- Task Module Seed Data (B1)
-- Version: 4
-- Description: Sample tasks for development and testing

-- Practitioner IDs
-- Anna Karlsson (Läkare): aaaaaaaa-0001-0001-0001-000000000001
-- Erik Lindgren (Sjuksköterska): aaaaaaaa-0002-0002-0002-000000000002
-- System: 00000000-0000-0000-0000-000000000000

-- Insert sample tasks for active encounters
INSERT INTO tasks (id, title, description, category, status, priority, patient_id, encounter_id, assignee_id, created_by_id, source_type, due_at, created_at, updated_at)
VALUES
    -- Uppgifter för Eva Eriksson (pågående besök - ortostatisk hypotension)
    ('d1111111-1111-1111-1111-111111111111', 'Kontrollera ortostatiskt blodtryck',
     'Ta blodtryck liggande, sittande och stående. Notera eventuell yrsel vid uppresning.',
     'CLINICAL', 'COMPLETED', 'NORMAL',
     '11111111-1111-1111-1111-111111111111', 'e1111111-1111-1111-1111-111111111111',
     'aaaaaaaa-0002-0002-0002-000000000002', '00000000-0000-0000-0000-000000000000',
     'ENCOUNTER', NOW() - INTERVAL '15 minutes', NOW() - INTERVAL '30 minutes', NOW()),

    ('d1111111-1111-1111-1111-111111111112', 'Signera journalanteckning',
     'Signera mottagningsanteckning för Eva Eriksson.',
     'DOCUMENTATION', 'PENDING', 'NORMAL',
     '11111111-1111-1111-1111-111111111111', 'e1111111-1111-1111-1111-111111111111',
     'aaaaaaaa-0001-0001-0001-000000000001', '00000000-0000-0000-0000-000000000000',
     'ENCOUNTER', NOW() + INTERVAL '2 hours', NOW() - INTERVAL '20 minutes', NOW()),

    ('d1111111-1111-1111-1111-111111111113', 'Boka återbesök om 2 veckor',
     'Boka tid för blodtryckskontroll om 2 veckor.',
     'ADMINISTRATIVE', 'PENDING', 'LOW',
     '11111111-1111-1111-1111-111111111111', 'e1111111-1111-1111-1111-111111111111',
     'aaaaaaaa-0002-0002-0002-000000000002', 'aaaaaaaa-0001-0001-0001-000000000001',
     'ENCOUNTER', NOW() + INTERVAL '1 day', NOW() - INTERVAL '15 minutes', NOW()),

    -- Uppgifter för Karl Persson (akut bröstsmärta - pågående)
    ('d2222222-2222-2222-2222-222222222221', 'Ge syrgas',
     'Administrera 2L syrgas via näsgrimma. Målsaturation >95%.',
     'CLINICAL', 'COMPLETED', 'URGENT',
     '22222222-2222-2222-2222-222222222222', 'e2222222-2222-2222-2222-222222222222',
     'aaaaaaaa-0002-0002-0002-000000000002', '00000000-0000-0000-0000-000000000000',
     'ENCOUNTER', NOW() - INTERVAL '1 hour 30 minutes', NOW() - INTERVAL '1 hour 45 minutes', NOW() - INTERVAL '1 hour 30 minutes'),

    ('d2222222-2222-2222-2222-222222222222', 'Ta blodprover (troponin, blodstatus)',
     'Akut provtagning: Troponin T, CK-MB, blodstatus, elektrolyter, kreatinin.',
     'LAB', 'COMPLETED', 'URGENT',
     '22222222-2222-2222-2222-222222222222', 'e2222222-2222-2222-2222-222222222222',
     'aaaaaaaa-0002-0002-0002-000000000002', 'aaaaaaaa-0001-0001-0001-000000000001',
     'ENCOUNTER', NOW() - INTERVAL '1 hour 20 minutes', NOW() - INTERVAL '1 hour 40 minutes', NOW() - INTERVAL '1 hour'),

    ('d2222222-2222-2222-2222-222222222223', 'Administrera ASA 500mg',
     'Ge Trombyl 500mg peroralt om ej kontraindikation.',
     'MEDICATION', 'COMPLETED', 'URGENT',
     '22222222-2222-2222-2222-222222222222', 'e2222222-2222-2222-2222-222222222222',
     'aaaaaaaa-0002-0002-0002-000000000002', 'aaaaaaaa-0001-0001-0001-000000000001',
     'ENCOUNTER', NOW() - INTERVAL '1 hour 25 minutes', NOW() - INTERVAL '1 hour 35 minutes', NOW() - INTERVAL '1 hour 20 minutes'),

    ('d2222222-2222-2222-2222-222222222224', 'Kontakta kardiologjour',
     'Ring kardiologjouren för bedömning och ställningstagande till PCI.',
     'CLINICAL', 'IN_PROGRESS', 'URGENT',
     '22222222-2222-2222-2222-222222222222', 'e2222222-2222-2222-2222-222222222222',
     'aaaaaaaa-0001-0001-0001-000000000001', 'aaaaaaaa-0001-0001-0001-000000000001',
     'ENCOUNTER', NOW(), NOW() - INTERVAL '30 minutes', NOW()),

    ('d2222222-2222-2222-2222-222222222225', 'Upprepa troponin om 3 timmar',
     'Kontrollera troponin T hs om 3 timmar för att följa förlopp.',
     'LAB', 'PENDING', 'HIGH',
     '22222222-2222-2222-2222-222222222222', 'e2222222-2222-2222-2222-222222222222',
     'aaaaaaaa-0002-0002-0002-000000000002', 'aaaaaaaa-0001-0001-0001-000000000001',
     'ENCOUNTER', NOW() + INTERVAL '2 hours', NOW() - INTERVAL '30 minutes', NOW()),

    -- Uppgifter för Anna Lindberg (knäkontroll - pågående)
    ('d3333333-3333-3333-3333-333333333331', 'Kontrollera rörlighet i knä',
     'Undersök ROM (range of motion) och stabilitet i höger knä.',
     'CLINICAL', 'IN_PROGRESS', 'NORMAL',
     '33333333-3333-3333-3333-333333333333', 'e3333333-3333-3333-3333-333333333333',
     'aaaaaaaa-0001-0001-0001-000000000001', '00000000-0000-0000-0000-000000000000',
     'ENCOUNTER', NOW(), NOW() - INTERVAL '10 minutes', NOW()),

    ('d3333333-3333-3333-3333-333333333332', 'Skriva remiss till fysioterapeut',
     'Skriva remiss för fortsatt rehabilitering hos fysioterapeut.',
     'REFERRAL', 'PENDING', 'NORMAL',
     '33333333-3333-3333-3333-333333333333', 'e3333333-3333-3333-3333-333333333333',
     'aaaaaaaa-0001-0001-0001-000000000001', 'aaaaaaaa-0001-0001-0001-000000000001',
     'ENCOUNTER', NOW() + INTERVAL '1 day', NOW() - INTERVAL '5 minutes', NOW()),

    -- Uppgifter för Anders Nilsson (triagerad, väntar på läkare)
    ('daaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Läkarbedömning',
     'Patient triagerad med magsmärtor. Behöver läkarbedömning.',
     'CLINICAL', 'PENDING', 'HIGH',
     '88888888-8888-8888-8888-888888888888', 'eaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
     NULL, 'aaaaaaaa-0002-0002-0002-000000000002',
     'ENCOUNTER', NOW() + INTERVAL '30 minutes', NOW() - INTERVAL '35 minutes', NOW()),

    -- Uppgifter för Erik Johansson (anländ, väntar på triage)
    ('dbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Triage',
     'Ny patient anländ. Behöver triagering.',
     'CLINICAL', 'PENDING', 'NORMAL',
     '66666666-6666-6666-6666-666666666666', 'ebbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
     'aaaaaaaa-0002-0002-0002-000000000002', '00000000-0000-0000-0000-000000000000',
     'ENCOUNTER', NOW() + INTERVAL '15 minutes', NOW() - INTERVAL '5 minutes', NOW()),

    -- Generella uppgifter (ej kopplade till specifikt besök)
    ('d0000000-0000-0000-0000-000000000001', 'Granska labbsvar',
     'Granska och signera inkomna labbsvar från gårdagen.',
     'LAB', 'PENDING', 'NORMAL',
     NULL, NULL,
     'aaaaaaaa-0001-0001-0001-000000000001', '00000000-0000-0000-0000-000000000000',
     NULL, NOW() + INTERVAL '4 hours', NOW() - INTERVAL '12 hours', NOW()),

    ('d0000000-0000-0000-0000-000000000002', 'Signera recept',
     'Signera väntande e-recept.',
     'MEDICATION', 'PENDING', 'NORMAL',
     NULL, NULL,
     'aaaaaaaa-0001-0001-0001-000000000001', '00000000-0000-0000-0000-000000000000',
     NULL, NOW() + INTERVAL '2 hours', NOW() - INTERVAL '6 hours', NOW())

ON CONFLICT (id) DO NOTHING;

-- Insert reminders
INSERT INTO reminders (id, user_id, message, status, remind_at, patient_id, encounter_id, task_id, created_at)
VALUES
    -- Påminnelse om att följa upp troponin
    (gen_random_uuid(), 'aaaaaaaa-0001-0001-0001-000000000001',
     'Kontrollera troponinsvar för Karl Persson',
     'PENDING', NOW() + INTERVAL '1 hour',
     '22222222-2222-2222-2222-222222222222', 'e2222222-2222-2222-2222-222222222222', 'd2222222-2222-2222-2222-222222222225',
     NOW()),

    -- Påminnelse om återbesök för Eva Eriksson
    (gen_random_uuid(), 'aaaaaaaa-0002-0002-0002-000000000002',
     'Boka återbesök för Eva Eriksson (blodtryckskontroll)',
     'PENDING', NOW() + INTERVAL '30 minutes',
     '11111111-1111-1111-1111-111111111111', 'e1111111-1111-1111-1111-111111111111', 'd1111111-1111-1111-1111-111111111113',
     NOW())

ON CONFLICT DO NOTHING;

-- Insert watches (bevakningar)
INSERT INTO watches (id, user_id, watch_type, target_id, notify_on_change, note, active, created_at)
VALUES
    -- Läkaren bevakar akutpatienten Karl Persson
    (gen_random_uuid(), 'aaaaaaaa-0001-0001-0001-000000000001',
     'PATIENT', '22222222-2222-2222-2222-222222222222',
     true, 'Akut bröstsmärta - följ upp troponin',
     true, NOW()),

    -- Sjuksköterskan bevakar akutpatienten
    (gen_random_uuid(), 'aaaaaaaa-0002-0002-0002-000000000002',
     'ENCOUNTER', 'e2222222-2222-2222-2222-222222222222',
     true, 'Bevakar akutbesök',
     true, NOW())

ON CONFLICT DO NOTHING;
