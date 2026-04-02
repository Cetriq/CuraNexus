# CuraNexus

Swedish Healthcare Provider Platform - a modular, process-driven healthcare system.

## Prerequisites

- Java 21+
- Maven 3.8+
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
| PostgreSQL | 5441 | Database |
| pgAdmin | 5050 | Database admin (dev profile) |

## API Documentation

Each service exposes Swagger UI at `/swagger-ui.html`:
- http://localhost:8080/swagger-ui.html (Patient)
- http://localhost:8081/swagger-ui.html (Care-Encounter)
- etc.

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

## Architecture

- **Language**: Java 21
- **Framework**: Spring Boot 3.2
- **Database**: PostgreSQL 15
- **API**: REST with OpenAPI/Swagger
- **FHIR**: R4 via HAPI FHIR 7.0.2

## Modules

```
modules/
├── patient/         # A1 - Patient Identity
├── care-encounter/  # A2 - Care Encounters
├── journal/         # A3 - Clinical Documentation
├── triage/          # A4 - Triage & Assessment
├── task/            # B1 - Task & Workflow
├── authorization/   # C2 - Authorization (RBAC+ABAC)
├── audit/           # C3 - Audit & Logging
└── integration/     # E1 - API Gateway
```

## License

Licensed under the [European Union Public Licence (EUPL) v1.2](LICENSE).

The EUPL is the official open source licence of the European Union, designed for public sector software sharing across EU member states.
