-- V1: Create coding schema for medical classification systems
-- Supports ICD-10-SE, KVÅ, ATC and other Swedish healthcare code systems

-- Code systems metadata
CREATE TABLE code_systems (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type VARCHAR(20) NOT NULL UNIQUE,
    version VARCHAR(20) NOT NULL,
    valid_from DATE NOT NULL,
    valid_to DATE,
    source_url VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE
);

-- ICD-10-SE Diagnosis codes
CREATE TABLE diagnosis_codes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(10) NOT NULL,
    display_name VARCHAR(500) NOT NULL,
    swedish_name VARCHAR(500) NOT NULL,
    chapter VARCHAR(5),
    chapter_name VARCHAR(200),
    block VARCHAR(20),
    parent_code VARCHAR(10),
    level INTEGER,
    is_leaf BOOLEAN NOT NULL DEFAULT TRUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    search_text VARCHAR(1000),
    gender_restriction VARCHAR(1),
    age_min INTEGER,
    age_max INTEGER
);

-- KVÅ Procedure codes
CREATE TABLE procedure_codes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(10) NOT NULL,
    display_name VARCHAR(500) NOT NULL,
    swedish_name VARCHAR(500) NOT NULL,
    category VARCHAR(5),
    category_name VARCHAR(200),
    parent_code VARCHAR(10),
    level INTEGER,
    is_leaf BOOLEAN NOT NULL DEFAULT TRUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    search_text VARCHAR(1000),
    performer_type VARCHAR(20),
    requires_laterality BOOLEAN NOT NULL DEFAULT FALSE
);

-- ATC Medication codes
CREATE TABLE medication_codes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(10) NOT NULL,
    display_name VARCHAR(500) NOT NULL,
    swedish_name VARCHAR(500) NOT NULL,
    level INTEGER NOT NULL,
    parent_code VARCHAR(10),
    anatomical_group VARCHAR(1),
    therapeutic_group VARCHAR(3),
    pharmacological_group VARCHAR(4),
    chemical_group VARCHAR(5),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    search_text VARCHAR(1000),
    ddd_value DOUBLE PRECISION,
    ddd_unit VARCHAR(20),
    administration_route VARCHAR(10)
);

-- Indexes for diagnosis codes
CREATE INDEX idx_diagnosis_code ON diagnosis_codes(code);
CREATE INDEX idx_diagnosis_chapter ON diagnosis_codes(chapter);
CREATE INDEX idx_diagnosis_search ON diagnosis_codes(search_text);
CREATE INDEX idx_diagnosis_active ON diagnosis_codes(active) WHERE active = TRUE;

-- Indexes for procedure codes
CREATE INDEX idx_procedure_code ON procedure_codes(code);
CREATE INDEX idx_procedure_category ON procedure_codes(category);
CREATE INDEX idx_procedure_search ON procedure_codes(search_text);
CREATE INDEX idx_procedure_active ON procedure_codes(active) WHERE active = TRUE;

-- Indexes for medication codes
CREATE INDEX idx_medication_code ON medication_codes(code);
CREATE INDEX idx_medication_level ON medication_codes(level);
CREATE INDEX idx_medication_search ON medication_codes(search_text);
CREATE INDEX idx_medication_active ON medication_codes(active) WHERE active = TRUE;
CREATE INDEX idx_medication_anatomical ON medication_codes(anatomical_group);

-- Insert code systems metadata
INSERT INTO code_systems (type, version, valid_from, source_url, active) VALUES
('ICD10_SE', '2024', '2024-01-01', 'https://www.socialstyrelsen.se/statistik-och-data/klassifikationer-och-koder/icd-10/', TRUE),
('KVA', '2024', '2024-01-01', 'https://www.socialstyrelsen.se/statistik-och-data/klassifikationer-och-koder/kva/', TRUE),
('ATC', '2024', '2024-01-01', 'https://www.whocc.no/atc/', TRUE);

