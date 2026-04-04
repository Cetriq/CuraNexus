# Cura Nexus – Arkitektur- och designprinciper

Version: 2.0
Senast uppdaterad: 2026-04-04

---

## Sammanfattning

Cura Nexus bygger på fyra fundament:

1. **Flöde före dokument** – vårdprocessen styr, journalen är output
2. **Struktur före frihet** – maskinläsbar data som standard
3. **Säkerhet före bekvämlighet** – audit och access inbyggt från start
4. **Plattform före system** – utbyggbart och utbytbart

---

## Principer

### 1. Vårdflödet är primärt

Systemet modelleras utifrån vårdprocesser, inte dokument.

- Alla funktioner kopplas till ett vårdflöde
- Journaldata uppstår som konsekvens av aktiviteter
- En patients resa ska kunna följas som sammanhängande flöde

**Implikation:** Inga funktioner byggs "för journalen" isolerat.

---

### 2. Strukturerad data som standard

All information ska i första hand vara strukturerad och maskinläsbar.

- Fri text är komplement, inte primär lagring
- Data ska kunna återanvändas i beslutsstöd, analys och uppföljning
- Datamodellen ska vara konsekvent över hela plattformen

**Implikation:** API och datamodell styr implementationen.

---

### 3. En källa till sanning per informationsobjekt

Varje typ av information har en tydlig ägare.

- Ingen duplicering av kärndata
- Synkronisering undviks – referenser används
- Masterdata är centraliserad

**Implikation:** Integrationsdesign blir avgörande.

---

### 4. Kontextbaserad åtkomst

Åtkomst till information styrs av kontext, inte bara roll.

- Vårdrelation är grund för åtkomst
- Tid, plats och uppdrag påverkar behörighet
- Alla åtkomster är spårbara

**Implikation:** RBAC räcker inte – ABAC krävs.

---

### 5. Säkerhet och spårbarhet inbyggt från början

Systemet är designat för revision, inte kompletterat i efterhand.

- Alla ändringar är spårbara
- Alla åtkomster loggas
- Alla kritiska beslut kan rekonstrueras

**Implikation:** Audit är en kärnfunktion, inte sidofunktion.

---

### 6. API-first som grundprincip

All funktionalitet är tillgänglig via väldefinierade API:er.

- UI är aldrig enda vägen till funktion
- Alla integrationer går via standardiserade gränssnitt
- API-kontrakt definieras före implementation

**Implikation:** OpenAPI är förstaklassartefakt.

---

### 7. Modulär arkitektur med tydliga gränser

Systemet delas upp i tydliga domäner (bounded contexts).

- Moduler är löst kopplade
- Interna beroenden minimeras
- Kommunikation mellan moduler sker via definierade kontrakt

**Implikation:** Möjliggör skalning och utbytbarhet.

---

### 8. Event-driven där det ger värde

Systemet reagerar på händelser, inte bara anrop.

- Viktiga förändringar genererar events
- Andra moduler kan prenumerera
- Events är spårbara och versionerade

**Implikation:** Stöd för automation och realtid.

---

### 9. AI som assistent – aldrig som ensam beslutsfattare

AI stödjer, men ersätter inte, medicinska beslut.

- AI är transparent och förklarbar
- AI kan stängas av per funktion
- AI tränar inte på data utan explicit godkännande

**Implikation:** AI integreras som tjänstelager, inte kärna.

---

### 10. Människa i centrum – minimal administration

Systemet minskar, inte ökar, administrativ belastning.

- Information registreras en gång
- Uppgifter automatiseras där möjligt
- UI anpassas efter roll och situation

**Implikation:** Workflow och UX är kritiska delar.

---

### 11. Plattform, inte produkt

Cura Nexus är en plattform för vidareutveckling.

- Tredjepartsfunktioner kan kopplas in
- Intern utveckling kan ske utan att bryta kärnan
- Funktioner kan ersättas utan total ombyggnad

**Implikation:** Extensibilitet är designat från start.

---

### 12. Nationell interoperabilitet som krav

Systemet fungerar i svensk vård från dag ett.

- Stöd för nationella tjänster
- Standardiserade format
- Ingen inlåsning i proprietära strukturer

**Implikation:** FHIR och öppna standarder.

---

### 13. Skalbarhet från start

Systemet är designat för stora organisationer.

- Fungerar för 100 000 användare
- Hanterar hög samtidighet
- Kan delas upp per region/organisation

**Implikation:** Stateless tjänster + skalbar databasstrategi.

---

### 14. Testbarhet och verifierbarhet

All funktionalitet kan testas automatiskt.

- API:er är testbara utan UI
- Testdata kan genereras
- Regressioner upptäcks automatiskt

**Implikation:** Test är del av definition of done.

---

### 15. Förändring utan kaos

Systemet kan förändras utan destabilisering.

- Versionshantering av API:er
- Migrationsstrategi för data
- Bakåtkompatibilitet där det krävs

**Implikation:** Förändring är designad, inte undantag.

---

### 16. AI-driven utveckling under kontroll

Utvecklingen är AI-driven men styrd.

- All kod följer definierade regler
- AI arbetar utifrån kontrakt och mallar
- Alla förändringar är spårbara

**Implikation:** Claude arbetar inom definierade ramar.

---

## Validering

Varje princip ska kunna valideras:

| Princip | Valideringsfråga |
|---------|------------------|
| 1 | Är funktionen kopplad till ett vårdflöde? |
| 2 | Är data strukturerad och maskinläsbar? |
| 3 | Finns det duplicerad data? |
| 4 | Krävs vårdrelation för åtkomst? |
| 5 | Loggas alla åtkomster? |
| 6 | Finns OpenAPI-specifikation? |
| 7 | Är modulen självständig? |
| 8 | Genereras events vid viktiga händelser? |
| 9 | Kan AI-funktionen stängas av? |
| 10 | Minskar funktionen administrativt arbete? |
| 11 | Kan komponenten bytas ut? |
| 12 | Följer vi svenska standarder? |
| 13 | Fungerar det under last? |
| 14 | Finns automatiska tester? |
| 15 | Är ändringen bakåtkompatibel? |
| 16 | Följer AI-genererad kod våra regler? |

