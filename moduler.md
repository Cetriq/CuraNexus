# Cura Nexus – Modullista

Version: 2.0
Senast uppdaterad: 2026-04-04

---

## Översikt

Cura Nexus är uppbyggt av moduler organiserade i fem kategorier. Varje modul är en bounded context med tydligt definierat ansvar.

**Tre verkliga kärnor:**
1. **Vårdkontakt (A2)** – alla vårdkontakter börjar här
2. **Journal (A3)** – all vård dokumenteras här
3. **Uppgifter (B1)** – all vård genomförs här

Allt annat är: input, output, styrning eller optimering.

---

## A. Patientnära vårdmoduler (kärnan)

### A1. Patient- och identitetsmodul

Grundidentitet och relationer.

- Patientidentitet (personnummer, samordningsnummer)
- Kontaktuppgifter
- Närstående och ombud
- Skyddade uppgifter
- Samtycken (basnivå)

---

### A2. Vårdkontakt- och ärendemodul ⭐ KÄRNA

All vård sker via en vårdkontakt.

- Vårdkontakter och livscykel
- Triagerat inflöde (koppling A4)
- Status och ansvar
- Koppling till alla andra moduler

---

### A3. Journalmodul ⭐ KÄRNA

Medicinsk dokumentation.

- Anteckningar
- Diagnoser (ICD-10-SE)
- Åtgärder (KVÅ)
- Observationer
- Tidslinje
- Signering

---

### A4. Triage- och bedömningsmodul

Första medicinska bedömning.

- Symtombaserad triage (RETTS-inspirerad)
- Prioritering
- Vårdnivåbeslut
- Beslutsstöd

---

### A5. Boknings- och tidmodul

Matchar behov mot resurser.

- Bokning och ombokning
- Väntelistor
- Kallelser
- Resurskoppling

---

### A6. Remiss- och samverkansmodul

Flöden mellan vårdgivare.

- Remisser
- Remissbedömning
- Svar
- Statusspårning

---

### A7. Läkemedels- och ordinationsmodul

Läkemedelshantering.

- Ordination
- Läkemedelslista
- Interaktionskontroller
- Ändringar

---

### A8. Prov- och svarshanteringsmodul

Lab och analyser.

- Beställning
- Svar
- Avvikelsehantering
- Uppföljning

---

### A9. Undersöknings- och bildmodul

Röntgen och diagnostik.

- Beställning
- Svar och utlåtande
- Bildlänkning

---

### A10. Vårdplaneringsmodul

Långsiktig vård.

- Vårdplan
- Mål
- Insatser
- Uppföljning

---

### A11. Vårdmötesmodul

Alla typer av vårdkontakter.

- Fysiskt besök
- Video
- Telefon
- Asynkron kommunikation

---

### A12. Patientkommunikationsmodul

Dialog med patient.

- Meddelanden
- Kallelser
- Uppföljning
- Egenrapportering

---

### A13. Formulär- och insamlingsmodul

Strukturerad input.

- Anamnesformulär
- Screening
- Dynamiska formulär
- Validering

---

## B. Operativa verksamhetsmoduler

### B1. Uppgifts- och arbetsflödesmodul ⭐ KÄRNA

Execution layer.

- Tasks
- Bevakningar
- Påminnelser
- Delegering

---

### B2. Resurs- och kapacitetsmodul

Intern optimering.

- Bemanning
- Rum och utrustning
- Beläggning
- Kapacitetsplanering

---

### B3. Ekonomi- och ersättningsmodul

Ekonomisk koppling.

- Patientavgifter
- Ersättningsdata
- Fakturaunderlag

---

### B4. Kodnings- och klassificeringsmodul

Standardisering.

- ICD-10-SE
- KVÅ
- ATC
- Klassificering och kvalitetssäkring

---

### B5. Intygs- och dokumentmodul

Formella dokument.

- Intyg (FK7263, FK7804, etc.)
- Dokumentutbyte
- Signering

---

### B6. Kvalitets- och avvikelsemodul

Patientsäkerhet.

- Avvikelser
- Incidenter
- Analys
- Förbättring

---

## C. Styrning, säkerhet och efterlevnad

### C1. Samtyckes- och åtkomstmodul

Dataskydd.

- Samtycke
- Spärrar
- Nödöppning

---

### C2. Behörighets- och rollmodul

Access control.

- RBAC + ABAC
- Delegation
- Kontextstyrning

---

### C3. Logg- och auditmodul

Full spårbarhet.

- Åtkomstlogg
- Ändringslogg
- Revisionsstöd

---

### C4. Masterdata- och regelverksmodul

Sanningens källa.

- Kodverk
- Organisation
- Regler
- Mallar

---

### C5. Administrationsmodul

Systemstyrning.

- Konfiguration
- Köer
- Inställningar

---

## D. Intelligens och uppföljning

### D1. Beslutsstödsmodul

Regler och riktlinjer.

- Varningar
- Rekommendationer
- Vårdprogram

---

### D2. AI- och automationsmodul

AI som tjänstelager.

- Sammanfattning
- Kodningsstöd
- Prediktion
- Automation

---

### D3. Analys- och rapportmodul

Styrning.

- Dashboards
- KPI
- Uppföljning

---

### D4. Notifierings- och händelsemodul

Event motor.

- Notifieringar
- Event publishing
- Prenumerationer

---

## E. Plattform

### E1. Integrationsmodul

All extern kommunikation.

- API Gateway
- FHIR R4
- Externa system
- Köer

---

### E2. Plattformstjänster

Teknisk bas.

- Auth
- Logging
- Storage
- Cache
- Observability

---

## Rekommenderad implementation

För ett minimalt fungerande vårdsystem, implementera dessa först:

| Prioritet | Modul | Beskrivning |
|-----------|-------|-------------|
| 1 | A1 Patient | Basdatamodell |
| 2 | A2 Vårdkontakt | Navet i systemet |
| 3 | A3 Journal | Dokumentation |
| 4 | B1 Uppgifter | Execution layer |
| 5 | C2 Behörighet | Access control |
| 6 | E1 Integration | API Gateway och FHIR |

