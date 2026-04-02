# Architectural Decisions

## 2026-03-29: A1 Patient Module

### Decision 1: Primary Patient Identifier
**Context:** Need to decide how patients are uniquely identified in the system.
**Decision:** Use Swedish personnummer (12 digits) as primary identifier.
**Rationale:**
- National standard in Swedish healthcare
- Enables automatic extraction of date of birth and gender
- No need for separate identity lookup service

### Decision 2: API Language
**Context:** Whether to use Swedish or English for API naming.
**Decision:** Use English for all API endpoints and field names.
**Rationale:**
- International developer accessibility
- Consistent with industry standards
- Domain terms remain Swedish in documentation

### Decision 3: Build Tool
**Context:** Maven vs Gradle for the project.
**Decision:** Maven.
**Rationale:**
- User preference
- Well-established in enterprise Java
- Simpler learning curve for team members
- Good IDE support

### Decision 4: Module Structure
**Context:** How to organize code within each module.
**Decision:** Four-layer architecture: api, service, domain, repository.
**Rationale:**
- Clear separation of concerns
- Matches CLAUDE.md requirements
- Easy to test each layer independently
- Standard Spring Boot pattern

### Decision 5: Protected Identity Handling
**Context:** How to handle patients with "skyddade personuppgifter".
**Decision:** Boolean flag on Patient entity, personnummer masked in summary views.
**Rationale:**
- Simple implementation
- Prevents accidental exposure in lists
- Full details available when explicitly requested (with proper authorization, to be implemented in C2)

## 2026-03-30: Docker Infrastructure

### Decision 6: Database Container Strategy
**Context:** Whether to use separate PostgreSQL containers per module or share one instance.
**Decision:** One PostgreSQL container with three separate databases.
**Rationale:**
- Simpler infrastructure and resource management
- Databases: `curanexus` (shared by patient, care-encounter, journal, task, authorization), `curanexus_audit` (separate for compliance), `curanexus_triage` (separate for emergency department)
- Matches production pattern where audit and triage have isolation requirements
- Easier local development setup

### Decision 7: Dockerfile Strategy
**Context:** Whether to have separate Dockerfiles per module or a shared one.
**Decision:** Shared Dockerfile in project root with build-args.
**Rationale:**
- All modules are Spring Boot applications with identical runtime requirements
- Build-args (`MODULE_NAME`) allow per-module customization
- Reduces maintenance overhead
- Consistent security and JVM configuration across all services

### Decision 8: Container Image Base
**Context:** Which base image to use for Java 21 applications.
**Decision:** `eclipse-temurin:21-jre-alpine`
**Rationale:**
- Eclipse Temurin is the community-driven successor to AdoptOpenJDK
- JRE-only (not JDK) for smaller image size
- Alpine Linux for minimal footprint (~200MB per service)
- Well-maintained and security-patched

### Decision 9: Container Security
**Context:** How to run applications securely in containers.
**Decision:** Non-root user with UID 1001.
**Rationale:**
- Security best practice (no root in containers)
- UID 1001 avoids conflicts with system users
- User/group named `curanexus` for clarity
- Consistent across all service containers

### Decision 10: Service Ports
**Context:** Port allocation for microservices.
**Decision:** Fixed port per service (8080-8087).
**Rationale:**
- patient: 8080
- care-encounter: 8081
- journal: 8082
- task: 8083
- authorization: 8084
- integration: 8085
- audit: 8086
- triage: 8087
- Predictable and easy to remember
- No port conflicts when running all services locally

### Decision 11: Development Tools
**Context:** Whether to include development tools in docker-compose.
**Decision:** Include pgAdmin on dev profile.
**Rationale:**
- pgAdmin accessible at port 5050 for database management
- Only starts with `--profile dev` flag
- Keeps production deployment lean
- Speeds up local development and debugging