-- Insert sample ICD-10-SE diagnosis codes (common diagnoses)
INSERT INTO diagnosis_codes (code, display_name, swedish_name, chapter, chapter_name, level, is_leaf, search_text) VALUES
-- Infektionssjukdomar (A00-B99)
('A00-B99', 'Infectious and parasitic diseases', 'Vissa infektionssjukdomar och parasitsjukdomar', 'I', 'Infektionssjukdomar', 1, FALSE, 'a00-b99 infektionssjukdomar parasitsjukdomar'),
('A09', 'Infectious gastroenteritis and colitis', 'Infektiös gastroenterit och kolit', 'I', 'Infektionssjukdomar', 2, TRUE, 'a09 infektiös gastroenterit kolit magsjuka diarré'),
('A41', 'Other sepsis', 'Annan sepsis', 'I', 'Infektionssjukdomar', 2, FALSE, 'a41 sepsis blodförgiftning'),
('A41.9', 'Sepsis, unspecified', 'Sepsis, ospecificerad', 'I', 'Infektionssjukdomar', 3, TRUE, 'a41.9 sepsis ospecificerad blodförgiftning'),

-- Tumörer (C00-D48)
('C00-D48', 'Neoplasms', 'Tumörer', 'II', 'Tumörer', 1, FALSE, 'c00-d48 tumörer cancer neoplasm'),
('C34', 'Malignant neoplasm of bronchus and lung', 'Malign tumör i bronk och lunga', 'II', 'Tumörer', 2, FALSE, 'c34 lungcancer bronkcancer malign tumör'),
('C34.9', 'Malignant neoplasm of unspecified part of bronchus or lung', 'Malign tumör i bronk eller lunga, ospecificerad lokalisation', 'II', 'Tumörer', 3, TRUE, 'c34.9 lungcancer ospecificerad'),
('C50', 'Malignant neoplasm of breast', 'Malign tumör i bröstkörtel', 'II', 'Tumörer', 2, FALSE, 'c50 bröstcancer mammarcancer'),
('C50.9', 'Malignant neoplasm of breast, unspecified', 'Malign tumör i bröstkörtel, ospecificerad', 'II', 'Tumörer', 3, TRUE, 'c50.9 bröstcancer ospecificerad'),

-- Endokrina sjukdomar (E00-E90)
('E00-E90', 'Endocrine, nutritional and metabolic diseases', 'Endokrina sjukdomar, nutritionsrubbningar och ämnesomsättningssjukdomar', 'IV', 'Endokrina sjukdomar', 1, FALSE, 'e00-e90 endokrina sjukdomar diabetes'),
('E10', 'Type 1 diabetes mellitus', 'Diabetes mellitus typ 1', 'IV', 'Endokrina sjukdomar', 2, FALSE, 'e10 diabetes typ 1 insulinberoende'),
('E10.9', 'Type 1 diabetes mellitus without complications', 'Diabetes mellitus typ 1 utan komplikationer', 'IV', 'Endokrina sjukdomar', 3, TRUE, 'e10.9 diabetes typ 1 utan komplikationer'),
('E11', 'Type 2 diabetes mellitus', 'Diabetes mellitus typ 2', 'IV', 'Endokrina sjukdomar', 2, FALSE, 'e11 diabetes typ 2'),
('E11.9', 'Type 2 diabetes mellitus without complications', 'Diabetes mellitus typ 2 utan komplikationer', 'IV', 'Endokrina sjukdomar', 3, TRUE, 'e11.9 diabetes typ 2 utan komplikationer'),

