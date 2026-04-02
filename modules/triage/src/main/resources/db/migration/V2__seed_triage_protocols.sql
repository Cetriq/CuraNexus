-- Seed data for RETTS-inspired triage protocols
-- These are simplified examples based on common emergency presentations

-- Chest pain protocol
INSERT INTO triage_protocols (id, code, name, description, category, version, active)
VALUES (
    'a1b2c3d4-e5f6-7890-abcd-ef1234567801',
    'RETTS-CHEST',
    'Chest Pain Protocol',
    'Assessment protocol for patients presenting with chest pain. Based on RETTS guidelines.',
    'Cardiovascular',
    '1.0',
    true
);

INSERT INTO protocol_steps (id, protocol_id, step_order, instruction, assessment_criteria) VALUES
('11111111-1111-1111-1111-111111111101', 'a1b2c3d4-e5f6-7890-abcd-ef1234567801', 1,
 'Assess airway, breathing, and circulation (ABC)', 'Check for signs of respiratory distress, cyanosis, or circulatory compromise'),
('11111111-1111-1111-1111-111111111102', 'a1b2c3d4-e5f6-7890-abcd-ef1234567801', 2,
 'Obtain vital signs including SpO2', 'BP, HR, RR, Temperature, SpO2'),
('11111111-1111-1111-1111-111111111103', 'a1b2c3d4-e5f6-7890-abcd-ef1234567801', 3,
 'Perform 12-lead ECG within 10 minutes', 'ECG interpretation for STEMI/NSTEMI'),
('11111111-1111-1111-1111-111111111104', 'a1b2c3d4-e5f6-7890-abcd-ef1234567801', 4,
 'Assess pain characteristics using OPQRST', 'Onset, Provocation, Quality, Region, Severity, Time');

INSERT INTO step_actions (step_id, action) VALUES
('11111111-1111-1111-1111-111111111101', 'Position patient upright if conscious'),
('11111111-1111-1111-1111-111111111101', 'Administer oxygen if SpO2 < 94%'),
('11111111-1111-1111-1111-111111111103', 'If STEMI: activate cardiac catheterization lab'),
('11111111-1111-1111-1111-111111111103', 'Administer aspirin 300mg if not contraindicated');

INSERT INTO protocol_red_flags (protocol_id, red_flag) VALUES
('a1b2c3d4-e5f6-7890-abcd-ef1234567801', 'ST elevation on ECG'),
('a1b2c3d4-e5f6-7890-abcd-ef1234567801', 'Hypotension (SBP < 90 mmHg)'),
('a1b2c3d4-e5f6-7890-abcd-ef1234567801', 'Signs of heart failure'),
('a1b2c3d4-e5f6-7890-abcd-ef1234567801', 'Altered consciousness'),
('a1b2c3d4-e5f6-7890-abcd-ef1234567801', 'Radiating pain to jaw or arm');

-- Respiratory distress protocol
INSERT INTO triage_protocols (id, code, name, description, category, version, active)
VALUES (
    'a1b2c3d4-e5f6-7890-abcd-ef1234567802',
    'RETTS-RESP',
    'Respiratory Distress Protocol',
    'Assessment protocol for patients with breathing difficulties.',
    'Respiratory',
    '1.0',
    true
);

INSERT INTO protocol_steps (id, protocol_id, step_order, instruction, assessment_criteria) VALUES
('22222222-2222-2222-2222-222222222201', 'a1b2c3d4-e5f6-7890-abcd-ef1234567802', 1,
 'Assess airway patency and breathing effort', 'Look for stridor, wheezing, use of accessory muscles'),
('22222222-2222-2222-2222-222222222202', 'a1b2c3d4-e5f6-7890-abcd-ef1234567802', 2,
 'Measure oxygen saturation and apply oxygen if needed', 'Target SpO2 94-98% (88-92% for COPD patients)'),
('22222222-2222-2222-2222-222222222203', 'a1b2c3d4-e5f6-7890-abcd-ef1234567802', 3,
 'Assess level of consciousness', 'AVPU scale'),
