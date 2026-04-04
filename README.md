# CuraNexus

**Swedish Healthcare Provider Platform** - A modular, process-driven healthcare system designed for 100,000+ users.

> **Primary Principle:** The care process is primary. The journal is a byproduct.

## Features

### Core Healthcare Modules (A-series)
- **A1 Patient Identity** - Personnummer-based patient management with protected identity support
- **A2 Care Encounters** - Full encounter lifecycle with readiness checking
- **A3 Clinical Documentation** - Notes, diagnoses, procedures with signing workflow
- **A4 Triage** - RETTS-inspired emergency triage system
- **A5 Booking** - Appointment scheduling with check-in and visit lifecycle
- **A6 Referral** - Referral workflow (draft → sent → received → scheduled)
- **A7 Medication** - Prescription management with drug interaction checking
- **A8 Lab** - Lab order management with result tracking
- **A13 Forms** - Dynamic form templates with validation and submissions

### Operational Modules (B-series)
- **B1 Task Management** - Configurable task templates with automatic creation
- **B4 Coding & Classification** - ICD-10-SE, KVÅ, ATC code systems with search and validation
- **B5 Certificates** - Medical certificate management (FK7263, FK7804, etc.)

### Security & Compliance (C-series)
- **C1 Consent** - Patient consent management with access blocks (spärrar)
- **C2 Authorization** - RBAC + ABAC access control with care relation context
- **C3 Audit** - PDL-compliant audit logging (WHO, WHAT, WHEN, WHERE, WHY)

### Intelligence & Events (D-series)
- **D4 Notification** - Event publishing and notifications

### Platform (E-series)
- **E1 Integration Gateway** - API Gateway with FHIR R4 support

### Integration & Interoperability
- **FHIR R4** - Full FHIR support via HAPI FHIR 7.0.2
- **Swedish Extensions** - Personnummer, HSA-ID, ICD-10-SE, KVÅ, RETTS
- **API Gateway** - Unified gateway with circuit breaker patterns
- **Event-Driven** - RabbitMQ-based module communication

## Prerequisites

- Java 21+
- Maven 3.9+
- Docker & Docker Compose

## Quick Start

```bash
# 1. Clone the repository
git clone https://github.com/curanexus/curanexus.git
cd curanexus

# 2. Copy environment variables
cp .env.example .env

# 3. Build and start all services
./build-docker.sh
docker-compose up -d

# 4. Verify all services are healthy
docker-compose ps
```

## Services

| Service | Port | Module | Description |
|---------|------|--------|-------------|
| Patient | 8080 | A1 | Patient identity management |
| Care-Encounter | 8081 | A2 | Care encounter lifecycle |
| Journal | 8082 | A3 | Clinical documentation |
| Task | 8083 | B1 | Task and workflow management |
| Authorization | 8084 | C2 | RBAC + ABAC authorization |
| Integration | 8085 | E1 | API Gateway with FHIR support |
| Triage | 8087 | A4 | RETTS-inspired triage system |
| Notification | 8088 | D4 | Notifications and messaging |
| Booking | 8089 | A5 | Appointment scheduling |
| Medication | 8090 | A7 | Prescription management |
| Referral | 8091 | A6 | Referral workflow |
| Lab | 8092 | A8 | Lab orders and results |
| Forms | 8093 | A13 | Dynamic form templates |
| Certificates | 8094 | B5 | Medical certificates |
| Consent | 8095 | C1 | Consent and access blocks |
| Audit | 8096 | C3 | PDL-compliant audit logging |
| Coding | 8097 | B4 | ICD-10, KVÅ, ATC code systems |
| Frontend | 3001 | — | React web application |
| RabbitMQ | 5672/15672 | — | Message broker |
| PostgreSQL | 5441 | — | Database |
| pgAdmin | 5050 | — | Database admin (dev profile) |

## API Documentation

Each service exposes OpenAPI documentation:
- http://localhost:8080/swagger-ui.html (Patient)
- http://localhost:8081/swagger-ui.html (Care-Encounter)
- http://localhost:8082/swagger-ui.html (Journal)
- http://localhost:8083/swagger-ui.html (Task)
- http://localhost:8084/swagger-ui.html (Authorization)
- http://localhost:8089/swagger-ui.html (Booking)
- http://localhost:8090/swagger-ui.html (Medication)
- http://localhost:8091/swagger-ui.html (Referral)
- http://localhost:8092/swagger-ui.html (Lab)
- http://localhost:8093/api/v1/forms/swagger-ui (Forms)
- http://localhost:8094/api/v1/certificates/swagger-ui (Certificates)
- http://localhost:8095/api/v1/consent/swagger-ui (Consent)
- http://localhost:8096/api/v1/audit/swagger-ui (Audit)
- http://localhost:8097/api/v1/coding/swagger-ui (Coding)