-- Psykiska sjukdomar (F00-F99)
('F00-F99', 'Mental and behavioural disorders', 'Psykiska sjukdomar och syndrom samt beteendestörningar', 'V', 'Psykiska sjukdomar', 1, FALSE, 'f00-f99 psykiska sjukdomar psykiatri'),
('F32', 'Depressive episode', 'Depressiv episod', 'V', 'Psykiska sjukdomar', 2, FALSE, 'f32 depression depressiv episod'),
('F32.0', 'Mild depressive episode', 'Lindrig depressiv episod', 'V', 'Psykiska sjukdomar', 3, TRUE, 'f32.0 lindrig depression'),
('F32.1', 'Moderate depressive episode', 'Medelsvår depressiv episod', 'V', 'Psykiska sjukdomar', 3, TRUE, 'f32.1 medelsvår depression'),
('F32.2', 'Severe depressive episode without psychotic symptoms', 'Svår depressiv episod utan psykotiska symtom', 'V', 'Psykiska sjukdomar', 3, TRUE, 'f32.2 svår depression'),
('F41', 'Other anxiety disorders', 'Andra ångestsyndrom', 'V', 'Psykiska sjukdomar', 2, FALSE, 'f41 ångest ångestsyndrom'),
('F41.1', 'Generalized anxiety disorder', 'Generaliserat ångestsyndrom', 'V', 'Psykiska sjukdomar', 3, TRUE, 'f41.1 generaliserat ångestsyndrom gad'),

-- Cirkulationsorganens sjukdomar (I00-I99)
('I00-I99', 'Diseases of the circulatory system', 'Cirkulationsorganens sjukdomar', 'IX', 'Cirkulationsorganens sjukdomar', 1, FALSE, 'i00-i99 cirkulationsorganens sjukdomar hjärta kärl'),
('I10', 'Essential (primary) hypertension', 'Essentiell (primär) hypertoni', 'IX', 'Cirkulationsorganens sjukdomar', 2, TRUE, 'i10 hypertoni högt blodtryck essentiell primär'),
('I21', 'Acute myocardial infarction', 'Akut hjärtinfarkt', 'IX', 'Cirkulationsorganens sjukdomar', 2, FALSE, 'i21 hjärtinfarkt akut myokardinfarkt'),
('I21.9', 'Acute myocardial infarction, unspecified', 'Akut hjärtinfarkt, ospecificerad', 'IX', 'Cirkulationsorganens sjukdomar', 3, TRUE, 'i21.9 hjärtinfarkt ospecificerad'),
('I25', 'Chronic ischaemic heart disease', 'Kronisk ischemisk hjärtsjukdom', 'IX', 'Cirkulationsorganens sjukdomar', 2, FALSE, 'i25 kronisk ischemisk hjärtsjukdom'),
('I25.9', 'Chronic ischaemic heart disease, unspecified', 'Kronisk ischemisk hjärtsjukdom, ospecificerad', 'IX', 'Cirkulationsorganens sjukdomar', 3, TRUE, 'i25.9 kronisk ischemisk hjärtsjukdom ospecificerad'),
('I48', 'Atrial fibrillation and flutter', 'Förmaksflimmer och förmaksfladder', 'IX', 'Cirkulationsorganens sjukdomar', 2, FALSE, 'i48 förmaksflimmer förmaksfladder'),
('I48.9', 'Atrial fibrillation and atrial flutter, unspecified', 'Förmaksflimmer och förmaksfladder, ospecificerat', 'IX', 'Cirkulationsorganens sjukdomar', 3, TRUE, 'i48.9 förmaksflimmer ospecificerat'),
('I63', 'Cerebral infarction', 'Hjärninfarkt', 'IX', 'Cirkulationsorganens sjukdomar', 2, FALSE, 'i63 hjärninfarkt stroke'),
('I63.9', 'Cerebral infarction, unspecified', 'Hjärninfarkt, ospecificerad', 'IX', 'Cirkulationsorganens sjukdomar', 3, TRUE, 'i63.9 hjärninfarkt stroke ospecificerad'),

