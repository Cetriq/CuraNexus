Bra. Då översätter vi visionen till arkitektur- och designprinciper för Cura Nexus – sådana som både kan styra utveckling (Claude) och fungera i upphandling.

De ska vara:
	•	tydliga
	•	styrande (inte fluff)
	•	möjliga att validera
	•	användbara i CLAUDE.md

⸻

Principer – Cura Nexus

1. Vårdflödet är primärt

Systemet ska modelleras utifrån vårdprocesser, inte dokument.
	•	Alla funktioner ska kunna kopplas till ett vårdflöde
	•	Journaldata ska uppstå som en konsekvens av aktiviteter
	•	Det ska vara möjligt att följa en patients resa som ett sammanhängande flöde

👉 Implikation: inga funktioner byggs “för journalen” isolerat

⸻

2. Strukturerad data som standard

All information ska i första hand vara strukturerad och maskinläsbar.
	•	Fri text är ett komplement, inte primär lagring
	•	Data ska kunna återanvändas i beslutsstöd, analys och uppföljning
	•	Datamodellen ska vara konsekvent över hela plattformen

👉 Implikation: API och datamodell styr implementationen

⸻

3. En källa till sanning per informationsobjekt

Varje typ av information ska ha en tydlig ägare.
	•	Ingen duplicering av kärndata
	•	Synkronisering ska undvikas – referenser ska användas
	•	Masterdata ska vara centraliserad

👉 Implikation: integrationsdesign blir avgörande

⸻

4. Kontextbaserad åtkomst (Zero Trust i praktiken)

Åtkomst till information ska styras av kontext, inte bara roll.
	•	Vårdrelation ska vara grund för åtkomst
	•	Tid, plats och uppdrag ska påverka behörighet
	•	Alla åtkomster ska vara spårbara

👉 Implikation: RBAC räcker inte → ABAC krävs

⸻

5. Säkerhet och spårbarhet är inbyggt från början

Systemet ska vara designat för revision, inte kompletteras i efterhand.
	•	Alla ändringar ska vara spårbara
	•	Alla åtkomster ska loggas
	•	Alla kritiska beslut ska kunna rekonstrueras

👉 Implikation: audit är en kärnfunktion, inte en sidofunktion

⸻

6. API-first som grundprincip

All funktionalitet ska vara tillgänglig via väldefinierade API:er.
	•	UI får aldrig vara den enda vägen till funktion
	•	Alla integrationer ska gå via standardiserade gränssnitt
	•	API-kontrakt ska definieras före implementation

👉 Implikation: OpenAPI är förstaklassartefakt

⸻

7. Modulär arkitektur med tydliga gränser

Systemet ska delas upp i tydliga domäner (bounded contexts).
	•	Moduler ska vara löst kopplade
	•	Interna beroenden ska minimeras
	•	Kommunikation mellan moduler ska ske via definierade kontrakt

👉 Implikation: möjliggör skalning och utbytbarhet

⸻

8. Event-driven där det ger värde

Systemet ska reagera på händelser, inte bara anrop.
	•	Viktiga förändringar ska generera events
	•	Andra moduler ska kunna prenumerera
	•	Event ska vara spårbara och versionerade

👉 Implikation: stöd för automation och realtid

⸻

9. AI som assistent – aldrig som ensam beslutsfattare

AI ska stödja, inte ersätta, medicinska beslut.
	•	AI ska vara transparent och förklarbar
	•	AI ska kunna stängas av per funktion
	•	AI får inte träna på data utan explicit godkännande

👉 Implikation: AI integreras som tjänstelager, inte kärna

⸻

10. Människa i centrum – minimal administration

Systemet ska minska, inte öka, administrativ belastning.
	•	Information ska registreras en gång
	•	Uppgifter ska automatiseras där möjligt
	•	UI ska vara anpassat efter roll och situation

👉 Implikation: workflow och UX är kritiska delar

⸻

11. Plattform, inte produkt

Cura Nexus ska vara en plattform för vidareutveckling.
	•	Tredjepartsfunktioner ska kunna kopplas in
	•	Intern utveckling ska kunna ske utan att bryta kärnan
	•	Funktioner ska kunna ersättas utan total ombyggnad

👉 Implikation: liknar din DIS-tools-tanke

⸻

12. Nationell interoperabilitet som krav, inte tillval

Systemet ska fungera i svensk vård från dag ett.
	•	Stöd för nationella tjänster
	•	Standardiserade format
	•	Ingen inlåsning i proprietära strukturer

👉 Implikation: FHIR och öppna standarder

⸻

13. Skalbarhet från start

Systemet ska designas för stora organisationer.
	•	Ska fungera för 100 000 användare
	•	Ska hantera hög samtidighet
	•	Ska kunna delas upp per region/organisation

👉 Implikation: stateless tjänster + skalbar databasstrategi

⸻

14. Testbarhet och verifierbarhet

All funktionalitet ska kunna testas automatiskt.
	•	API:er ska vara testbara utan UI
	•	Testdata ska kunna genereras
	•	Regressioner ska upptäckas automatiskt

👉 Implikation: test är en del av definition of done

⸻

15. Förändring utan kaos

Systemet ska kunna förändras utan att destabiliseras.
	•	Versionshantering av API:er
	•	Migrationsstrategi för data
	•	Bakåtkompatibilitet där det krävs

👉 Implikation: förändring är designad, inte undantag

⸻

16. AI-driven utveckling under kontroll

Utvecklingen ska vara AI-driven men styrd.
	•	All kod ska följa definierade regler
	•	AI ska arbeta utifrån kontrakt och mallar
	•	Alla förändringar ska vara spårbara

👉 Implikation: Claude Runtime blir en kärndel

⸻

🔥 Sammanfattning (det här är kärnan)

Cura Nexus bygger på fyra fundament:
	1.	Flöde före dokument
	2.	Struktur före frihet
	3.	Säkerhet före bekvämlighet
	4.	Plattform före system

⸻