### FHIR Endpoints

The Integration Gateway provides FHIR R4 endpoints:
- `GET /fhir/metadata` - Capability statement
- `GET /fhir/Patient/{id}` - Read patient
- `GET /fhir/Patient?identifier={personnummer}` - Search patients
- `GET /fhir/Encounter/{id}` - Read encounter
- `GET /fhir/Observation?patient={id}&category=vital-signs` - Vital signs

## Development

### Run tests
```bash
mvn test
```

### Build without Docker
```bash
mvn clean package -DskipTests
```

### Start individual service
```bash
cd modules/patient
mvn spring-boot:run
```

### Start with dev tools (pgAdmin)
```bash
docker-compose --profile dev up -d
```

### Run E2E tests
```bash
# Core workflow test (encounter → tasks → notes → finish)
./scripts/e2e-workflow-test.sh

# New modules test (booking, medication, referral, lab)
./scripts/e2e-new-modules-test.sh
```

## Architecture

### Technology Stack
- **Language**: Java 21
- **Framework**: Spring Boot 3.2
- **Database**: PostgreSQL 15
- **Messaging**: RabbitMQ 3.12
- **API**: REST with OpenAPI/Swagger
- **FHIR**: R4 via HAPI FHIR 7.0.2

### Project Structure

```
curanexus/
├── frontend/              # React Web Application (root level)
├── modules/
│   ├── patient/           # A1 - Patient Identity
│   ├── care-encounter/    # A2 - Care Encounters
│   ├── journal/           # A3 - Clinical Documentation
│   ├── triage/            # A4 - Triage & Assessment
│   ├── booking/           # A5 - Appointment Scheduling
│   ├── referral/          # A6 - Referral Workflow
│   ├── medication/        # A7 - Prescription Management
│   ├── lab/               # A8 - Lab Orders & Results
│   ├── forms/             # A13 - Dynamic Forms
│   ├── task/              # B1 - Task & Workflow
│   ├── coding/            # B4 - Code Systems (ICD-10, KVÅ, ATC)
│   ├── certificates/      # B5 - Medical Certificates
│   ├── consent/           # C1 - Consent & Access Blocks
│   ├── authorization/     # C2 - Authorization (RBAC+ABAC)
│   ├── audit/             # C3 - PDL-compliant Audit Logging
│   ├── notification/      # D4 - Notifications & Events
│   ├── integration/       # E1 - API Gateway & FHIR
│   └── events/            # Shared - Domain Events
├── docker/                # Docker configuration
├── scripts/               # Build and test scripts
└── docs/                  # Documentation
```

### Key Design Principles

1. **Flow First** - Features map to care processes
2. **Structured Data First** - Machine-readable data is default
3. **Single Source of Truth** - No duplicated core data
4. **API First** - All functionality via API
5. **Zero Trust Access** - Access requires care relation context
6. **Event-Driven** - Modules communicate via events

## Swedish Healthcare Standards

### Supported Identifiers
| Identifier | OID | Description |
|------------|-----|-------------|
| Personnummer | 1.2.752.129.2.1.3.1 | Swedish personal identity number |
| Samordningsnummer | 1.2.752.129.2.1.3.3 | Coordination number |
| HSA-ID | 1.2.752.129.2.1.4.1 | Healthcare personnel identifier |

### Supported Code Systems
| System | Description | Module |
|--------|-------------|--------|
| ICD-10-SE | Swedish ICD-10 diagnoses | B4 Coding |
| KVÅ | Swedish procedure codes | B4 Coding |
| ATC | Medication classification | B4 Coding |
| SNOMED CT-SE | Swedish SNOMED CT edition | — |
| RETTS | Emergency triage levels | A4 Triage |

### Medical Certificates
| Type | Description |
|------|-------------|
| FK7263 | Sjukintyg (sick leave certificate) |
| FK7804 | Läkarintyg för sjukpenning |
| FK7800 | Läkarutlåtande för aktivitetsersättning |
| DOD | Dödsbevis (death certificate) |

## License

Licensed under the [European Union Public Licence (EUPL) v1.2](LICENSE).

The EUPL is the official open source licence of the European Union, designed for public sector software sharing across EU member states.