-- Andningsorganens sjukdomar (J00-J99)
('J00-J99', 'Diseases of the respiratory system', 'Andningsorganens sjukdomar', 'X', 'Andningsorganens sjukdomar', 1, FALSE, 'j00-j99 andningsorganens sjukdomar lungor'),
('J06', 'Acute upper respiratory infections', 'Akut övre luftvägsinfektion', 'X', 'Andningsorganens sjukdomar', 2, FALSE, 'j06 övre luftvägsinfektion förkylning'),
('J06.9', 'Acute upper respiratory infection, unspecified', 'Akut övre luftvägsinfektion, ospecificerad', 'X', 'Andningsorganens sjukdomar', 3, TRUE, 'j06.9 övre luftvägsinfektion förkylning ospecificerad'),
('J18', 'Pneumonia, unspecified organism', 'Pneumoni, ospecificerad', 'X', 'Andningsorganens sjukdomar', 2, FALSE, 'j18 pneumoni lunginflammation'),
('J18.9', 'Pneumonia, unspecified', 'Pneumoni, ospecificerad', 'X', 'Andningsorganens sjukdomar', 3, TRUE, 'j18.9 pneumoni lunginflammation ospecificerad'),
('J44', 'Other chronic obstructive pulmonary disease', 'Annan kronisk obstruktiv lungsjukdom', 'X', 'Andningsorganens sjukdomar', 2, FALSE, 'j44 kol kronisk obstruktiv lungsjukdom'),
('J44.9', 'Chronic obstructive pulmonary disease, unspecified', 'Kronisk obstruktiv lungsjukdom, ospecificerad', 'X', 'Andningsorganens sjukdomar', 3, TRUE, 'j44.9 kol ospecificerad'),
('J45', 'Asthma', 'Astma', 'X', 'Andningsorganens sjukdomar', 2, FALSE, 'j45 astma'),
('J45.9', 'Asthma, unspecified', 'Astma, ospecificerad', 'X', 'Andningsorganens sjukdomar', 3, TRUE, 'j45.9 astma ospecificerad'),

-- Muskuloskeletala sjukdomar (M00-M99)
('M00-M99', 'Diseases of the musculoskeletal system', 'Sjukdomar i muskuloskeletala systemet och bindväven', 'XIII', 'Muskuloskeletala sjukdomar', 1, FALSE, 'm00-m99 muskuloskeletala sjukdomar'),
('M54', 'Dorsalgia', 'Ryggvärk', 'XIII', 'Muskuloskeletala sjukdomar', 2, FALSE, 'm54 ryggvärk dorsalgi'),
('M54.5', 'Low back pain', 'Ländryggssmärta', 'XIII', 'Muskuloskeletala sjukdomar', 3, TRUE, 'm54.5 ländryggssmärta lumbago'),

-- Skador (S00-T98)
('S00-T98', 'Injury, poisoning and certain other consequences of external causes', 'Skador, förgiftningar och vissa andra följder av yttre orsaker', 'XIX', 'Skador och förgiftningar', 1, FALSE, 's00-t98 skador förgiftningar'),
('S72', 'Fracture of femur', 'Fraktur på lårben', 'XIX', 'Skador och förgiftningar', 2, FALSE, 's72 fraktur lårben femur'),
('S72.0', 'Fracture of neck of femur', 'Fraktur på lårbenshalsen', 'XIX', 'Skador och förgiftningar', 3, TRUE, 's72.0 höftfraktur lårbenshals');

-- Insert sample KVÅ procedure codes
INSERT INTO procedure_codes (code, display_name, swedish_name, category, category_name, level, is_leaf, search_text, performer_type) VALUES
-- Diagnostiska åtgärder
('AA', 'Diagnostic procedures', 'Diagnostiska åtgärder', 'A', 'Diagnostik', 1, FALSE, 'aa diagnostiska åtgärder', 'ANY'),
('AA000', 'History taking', 'Anamnesupptagning', 'A', 'Diagnostik', 2, TRUE, 'aa000 anamnesupptagning sjukhistoria', 'ANY'),
('AE015', 'ECG examination', 'EKG-undersökning', 'A', 'Diagnostik', 2, TRUE, 'ae015 ekg elektrokardiografi hjärta', 'ANY'),