('22222222-2222-2222-2222-222222222204', 'a1b2c3d4-e5f6-7890-abcd-ef1234567802', 4,
 'Obtain history of respiratory conditions', 'Asthma, COPD, previous intubation');

INSERT INTO step_actions (step_id, action) VALUES
('22222222-2222-2222-2222-222222222201', 'Position patient upright'),
('22222222-2222-2222-2222-222222222201', 'Prepare for possible intubation if severe'),
('22222222-2222-2222-2222-222222222202', 'Administer nebulized bronchodilators if wheezing');

INSERT INTO protocol_red_flags (protocol_id, red_flag) VALUES
('a1b2c3d4-e5f6-7890-abcd-ef1234567802', 'SpO2 < 90% on room air'),
('a1b2c3d4-e5f6-7890-abcd-ef1234567802', 'Unable to speak in full sentences'),
('a1b2c3d4-e5f6-7890-abcd-ef1234567802', 'Cyanosis'),
('a1b2c3d4-e5f6-7890-abcd-ef1234567802', 'Altered mental status'),
('a1b2c3d4-e5f6-7890-abcd-ef1234567802', 'Silent chest on auscultation');

-- Abdominal pain protocol
INSERT INTO triage_protocols (id, code, name, description, category, version, active)
VALUES (
    'a1b2c3d4-e5f6-7890-abcd-ef1234567803',
    'RETTS-ABD',
    'Abdominal Pain Protocol',
    'Assessment protocol for patients presenting with abdominal pain.',
    'Gastrointestinal',
    '1.0',
    true
);

INSERT INTO protocol_steps (id, protocol_id, step_order, instruction, assessment_criteria) VALUES
('33333333-3333-3333-3333-333333333301', 'a1b2c3d4-e5f6-7890-abcd-ef1234567803', 1,
 'Assess vital signs and hemodynamic stability', 'Check for signs of shock or peritonitis'),
('33333333-3333-3333-3333-333333333302', 'a1b2c3d4-e5f6-7890-abcd-ef1234567803', 2,
 'Characterize the pain using OPQRST', 'Onset, location, radiation, quality, severity'),
('33333333-3333-3333-3333-333333333303', 'a1b2c3d4-e5f6-7890-abcd-ef1234567803', 3,
 'Perform abdominal examination', 'Tenderness, guarding, rigidity, bowel sounds'),
('33333333-3333-3333-3333-333333333304', 'a1b2c3d4-e5f6-7890-abcd-ef1234567803', 4,
 'Check for pregnancy in women of childbearing age', 'Rule out ectopic pregnancy');

INSERT INTO protocol_red_flags (protocol_id, red_flag) VALUES
('a1b2c3d4-e5f6-7890-abcd-ef1234567803', 'Rigid abdomen'),
('a1b2c3d4-e5f6-7890-abcd-ef1234567803', 'Signs of shock (hypotension, tachycardia)'),
('a1b2c3d4-e5f6-7890-abcd-ef1234567803', 'Pulsatile abdominal mass (AAA)'),
('a1b2c3d4-e5f6-7890-abcd-ef1234567803', 'Positive pregnancy test with pain/bleeding'),
('a1b2c3d4-e5f6-7890-abcd-ef1234567803', 'Hematemesis or melena');

-- Trauma protocol
INSERT INTO triage_protocols (id, code, name, description, category, version, active)
VALUES (
    'a1b2c3d4-e5f6-7890-abcd-ef1234567804',
    'RETTS-TRAUMA',
    'Trauma Assessment Protocol',
    'Primary and secondary survey for trauma patients.',
    'Trauma',
    '1.0',
    true
);

INSERT INTO protocol_steps (id, protocol_id, step_order, instruction, assessment_criteria) VALUES
('44444444-4444-4444-4444-444444444401', 'a1b2c3d4-e5f6-7890-abcd-ef1234567804', 1,
 'Primary survey: ABCDE approach', 'Airway, Breathing, Circulation, Disability, Exposure'),
