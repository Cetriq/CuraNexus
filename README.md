# CuraNexus

**Swedish Healthcare Provider Platform** - A modular, process-driven healthcare system designed for 100,000+ users.

> **Primary Principle:** The care process is primary. The journal is a byproduct.

## Features

### Core Healthcare Modules
- **Patient Identity** - Personnummer-based patient management with protected identity support
- **Care Encounters** - Full encounter lifecycle with readiness checking
- **Clinical Documentation** - Notes, diagnoses, procedures with signing workflow
- **Triage** - RETTS-inspired emergency triage system

### Clinical Support Modules
- **Booking** - Appointment scheduling with check-in and visit lifecycle
- **Medication** - Prescription management with drug interaction checking
- **Referral** - Referral workflow (draft → sent → received → scheduled)
- **Lab** - Lab order management with result tracking

### Workflow & Tasks
- **Task Management** - Configurable task templates with automatic creation
- **Task Dependencies** - Sequential task execution with blocking/unblocking
- **Due Dates & Escalation** - Automatic escalation of overdue tasks
- **Progress Tracking** - Real-time encounter completion tracking

### Security & Compliance
- **RBAC + ABAC** - Role-based and attribute-based access control
- **Care Relations** - Access requires active care relation (PDL compliance)
- **Emergency Access** - "Nödåtkomst" with mandatory reason logging
- **Full Audit Trail** - All access decisions logged

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
git clone https://github.com/your-org/curanexus.git
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

| Service | Port | Description |
|---------|------|-------------|
| Patient | 8080 | Patient identity management |
| Care-Encounter | 8081 | Care encounter lifecycle |
| Journal | 8082 | Clinical documentation (notes, diagnoses, procedures) |
| Task | 8083 | Task and workflow management |
| Authorization | 8084 | RBAC + ABAC authorization |
| Integration Gateway | 8085 | API Gateway with FHIR support |
| Audit | 8086 | PDL-compliant audit logging |
| Triage | 8087 | RETTS-inspired triage system |
| Notification | 8088 | Notifications and messaging |
| Booking | 8089 | Appointment scheduling |
| Medication | 8090 | Prescription management |
| Referral | 8091 | Referral workflow |
| Lab | 8092 | Lab orders and results |
| RabbitMQ | 5672/15672 | Message broker |
| PostgreSQL | 5441 | Database |
| pgAdmin | 5050 | Database admin (dev profile) |

## API Documentation

Each service exposes Swagger UI at `/swagger-ui.html`:
- http://localhost:8080/swagger-ui.html (Patient)
- http://localhost:8081/swagger-ui.html (Care-Encounter)
- http://localhost:8082/swagger-ui.html (Journal)
- http://localhost:8083/swagger-ui.html (Task)
- http://localhost:8084/swagger-ui.html (Authorization)
- http://localhost:8089/swagger-ui.html (Booking)
- http://localhost:8090/swagger-ui.html (Medication)
- http://localhost:8091/swagger-ui.html (Referral)
- http://localhost:8092/swagger-ui.html (Lab)

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

### Module Structure

```
modules/
├── patient/           # A1 - Patient Identity
├── care-encounter/    # A2 - Care Encounters
├── journal/           # A3 - Clinical Documentation
├── triage/            # A4 - Triage & Assessment
├── task/              # B1 - Task & Workflow
├── booking/           # B2 - Appointment Scheduling
├── medication/        # B3 - Prescription Management
├── referral/          # B4 - Referral Workflow
├── lab/               # B5 - Lab Orders & Results
├── authorization/     # C2 - Authorization (RBAC+ABAC)
├── audit/             # C3 - Audit & Logging
├── events/            # Shared - Domain Events
├── notification/      # D1 - Notifications
└── integration/       # E1 - API Gateway & FHIR
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
| System | Description |
|--------|-------------|
| ICD-10-SE | Swedish ICD-10 diagnoses |
| KVÅ | Swedish procedure codes |
| SNOMED CT-SE | Swedish SNOMED CT edition |
| RETTS | Emergency triage levels |

## License

Licensed under the [European Union Public Licence (EUPL) v1.2](LICENSE).

The EUPL is the official open source licence of the European Union, designed for public sector software sharing across EU member states.