-- Terapeutiska åtgärder
('DA', 'Therapeutic procedures - nervous system', 'Terapeutiska åtgärder - nervsystemet', 'D', 'Terapi', 1, FALSE, 'da terapeutiska åtgärder nervsystemet', 'PHYSICIAN'),

-- Kirurgiska åtgärder
('NF', 'Surgical procedures - hip', 'Kirurgiska åtgärder - höft', 'N', 'Kirurgi', 1, FALSE, 'nf kirurgiska åtgärder höft', 'PHYSICIAN'),
('NFB09', 'Primary total hip replacement', 'Primär total höftledsplastik', 'N', 'Kirurgi', 2, TRUE, 'nfb09 höftledsplastik höftprotes total', 'PHYSICIAN'),
('NFB19', 'Primary partial hip replacement', 'Primär partiell höftledsplastik', 'N', 'Kirurgi', 2, TRUE, 'nfb19 höftledsplastik partiell halvprotes', 'PHYSICIAN'),

-- Omvårdnadsåtgärder
('QA', 'Nursing procedures - general', 'Omvårdnadsåtgärder - allmänt', 'Q', 'Omvårdnad', 1, FALSE, 'qa omvårdnadsåtgärder', 'NURSE'),
('QA001', 'Blood pressure measurement', 'Blodtrycksmätning', 'Q', 'Omvårdnad', 2, TRUE, 'qa001 blodtrycksmätning', 'NURSE'),
('QA009', 'Assessment of general health status', 'Bedömning av allmäntillstånd', 'Q', 'Omvårdnad', 2, TRUE, 'qa009 bedömning allmäntillstånd', 'ANY'),

-- Undersökningar
('UX', 'Radiology - general', 'Röntgen - allmänt', 'U', 'Radiologi', 1, FALSE, 'ux röntgen radiologi', 'ANY'),
('UX0AA', 'Plain X-ray chest', 'Slätröntgen thorax', 'U', 'Radiologi', 2, TRUE, 'ux0aa slätröntgen thorax lungröntgen', 'ANY'),
('UX0AB', 'CT scan chest', 'Datortomografi thorax', 'U', 'Radiologi', 2, TRUE, 'ux0ab datortomografi ct thorax lungor', 'ANY'),

-- Provtagning
('AF', 'Laboratory - sampling', 'Provtagning', 'A', 'Diagnostik', 1, FALSE, 'af provtagning laboratorium', 'ANY'),
('AF000', 'Blood sampling, venous', 'Venös blodprovstagning', 'A', 'Diagnostik', 2, TRUE, 'af000 venös blodprovstagning venprov', 'ANY'),

-- Vaccination
('DT', 'Vaccination', 'Vaccination', 'D', 'Terapi', 1, FALSE, 'dt vaccination', 'ANY'),
('DT001', 'Administration of vaccine', 'Vaccination, administrering', 'D', 'Terapi', 2, TRUE, 'dt001 vaccination administrering vaccin', 'ANY'),

-- Samtalsbehandling
('DU', 'Psychotherapy', 'Samtalsbehandling', 'D', 'Terapi', 1, FALSE, 'du samtalsbehandling psykoterapi', 'PSYCHOLOGIST'),
('DU007', 'Cognitive behavioral therapy', 'Kognitiv beteendeterapi', 'D', 'Terapi', 2, TRUE, 'du007 kbt kognitiv beteendeterapi', 'PSYCHOLOGIST');

