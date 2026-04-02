Bra att vi går tillbaka till modulerna nu — det är här allt hänger ihop.
Jag ger dig en ren, konsoliderad och “Claude-ready” modullista för Cura Nexus.

Jag har:
	•	rensat överlapp
	•	tydliggjort ansvar
	•	gjort den kompatibel med bounded contexts
	•	gjort den redo att brytas till REQ-XXX

⸻

🧩 Cura Nexus – Modullista (v2, konsoliderad)

🟢 A. Patientnära vårdmoduler (kärnan)

A1. Patient- och identitetsmodul

Grundidentitet och relationer.
	•	patientidentitet
	•	kontaktuppgifter
	•	närstående/ombud
	•	skyddade uppgifter
	•	samtycken (basnivå)

⸻

A2. Vårdkontakt- och ärendemodul

All vård sker via ett ärende.
	•	vårdkontakter
	•	triagerat inflöde (koppling A4)
	•	status och livscykel
	•	ansvar
	•	koppling till alla andra moduler

👉 Detta är navet i systemet

⸻

A3. Journalmodul

Medicinsk dokumentation.
	•	anteckningar
	•	diagnoser
	•	åtgärder
	•	observationer
	•	tidslinje
	•	signering

👉 Journal = output från flödet

⸻

A4. Triage- och bedömningsmodul

Första medicinska bedömning.
	•	symtombaserad triage
	•	prioritering
	•	vårdnivåbeslut
	•	beslutsstöd

⸻

A5. Boknings- och tidmodul

Matchar behov mot resurser.
	•	bokning/ombokning
	•	väntelistor
	•	kallelser
	•	resurskoppling

⸻

A6. Remiss- och samverkansmodul

Flöden mellan vårdgivare.
	•	remisser
	•	remissbedömning
	•	svar
	•	statusspårning

⸻

A7. Läkemedels- och ordinationsmodul

Läkemedelshantering.
	•	ordination
	•	läkemedelslista
	•	interaktionskontroller
	•	ändringar

⸻

A8. Prov- och svarshanteringsmodul

Lab och analyser.
	•	beställning
	•	svar
	•	avvikelsehantering
	•	uppföljning

⸻

A9. Undersöknings- och bildmodul

Röntgen och diagnostik.
	•	beställning
	•	svar/utlåtande
	•	bildlänkning

⸻

A10. Vårdplaneringsmodul

Långsiktig vård.
	•	vårdplan
	•	mål
	•	insatser
	•	uppföljning

⸻

A11. Vårdmötesmodul

Alla typer av vårdkontakter.
	•	fysiskt
	•	video
	•	telefon
	•	asynkron

⸻

A12. Patientkommunikationsmodul

Dialog med patient.
	•	meddelanden
	•	kallelser
	•	uppföljning
	•	egenrapportering

⸻

A13. Formulär- och insamlingsmodul

Strukturerad input.
	•	anamnes
	•	screening
	•	formulär
	•	validering

⸻

🟡 B. Operativa verksamhetsmoduler

B1. Uppgifts- och arbetsflödesmodul

Execution layer.
	•	tasks
	•	bevakningar
	•	påminnelser
	•	delegering

👉 Binder ihop beslut → handling

⸻

B2. Resurs- och kapacitetsmodul

Intern optimering.
	•	bemanning
	•	rum/utrustning
	•	beläggning
	•	kapacitetsplanering

⸻

B3. Ekonomi- och ersättningsmodul

Ekonomisk koppling.
	•	patientavgifter
	•	ersättningsdata
	•	fakturaunderlag

⸻

B4. Kodnings- och klassificeringsmodul

Standardisering.
	•	ICD
	•	KVÅ
	•	klassificering
	•	kvalitetssäkring

⸻

B5. Intygs- och dokumentmodul

Formella dokument.
	•	intyg
	•	dokumentutbyte
	•	signering

⸻

B6. Kvalitets- och avvikelsemodul

Patientsäkerhet.
	•	avvikelser
	•	incidenter
	•	analys
	•	förbättring

⸻

🔵 C. Styrning, säkerhet och efterlevnad

C1. Samtyckes- och åtkomstmodul

Dataskydd.
	•	samtycke
	•	spärrar
	•	nödöppning

⸻

C2. Behörighets- och rollmodul

Access control.
	•	RBAC + ABAC
	•	delegation
	•	kontextstyrning

⸻

C3. Logg- och auditmodul

Full spårbarhet.
	•	åtkomstlogg
	•	ändringslogg
	•	revisionsstöd

⸻

C4. Masterdata- och regelverksmodul

Sanningens källa.
	•	kodverk
	•	organisation
	•	regler
	•	mallar

⸻

C5. Administrationsmodul

Systemstyrning.
	•	konfiguration
	•	köer
	•	inställningar

⸻

🟣 D. Intelligens och uppföljning

D1. Beslutsstödsmodul

Regler och riktlinjer.
	•	varningar
	•	rekommendationer
	•	vårdprogram

⸻

D2. AI- och automationsmodul

AI som tjänstelager.
	•	sammanfattning
	•	kodningsstöd
	•	prediktion
	•	automation

⸻

D3. Analys- och rapportmodul

Styrning.
	•	dashboards
	•	KPI
	•	uppföljning

⸻

D4. Notifierings- och händelsemodul

Event motor.
	•	notifieringar
	•	event publishing
	•	prenumerationer

⸻

⚙️ E. Plattform

E1. Integrationsmodul

All extern kommunikation.
	•	API gateway
	•	FHIR
	•	externa system
	•	köer

⸻

E2. Plattformstjänster

Teknisk bas.
	•	auth
	•	logging
	•	storage
	•	cache
	•	observability

⸻

🔥 Viktig observation (du kommer gilla denna)

Om vi kokar ner allt:

👉 3 verkliga kärnor i systemet
	1.	Vårdkontakt (A2) – allt börjar här
	2.	Journal (A3) – allt dokumenteras här
	3.	Uppgifter (B1) – allt genomförs här

Allt annat är:
	•	input
	•	output
	•	styrning
	•	optimering

⸻

🧠 Rekommenderad första implementation (för Claude)

För att testa hela modellen:

👉 Börja med dessa moduler:
	1.	A1 – Patient
	2.	A2 – Vårdkontakt
	3.	A3 – Journal
	4.	B1 – Uppgifter
	5.	C2 – Behörighet
	6.	E1 – Integration

Det räcker för att bygga ett minimalt fungerande vårdsystem.

⸻
