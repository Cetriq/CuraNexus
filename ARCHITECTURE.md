# Cura Nexus – Arkitekturbeslut

Version: 1.0
Senast uppdaterad: 2026-04-04

---

## Syfte

Detta dokument definierar:
1. Vilka moduler som är kärnmoduler
2. Vilka moduler som är egna tjänster vs bounded contexts
3. Kommunikationsmönster mellan moduler
4. Roadmap för tjänsteseparering

---

## Kärnmoduler (MVP)

Dessa 5 moduler utgör den minimala kärnan för ett fungerande vårdsystem:

| Modul | Kod | Beskrivning | Status |
|-------|-----|-------------|--------|
| Patient | A1 | Patientidentitet och grunddata | ✅ Egen tjänst |
| Vårdkontakt | A2 | Alla vårdkontakter börjar här | ✅ Egen tjänst |
| Journal | A3 | All medicinsk dokumentation | ✅ Egen tjänst |
| Uppgifter | B1 | Execution layer för vårdarbete | ✅ Egen tjänst |
| Behörighet | C2 | Access control (RBAC+ABAC) | ✅ Egen tjänst |

**Princip:** Dessa moduler ska ha djup implementation och fullständig funktionalitet före arbete på andra moduler.

---

## Stödtjänster (Fas 1)

Dessa moduler körs som egna tjänster men har lägre prioritet:

| Modul | Kod | Port | Beskrivning |
|-------|-----|------|-------------|
| Integration | E1 | 8085 | API Gateway och FHIR |
| Triage | A4 | 8087 | RETTS-inspirerad triage |

---

## Bounded Contexts (inte egna tjänster ännu)

Dessa moduler finns som kod men bör konsolideras eller köras i en modulär monolit tills domängränserna stabiliserats:

| Modul | Kod | Nuvarande status | Framtida beslut |
|-------|-----|------------------|-----------------|
| Bokning | A5 | Egen tjänst | Konsolidera med A2 eller behåll |
| Remiss | A6 | Egen tjänst | Behåll som egen |
| Läkemedel | A7 | Egen tjänst | Behåll som egen |
| Lab | A8 | Egen tjänst | Behåll som egen |
| Formulär | A13 | Egen tjänst | Konsolidera med A3 eller behåll |
| Kodning | B4 | Egen tjänst | Behåll som egen |
| Intyg | B5 | Egen tjänst | Konsolidera med A3 eller behåll |
| Samtycke | C1 | Egen tjänst | Behåll som egen |
| Audit | C3 | Egen tjänst | Behåll som egen |
| Notifikation | D4 | Egen tjänst | Behåll som egen |

**Observation:** 17 separata tjänster är operativt tungt. Överväg konsolidering.

---

## Tjänstegränser

### Nuvarande arkitektur (många mikrotjänster)

```
┌─────────────────────────────────────────────────────────────────┐
│                         Frontend (3001)                         │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Integration Gateway (8085)                    │
│                         FHIR R4 + API                           │
└─────────────────────────────────────────────────────────────────┘
                                │
        ┌───────────┬───────────┼───────────┬───────────┐
        ▼           ▼           ▼           ▼           ▼
┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐
│   Patient   │ │  Encounter  │ │   Journal   │ │    Task     │ │    Auth     │
│    8080     │ │    8081     │ │    8082     │ │    8083     │ │    8084     │
└─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘
        │               │               │               │               │
        └───────────────┴───────────────┴───────────────┴───────────────┘
                                        │
                                        ▼
                        ┌───────────────────────────┐
                        │      RabbitMQ (5672)      │
                        │        Event Bus          │
                        └───────────────────────────┘
                                        │
                                        ▼
                        ┌───────────────────────────┐
                        │    PostgreSQL (5441)      │
                        │     Delad databas         │
                        └───────────────────────────┘
```

### Rekommenderad konsolidering (fas 2)

```
┌─────────────────────────────────────────────────────────────────┐
│                         Frontend (3001)                         │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Integration Gateway (8085)                    │
└─────────────────────────────────────────────────────────────────┘
                                │
        ┌───────────────────────┼───────────────────────┐
        ▼                       ▼                       ▼
┌─────────────────┐   ┌─────────────────────┐   ┌─────────────────┐
│  KÄRNA-MONOLIT  │   │    STÖDTJÄNSTER     │   │    SECURITY     │
│                 │   │                     │   │                 │
│  • Patient      │   │  • Medication       │   │  • Auth         │
│  • Encounter    │   │  • Lab              │   │  • Consent      │
│  • Journal      │   │  • Referral         │   │  • Audit        │
│  • Task         │   │  • Coding           │   │                 │
│  • Triage       │   │  • Certificates     │   │                 │
│  • Booking      │   │                     │   │                 │
│  • Forms        │   │                     │   │                 │
└─────────────────┘   └─────────────────────┘   └─────────────────┘
        8080                8090-8097                8084-8096
```

---

## Kommunikationsmönster

### Synkron kommunikation (REST)
- Frontend → Integration Gateway → Backend-tjänster
- Tjänst-till-tjänst för realtidsdata

### Asynkron kommunikation (RabbitMQ)
- Events för viktiga domänhändelser:
  - `encounter.started`
  - `encounter.finished`
  - `task.created`
  - `task.completed`
  - `note.signed`

### Delad databas
- Alla tjänster delar samma PostgreSQL-instans
- Varje modul har prefix på Flyway-historik (`flyway_schema_history_{module}`)
- Separata scheman per modul övervägs för fas 3

---

## Beslut att ta

Följande beslut behöver fattas innan nästa fas:

1. **Konsolidering:** Vilka moduler ska slås ihop?
2. **Databasstrategi:** En databas eller flera?
3. **API-versioning:** Hur hanteras breaking changes?
4. **Testmatris:** Hur testar vi modul-interaktioner?

---

## Implementationsordning

### Fas 1: Kärnan (nu)
- [x] Patient (A1) - tjänst
- [x] Care Encounter (A2) - tjänst
- [x] Journal (A3) - tjänst
- [x] Task (B1) - tjänst
- [x] Authorization (C2) - tjänst
- [x] Integration (E1) - tjänst

### Fas 2: Djup i kärnan
- [ ] Fullständig CRUD för alla kärnmoduler
- [ ] Event-publicering mellan moduler
- [ ] Fullständig testning
- [ ] API-dokumentation komplett

### Fas 3: Stödmoduler
- [ ] Medication (A7)
- [ ] Lab (A8)
- [ ] Referral (A6)
- [ ] Certificates (B5)

### Fas 4: Konsolidering
- [ ] Utvärdera tjänstegränser
- [ ] Eventuell sammanslagning
- [ ] Prestandaoptimering

---

## Principer för nya moduler

Innan en ny modul skapas:

1. **Behövs den?** Kan funktionen ligga i befintlig modul?
2. **Egen tjänst?** Måste den vara separat deploybar?
3. **Kontrakt först:** OpenAPI-spec före implementation
4. **Tester:** Minst 80% kodtäckning
5. **Dokumentation:** README i modulkatalogen

