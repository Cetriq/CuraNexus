-- Journal Module Seed Data (A3)
-- Version: 2
-- Description: Sample clinical notes, diagnoses, procedures and observations

-- Practitioner IDs
-- Anna Karlsson (Läkare): aaaaaaaa-0001-0001-0001-000000000001
-- Erik Lindgren (Sjuksköterska): aaaaaaaa-0002-0002-0002-000000000002

-- ============================================
-- CLINICAL NOTES
-- ============================================

INSERT INTO clinical_notes (id, encounter_id, patient_id, type, title, content, status, author_id, author_name, signed_by_id, signed_by_name, signed_at, created_at, updated_at)
VALUES
    -- Pågående besök - Eva Eriksson (trötthet/yrsel)
    ('a1111111-1111-1111-1111-111111111111', 'e1111111-1111-1111-1111-111111111111', '11111111-1111-1111-1111-111111111111',
     'PROGRESS', 'Mottagningsanteckning',
     'Anamnes: 68-årig kvinna som söker pga trötthet och yrsel sedan ca 2 veckor. Känner sig yr när hon reser sig upp, särskilt på morgonen. Ingen synkope. Ingen bröstsmärta eller dyspné. Tar Enalapril 10mg x 1 för hypertoni.

Status: AT gott. BT 125/78 sittande, 110/70 stående (positiv ortostatisk reaktion). Puls 72 regelbunden. Cor: rena toner, inga blåsljud. Pulm: normala andningsljud bilat.

Bedömning: Ortostatisk hypotension, troligen läkemedelsrelaterad.

Åtgärd: Reducerar Enalapril till 5mg x 1. Återbesök om 2 veckor för kontroll av blodtryck.',
     'DRAFT', 'aaaaaaaa-0001-0001-0001-000000000001', 'Anna Karlsson', NULL, NULL, NULL, NOW() - INTERVAL '20 minutes', NOW()),

    -- Pågående akutbesök - Karl Persson (bröstsmärta)
    ('a2222222-2222-2222-2222-222222222222', 'e2222222-2222-2222-2222-222222222222', '22222222-2222-2222-2222-222222222222',
     'ADMISSION', 'Akutanteckning',
     'Anamnes: 51-årig man inkommer med akut insättande bröstsmärta sedan ca 2 timmar. Smärtan beskrivs som tryckande, utstrålning till vänster arm. Illamående. Tidigare frisk, ingen känd hjärtsjukdom. Röker 10 cig/dag sedan 30 år.

Status: Påverkad patient. BT 165/95. Puls 95 oregelbunden. Sat 94% på luft. Cor: oregelbunden rytm, inga blåsljud. Pulm: rena andningsljud.

EKG: Förmaksflimmer med snabb kammarfrekvens. ST-sänkningar V4-V6.

Bedömning: Misstänkt akut koronart syndrom. FF med snabb överledning. Inväntar troponin.',
     'DRAFT', 'aaaaaaaa-0001-0001-0001-000000000001', 'Anna Karlsson', NULL, NULL, NULL, NOW() - INTERVAL '1 hour', NOW()),

    -- Sjuksköterskeanteckning - triagering
    ('a2222222-2222-2222-2222-222222222223', 'e2222222-2222-2222-2222-222222222222', '22222222-2222-2222-2222-222222222222',
     'NURSING', 'Triageanteckning',
     'Patient anländer med ambulans. Alert men smärtpåverkad. VAS 7/10. Ansluten till telemetri. IV-infart höger arm. Tagen till akutrummet för EKG och provtagning.',
     'FINAL', 'aaaaaaaa-0002-0002-0002-000000000002', 'Erik Lindgren', 'aaaaaaaa-0002-0002-0002-000000000002', 'Erik Lindgren', NOW() - INTERVAL '1 hour 30 minutes', NOW() - INTERVAL '1 hour 40 minutes', NOW() - INTERVAL '1 hour 30 minutes'),

    -- Avslutade besök - signerade anteckningar
    ('a7777777-7777-7777-7777-777777777777', 'e7777777-7777-7777-7777-777777777777', '11111111-1111-1111-1111-111111111111',
     'PROGRESS', 'Återbesök huvudvärk',
     'Återbesök avseende huvudvärk. Patienten uppger att huvudvärken har avtagit efter insatt behandling med Paracetamol vid behov. Inga alarmsymtom. Neurologiskt status ua.

Bedömning: Spänningshuvudvärk, god effekt av analgetika.

Åtgärd: Fortsatt Paracetamol vb. Kontakt vid försämring.',
     'FINAL', 'aaaaaaaa-0001-0001-0001-000000000001', 'Anna Karlsson', 'aaaaaaaa-0001-0001-0001-000000000001', 'Anna Karlsson', NOW() - INTERVAL '1 week' + INTERVAL '30 minutes', NOW() - INTERVAL '1 week' + INTERVAL '10 minutes', NOW() - INTERVAL '1 week' + INTERVAL '30 minutes'),

    ('a8888888-8888-8888-8888-888888888888', 'e8888888-8888-8888-8888-888888888888', '22222222-2222-2222-2222-222222222222',
     'ADMISSION', 'Akutanteckning - trauma',
     'Anamnes: 51-årig man som inkom efter fall från cykel. Landade på höger höft. Kraftig smärta. Kan ej stödja på benet.

Status: Smärtpåverkad. Höger ben utåtroterat och förkortat. Palpationsömhet över höger höft. Distal neurologi och cirkulation intakt.

Röntgen: Cervikal collumfraktur höger femur.

Åtgärd: Smärtlindring med Morfin 5mg iv. Ortopedkonsult för operationsbedömning. Fasta inför ev operation.',
     'FINAL', 'aaaaaaaa-0001-0001-0001-000000000001', 'Anna Karlsson', 'aaaaaaaa-0001-0001-0001-000000000001', 'Anna Karlsson', NOW() - INTERVAL '3 days' + INTERVAL '3 hours', NOW() - INTERVAL '3 days' + INTERVAL '1 hour', NOW() - INTERVAL '3 days' + INTERVAL '3 hours')