('44444444-4444-4444-4444-444444444402', 'a1b2c3d4-e5f6-7890-abcd-ef1234567804', 2,
 'Cervical spine immobilization if mechanism warrants', 'High-speed collision, fall > 3m, diving injury'),
('44444444-4444-4444-4444-444444444403', 'a1b2c3d4-e5f6-7890-abcd-ef1234567804', 3,
 'Assess Glasgow Coma Scale', 'GCS < 15 requires close monitoring'),
('44444444-4444-4444-4444-444444444404', 'a1b2c3d4-e5f6-7890-abcd-ef1234567804', 4,
 'Secondary survey: Head-to-toe examination', 'Document all injuries');

INSERT INTO step_actions (step_id, action) VALUES
('44444444-4444-4444-4444-444444444401', 'Call trauma team if criteria met'),
('44444444-4444-4444-4444-444444444401', 'Establish IV access with large bore cannulae'),
('44444444-4444-4444-4444-444444444402', 'Apply cervical collar');

INSERT INTO protocol_red_flags (protocol_id, red_flag) VALUES
('a1b2c3d4-e5f6-7890-abcd-ef1234567804', 'GCS < 13'),
('a1b2c3d4-e5f6-7890-abcd-ef1234567804', 'Penetrating injury to head, neck, torso'),
('a1b2c3d4-e5f6-7890-abcd-ef1234567804', 'Flail chest'),
('a1b2c3d4-e5f6-7890-abcd-ef1234567804', 'Pelvic instability'),
('a1b2c3d4-e5f6-7890-abcd-ef1234567804', 'Amputation above wrist/ankle');

-- Neurological emergency protocol
INSERT INTO triage_protocols (id, code, name, description, category, version, active)
VALUES (
    'a1b2c3d4-e5f6-7890-abcd-ef1234567805',
    'RETTS-NEURO',
    'Neurological Emergency Protocol',
    'Assessment protocol for stroke, seizures, and altered consciousness.',
    'Neurological',
    '1.0',
    true
);

INSERT INTO protocol_steps (id, protocol_id, step_order, instruction, assessment_criteria) VALUES
('55555555-5555-5555-5555-555555555501', 'a1b2c3d4-e5f6-7890-abcd-ef1234567805', 1,
 'Assess consciousness level using AVPU/GCS', 'Document baseline and any changes'),
('55555555-5555-5555-5555-555555555502', 'a1b2c3d4-e5f6-7890-abcd-ef1234567805', 2,
 'Perform FAST stroke assessment', 'Face drooping, Arm weakness, Speech difficulty, Time to call'),
('55555555-5555-5555-5555-555555555503', 'a1b2c3d4-e5f6-7890-abcd-ef1234567805', 3,
 'Check blood glucose', 'Hypoglycemia can mimic stroke'),
('55555555-5555-5555-5555-555555555504', 'a1b2c3d4-e5f6-7890-abcd-ef1234567805', 4,
 'Document time of symptom onset', 'Critical for thrombolysis window');

INSERT INTO step_actions (step_id, action) VALUES
('55555555-5555-5555-5555-555555555502', 'If positive FAST: activate stroke team'),
('55555555-5555-5555-5555-555555555502', 'Prepare for CT head');

INSERT INTO protocol_red_flags (protocol_id, red_flag) VALUES
('a1b2c3d4-e5f6-7890-abcd-ef1234567805', 'Sudden onset severe headache'),
('a1b2c3d4-e5f6-7890-abcd-ef1234567805', 'Focal neurological deficit'),
('a1b2c3d4-e5f6-7890-abcd-ef1234567805', 'Seizure > 5 minutes'),
('a1b2c3d4-e5f6-7890-abcd-ef1234567805', 'Signs of raised intracranial pressure'),
('a1b2c3d4-e5f6-7890-abcd-ef1234567805', 'Symptoms within thrombolysis window (< 4.5 hours)');