-- Insert sample ATC medication codes
INSERT INTO medication_codes (code, display_name, swedish_name, level, parent_code, anatomical_group, therapeutic_group, pharmacological_group, chemical_group, search_text, ddd_value, ddd_unit, administration_route) VALUES
-- Anatomical level
('A', 'Alimentary tract and metabolism', 'Matsmältningsorgan och ämnesomsättning', 1, NULL, 'A', NULL, NULL, NULL, 'a matsmältningsorgan ämnesomsättning', NULL, NULL, NULL),
('B', 'Blood and blood forming organs', 'Blod och blodbildande organ', 1, NULL, 'B', NULL, NULL, NULL, 'b blod blodbildande organ', NULL, NULL, NULL),
('C', 'Cardiovascular system', 'Hjärta och kretslopp', 1, NULL, 'C', NULL, NULL, NULL, 'c hjärta kretslopp kardiovaskulärt', NULL, NULL, NULL),
('J', 'Antiinfectives for systemic use', 'Antiinfektiva medel för systemiskt bruk', 1, NULL, 'J', NULL, NULL, NULL, 'j antiinfektiva antibiotika', NULL, NULL, NULL),
('N', 'Nervous system', 'Nervsystemet', 1, NULL, 'N', NULL, NULL, NULL, 'n nervsystemet', NULL, NULL, NULL),
('R', 'Respiratory system', 'Andningsorgan', 1, NULL, 'R', NULL, NULL, NULL, 'r andningsorgan lungor', NULL, NULL, NULL),

-- Therapeutic level examples
('A10', 'Drugs used in diabetes', 'Diabetesmedel', 2, 'A', 'A', 'A10', NULL, NULL, 'a10 diabetesmedel diabetes', NULL, NULL, NULL),
('B01', 'Antithrombotic agents', 'Antitrombotiska medel', 2, 'B', 'B', 'B01', NULL, NULL, 'b01 antitrombotiska blodförtunnande', NULL, NULL, NULL),
('C03', 'Diuretics', 'Diuretika', 2, 'C', 'C', 'C03', NULL, NULL, 'c03 diuretika vätskedrivande', NULL, NULL, NULL),
('C07', 'Beta blocking agents', 'Betablockerare', 2, 'C', 'C', 'C07', NULL, NULL, 'c07 betablockerare betablockad', NULL, NULL, NULL),
('C09', 'Agents acting on renin-angiotensin system', 'Medel som påverkar renin-angiotensinsystemet', 2, 'C', 'C', 'C09', NULL, NULL, 'c09 ace-hämmare arb raas', NULL, NULL, NULL),
('J01', 'Antibacterials for systemic use', 'Antibakteriella medel för systemiskt bruk', 2, 'J', 'J', 'J01', NULL, NULL, 'j01 antibiotika antibakteriella', NULL, NULL, NULL),
('N02', 'Analgesics', 'Analgetika', 2, 'N', 'N', 'N02', NULL, NULL, 'n02 analgetika smärtstillande', NULL, NULL, NULL),
('N05', 'Psycholeptics', 'Psykoleptika', 2, 'N', 'N', 'N05', NULL, NULL, 'n05 psykoleptika lugnande', NULL, NULL, NULL),
('N06', 'Psychoanaleptics', 'Psykoanaleptika', 2, 'N', 'N', 'N06', NULL, NULL, 'n06 psykoanaleptika antidepressiva', NULL, NULL, NULL),
('R03', 'Drugs for obstructive airway diseases', 'Medel vid obstruktiva lungsjukdomar', 2, 'R', 'R', 'R03', NULL, NULL, 'r03 astma kol obstruktiva lungsjukdomar', NULL, NULL, NULL),

