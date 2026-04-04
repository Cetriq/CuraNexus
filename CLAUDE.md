# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Cura Nexus** – Healthcare Provider Platform (Sweden)

A unified, process-driven platform replacing fragmented healthcare systems. Designed for 100,000 users.

> **Primary Principle:** The care process is primary. The journal is a byproduct.

## Build Commands

```bash
# Build entire project
mvn clean install

# Build specific module
mvn clean install -pl modules/patient

# Run tests
mvn test

# Run single test class
mvn test -pl modules/patient -Dtest=PatientServiceTest

# Run integration tests (requires Docker for Testcontainers)
mvn verify -pl modules/patient

# Run application
mvn spring-boot:run -pl modules/patient
```

**Prerequisites:**
- Java 21
- Maven 3.9+
- Docker (for Testcontainers integration tests)

## Technology Stack (Non-Negotiable)

- **Language:** Java 21
- **Framework:** Spring Boot
- **Database:** PostgreSQL
- **API:** OpenAPI-first (contract before code)
- **Migrations:** Flyway or Liquibase
- **Testing:** JUnit + Testcontainers

**Forbidden:**
- No dynamic languages in core backend
- No direct SQL without repository/service layer
- No schema changes without migration
- No secrets in code
- No bypass of security layers

## Architecture

See `ARCHITECTURE.md` for detailed decisions on service boundaries and deployment.

### Core Modules (MVP Priority)

These 5 modules form the system core and should have deep implementation before others:

| Priority | Module | Description |
|----------|--------|-------------|
| 1 | Patient (A1) | Base data model |
| 2 | Care Encounter (A2) | All care starts here |
| 3 | Journal (A3) | All care documented here |
| 4 | Task (B1) | All care executed here |
| 5 | Authorization (C2) | Access control |

Each module must contain: API layer, Service layer, Domain model, Persistence layer.

Modules communicate only via API or Events – no direct dependencies.

### Project Structure

```
curanexus/
├── frontend/          # React frontend (root level, NOT under modules/)
├── modules/           # Backend Java modules
├── docker/            # Docker configuration
└── scripts/           # Build and test scripts
```

**Important:** Frontend lives at project root, not under modules/.

### Key Principles
1. **Flow First** – Features map to care processes, no isolated journal-only functionality
2. **Structured Data First** – Structured/machine-readable data is default; free text is secondary
3. **Single Source of Truth** – No duplicated core data, explicit ownership
4. **API First** – All functionality via API, no UI-only logic
5. **Zero Trust Access** – Access requires care relation context, all access logged
6. **Event-Driven (Selective)** – Events where meaningful, not everywhere

## Development Method (Mandatory Order)

1. Understand context
2. Define module and scope
3. Define API contract (OpenAPI)
4. Define data model
5. Implement service logic
6. Implement persistence
7. Write tests
8. Run tests
9. Document
10. Update session ledger

**No step may be skipped.**

## AI Behavior Rules

**MUST:**
- Never guess architecture
- Never invent patterns outside defined standards
- Always prefer explicit over implicit
- Always validate before proceeding
- Always produce testable code
- Explain decisions when uncertain

**MUST NOT:**
- Introduce new frameworks
- Modify core architecture without stating it
- Skip validation or testing
- Continue if uncertainty is high

### Autonomy Levels
- **Interactive:** Ask before major decisions
- **Guided Autonomous:** Execute within defined module
- **Sandboxed Autonomous:** Execute freely within sandbox ONLY

Never escalate autonomy without instruction.

## Session Management

### On Stop
1. Summarize completed work
2. List modified files
3. List open issues
4. Define next step
5. Generate resume prompt
6. Save to: `summary.md`, `ledger.json`, `resume-prompt.md`

### On Continue
1. Read CLAUDE.md
2. Read ARCHITECTURE.md
3. Read latest session ledger
4. Read latest summary
5. Read decisions.md (if exists)
6. Validate repo state
7. Continue from defined next step

## Security Rules

- No plaintext sensitive data
- All access authenticated and authorized
- Logging must include: who, what, when
- No hidden data flows

## Testing Rules

- All code must have unit tests
- All tests must pass before completion
- Use Testcontainers for integration tests
- Tests must be runnable without manual setup

## Database Rules

- PostgreSQL only
- All schema changes via migration
- No breaking changes without versioning
- Use transactions where required
- No business logic in SQL

## API Rules

- OpenAPI contract first
- Versioned endpoints
- Backward compatibility required
- Validation at API boundary
- No undocumented endpoints

## When In Doubt

> Choose structure over speed
> Choose safety over convenience
> Choose clarity over cleverness

If uncertain: STOP, explain uncertainty, propose options, wait or choose safest path.