ON CONFLICT (id) DO NOTHING;

-- ============================================
-- DIAGNOSES (ICD-10-SE)
-- ============================================

INSERT INTO diagnoses (id, encounter_id, patient_id, code, code_system, display_text, type, rank, onset_date, recorded_at, recorded_by_id)
VALUES
    -- Eva Eriksson - ortostatisk hypotension
    (gen_random_uuid(), 'e1111111-1111-1111-1111-111111111111', '11111111-1111-1111-1111-111111111111',
     'I95.1', 'ICD-10-SE', 'Ortostatisk hypotension', 'PRINCIPAL', 1, CURRENT_DATE - INTERVAL '14 days', NOW(), 'aaaaaaaa-0001-0001-0001-000000000001'),
    (gen_random_uuid(), 'e1111111-1111-1111-1111-111111111111', '11111111-1111-1111-1111-111111111111',
     'I10.9', 'ICD-10-SE', 'Essentiell hypertoni, ospecificerad', 'SECONDARY', 2, '2015-01-01', NOW(), 'aaaaaaaa-0001-0001-0001-000000000001'),

    -- Karl Persson - akut koronart syndrom
    (gen_random_uuid(), 'e2222222-2222-2222-2222-222222222222', '22222222-2222-2222-2222-222222222222',
     'I21.9', 'ICD-10-SE', 'Akut hjärtinfarkt, ospecificerad', 'DIFFERENTIAL', 1, CURRENT_DATE, NOW(), 'aaaaaaaa-0001-0001-0001-000000000001'),
    (gen_random_uuid(), 'e2222222-2222-2222-2222-222222222222', '22222222-2222-2222-2222-222222222222',
     'I48.9', 'ICD-10-SE', 'Förmaksflimmer, ospecificerat', 'SECONDARY', 2, CURRENT_DATE, NOW(), 'aaaaaaaa-0001-0001-0001-000000000001'),

    -- Karl Persson - tidigare cykelolycka (avslutad)
    (gen_random_uuid(), 'e8888888-8888-8888-8888-888888888888', '22222222-2222-2222-2222-222222222222',
     'S72.0', 'ICD-10-SE', 'Fraktur på lårbenshalsen', 'PRINCIPAL', 1, CURRENT_DATE - INTERVAL '3 days', NOW() - INTERVAL '3 days', 'aaaaaaaa-0001-0001-0001-000000000001'),
    (gen_random_uuid(), 'e8888888-8888-8888-8888-888888888888', '22222222-2222-2222-2222-222222222222',
     'W01.9', 'ICD-10-SE', 'Fall i samma plan på grund av halkning, snubbling eller felsteg', 'SECONDARY', 2, CURRENT_DATE - INTERVAL '3 days', NOW() - INTERVAL '3 days', 'aaaaaaaa-0001-0001-0001-000000000001'),

    -- Eva Eriksson - tidigare huvudvärk (avslutad)
    (gen_random_uuid(), 'e7777777-7777-7777-7777-777777777777', '11111111-1111-1111-1111-111111111111',
     'G44.2', 'ICD-10-SE', 'Spänningshuvudvärk', 'PRINCIPAL', 1, CURRENT_DATE - INTERVAL '3 weeks', NOW() - INTERVAL '1 week', 'aaaaaaaa-0001-0001-0001-000000000001'),

    -- Anna Lindberg - knäoperation uppföljning
    (gen_random_uuid(), 'e3333333-3333-3333-3333-333333333333', '33333333-3333-3333-3333-333333333333',
     'M23.5', 'ICD-10-SE', 'Kronisk instabilitet i knäled', 'PRINCIPAL', 1, CURRENT_DATE - INTERVAL '3 months', NOW(), 'aaaaaaaa-0001-0001-0001-000000000001'),
    (gen_random_uuid(), 'e3333333-3333-3333-3333-333333333333', '33333333-3333-3333-3333-333333333333',
     'Z96.6', 'ICD-10-SE', 'Ortopediskt led-implantat', 'SECONDARY', 2, CURRENT_DATE - INTERVAL '2 months', NOW(), 'aaaaaaaa-0001-0001-0001-000000000001')