-- Chemical substance level (level 5) - common medications
('A10BA02', 'Metformin', 'Metformin', 5, 'A10BA', 'A', 'A10', 'A10B', 'A10BA', 'a10ba02 metformin diabetes tabletter', 2000, 'mg', 'O'),
('B01AC06', 'Acetylsalicylic acid', 'Acetylsalicylsyra', 5, 'B01AC', 'B', 'B01', 'B01A', 'B01AC', 'b01ac06 acetylsalicylsyra aspirin trombyl', 1000, 'mg', 'O'),
('B01AF01', 'Rivaroxaban', 'Rivaroxaban', 5, 'B01AF', 'B', 'B01', 'B01A', 'B01AF', 'b01af01 rivaroxaban xarelto antikoagulantia', 20, 'mg', 'O'),
('C03CA01', 'Furosemide', 'Furosemid', 5, 'C03CA', 'C', 'C03', 'C03C', 'C03CA', 'c03ca01 furosemid furix diuretika vätskedrivande', 40, 'mg', 'O'),
('C07AB02', 'Metoprolol', 'Metoprolol', 5, 'C07AB', 'C', 'C07', 'C07A', 'C07AB', 'c07ab02 metoprolol seloken betablockerare', 150, 'mg', 'O'),
('C09AA02', 'Enalapril', 'Enalapril', 5, 'C09AA', 'C', 'C09', 'C09A', 'C09AA', 'c09aa02 enalapril renitec ace-hämmare', 10, 'mg', 'O'),
('C09CA01', 'Losartan', 'Losartan', 5, 'C09CA', 'C', 'C09', 'C09C', 'C09CA', 'c09ca01 losartan cozaar arb', 50, 'mg', 'O'),
('J01CA04', 'Amoxicillin', 'Amoxicillin', 5, 'J01CA', 'J', 'J01', 'J01C', 'J01CA', 'j01ca04 amoxicillin amimox antibiotika penicillin', 1000, 'mg', 'O'),
('J01FA01', 'Erythromycin', 'Erytromycin', 5, 'J01FA', 'J', 'J01', 'J01F', 'J01FA', 'j01fa01 erytromycin ery-max makrolid', 1000, 'mg', 'O'),
('N02AA01', 'Morphine', 'Morfin', 5, 'N02AA', 'N', 'N02', 'N02A', 'N02AA', 'n02aa01 morfin opioid smärtstillande', 100, 'mg', 'O'),
('N02BE01', 'Paracetamol', 'Paracetamol', 5, 'N02BE', 'N', 'N02', 'N02B', 'N02BE', 'n02be01 paracetamol alvedon panodil smärtstillande', 3000, 'mg', 'O'),
('N05BA01', 'Diazepam', 'Diazepam', 5, 'N05BA', 'N', 'N05', 'N05B', 'N05BA', 'n05ba01 diazepam stesolid bensodiazepin ångest', 10, 'mg', 'O'),
('N06AB04', 'Citalopram', 'Citalopram', 5, 'N06AB', 'N', 'N06', 'N06A', 'N06AB', 'n06ab04 citalopram cipramil ssri antidepressivum', 20, 'mg', 'O'),
('N06AB06', 'Sertraline', 'Sertralin', 5, 'N06AB', 'N', 'N06', 'N06A', 'N06AB', 'n06ab06 sertralin zoloft ssri antidepressivum', 50, 'mg', 'O'),
('R03AC02', 'Salbutamol', 'Salbutamol', 5, 'R03AC', 'R', 'R03', 'R03A', 'R03AC', 'r03ac02 salbutamol ventoline astma bronkdilaterare', 0.8, 'mg', 'Inhal'),
('R03AK06', 'Salmeterol and fluticasone', 'Salmeterol och flutikason', 5, 'R03AK', 'R', 'R03', 'R03A', 'R03AK', 'r03ak06 salmeterol flutikason seretide astma inhalation', NULL, NULL, 'Inhal');

-- Comments
COMMENT ON TABLE code_systems IS 'Metadata for medical classification systems';
COMMENT ON TABLE diagnosis_codes IS 'ICD-10-SE diagnosis codes for Swedish healthcare';
COMMENT ON TABLE procedure_codes IS 'KVÅ procedure codes for Swedish healthcare';
COMMENT ON TABLE medication_codes IS 'ATC medication classification codes';