ON CONFLICT DO NOTHING;

-- ============================================
-- PROCEDURES (KVÅ)
-- ============================================

INSERT INTO procedures (id, encounter_id, patient_id, code, code_system, display_text, status, performed_at, performed_by_id, performed_by_name, body_site, notes, created_at, updated_at)
VALUES
    -- Karl Persson - EKG på akuten
    (gen_random_uuid(), 'e2222222-2222-2222-2222-222222222222', '22222222-2222-2222-2222-222222222222',
     'AF310', 'KVÅ', 'Elektrokardiografi (EKG)', 'COMPLETED', NOW() - INTERVAL '1 hour 30 minutes', 'aaaaaaaa-0002-0002-0002-000000000002', 'Erik Lindgren', NULL, 'Standard 12-avlednings-EKG', NOW() - INTERVAL '1 hour 30 minutes', NOW()),

    -- Anna Lindberg - tidigare knäoperation
    (gen_random_uuid(), 'e9999999-9999-9999-9999-999999999999', '33333333-3333-3333-3333-333333333333',
     'NGE59', 'KVÅ', 'Artroskopi av knäled', 'COMPLETED', NOW() - INTERVAL '2 months', 'aaaaaaaa-0001-0001-0001-000000000001', 'Anna Karlsson', 'Höger knä', 'Menisksutur höger knä', NOW() - INTERVAL '2 months', NOW() - INTERVAL '2 months'),

    -- Karl Persson - höftoperation planerad
    (gen_random_uuid(), 'e8888888-8888-8888-8888-888888888888', '22222222-2222-2222-2222-222222222222',
     'NFB29', 'KVÅ', 'Sluten reposition av fraktur i höftled', 'COMPLETED', NOW() - INTERVAL '3 days' + INTERVAL '6 hours', 'aaaaaaaa-0001-0001-0001-000000000001', 'Anna Karlsson', 'Höger höft', 'Opererad med intern fixation', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days' + INTERVAL '6 hours')

ON CONFLICT DO NOTHING;

-- ============================================
-- OBSERVATIONS (Vital Signs)
-- ============================================

INSERT INTO observations (id, encounter_id, patient_id, code, code_system, display_text, category, value_numeric, unit, reference_range_low, reference_range_high, interpretation, observed_at, recorded_at, recorded_by_id, recorded_by_name)
VALUES
    -- Eva Eriksson - vitala parametrar
    (gen_random_uuid(), 'e1111111-1111-1111-1111-111111111111', '11111111-1111-1111-1111-111111111111',
     '8480-6', 'LOINC', 'Systoliskt blodtryck', 'VITAL_SIGNS', 125, 'mmHg', 90, 140, 'NORMAL', NOW() - INTERVAL '20 minutes', NOW() - INTERVAL '20 minutes', 'aaaaaaaa-0002-0002-0002-000000000002', 'Erik Lindgren'),
    (gen_random_uuid(), 'e1111111-1111-1111-1111-111111111111', '11111111-1111-1111-1111-111111111111',
     '8462-4', 'LOINC', 'Diastoliskt blodtryck', 'VITAL_SIGNS', 78, 'mmHg', 60, 90, 'NORMAL', NOW() - INTERVAL '20 minutes', NOW() - INTERVAL '20 minutes', 'aaaaaaaa-0002-0002-0002-000000000002', 'Erik Lindgren'),
    (gen_random_uuid(), 'e1111111-1111-1111-1111-111111111111', '11111111-1111-1111-1111-111111111111',
     '8867-4', 'LOINC', 'Hjärtfrekvens', 'VITAL_SIGNS', 72, '/min', 60, 100, 'NORMAL', NOW() - INTERVAL '20 minutes', NOW() - INTERVAL '20 minutes', 'aaaaaaaa-0002-0002-0002-000000000002', 'Erik Lindgren'),

    -- Karl Persson - akuta vitala parametrar
    (gen_random_uuid(), 'e2222222-2222-2222-2222-222222222222', '22222222-2222-2222-2222-222222222222',
     '8480-6', 'LOINC', 'Systoliskt blodtryck', 'VITAL_SIGNS', 165, 'mmHg', 90, 140, 'HIGH', NOW() - INTERVAL '1 hour 40 minutes', NOW() - INTERVAL '1 hour 40 minutes', 'aaaaaaaa-0002-0002-0002-000000000002', 'Erik Lindgren'),
    (gen_random_uuid(), 'e2222222-2222-2222-2222-222222222222', '22222222-2222-2222-2222-222222222222',
     '8462-4', 'LOINC', 'Diastoliskt blodtryck', 'VITAL_SIGNS', 95, 'mmHg', 60, 90, 'HIGH', NOW() - INTERVAL '1 hour 40 minutes', NOW() - INTERVAL '1 hour 40 minutes', 'aaaaaaaa-0002-0002-0002-000000000002', 'Erik Lindgren'),
    (gen_random_uuid(), 'e2222222-2222-2222-2222-222222222222', '22222222-2222-2222-2222-222222222222',
     '8867-4', 'LOINC', 'Hjärtfrekvens', 'VITAL_SIGNS', 95, '/min', 60, 100, 'NORMAL', NOW() - INTERVAL '1 hour 40 minutes', NOW() - INTERVAL '1 hour 40 minutes', 'aaaaaaaa-0002-0002-0002-000000000002', 'Erik Lindgren'),
    (gen_random_uuid(), 'e2222222-2222-2222-2222-222222222222', '22222222-2222-2222-2222-222222222222',
     '2708-6', 'LOINC', 'Syremättnad', 'VITAL_SIGNS', 94, '%', 95, 100, 'LOW', NOW() - INTERVAL '1 hour 40 minutes', NOW() - INTERVAL '1 hour 40 minutes', 'aaaaaaaa-0002-0002-0002-000000000002', 'Erik Lindgren'),
    (gen_random_uuid(), 'e2222222-2222-2222-2222-222222222222', '22222222-2222-2222-2222-222222222222',
     '8310-5', 'LOINC', 'Kroppstemperatur', 'VITAL_SIGNS', 37.2, 'Cel', 36.0, 37.5, 'NORMAL', NOW() - INTERVAL '1 hour 40 minutes', NOW() - INTERVAL '1 hour 40 minutes', 'aaaaaaaa-0002-0002-0002-000000000002', 'Erik Lindgren'),

    -- Karl Persson - lab-resultat (troponin)
    (gen_random_uuid(), 'e2222222-2222-2222-2222-222222222222', '22222222-2222-2222-2222-222222222222',
     '6598-7', 'LOINC', 'Troponin T hs', 'LABORATORY', 156, 'ng/L', 0, 14, 'CRITICAL_HIGH', NOW() - INTERVAL '45 minutes', NOW() - INTERVAL '30 minutes', 'aaaaaaaa-0001-0001-0001-000000000001', 'Anna Karlsson')

ON CONFLICT DO NOTHING;
