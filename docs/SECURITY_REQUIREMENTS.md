# Säkerhetskrav för CuraNexus

Detta dokument beskriver säkerhetskrav som ska implementeras för Forms-, Certificates- och Consent-modulerna.

## Status

**Nuvarande:** Modulerna saknar autentisering och auktorisation. Detta är acceptabelt för utvecklingsfasen men **MÅSTE** åtgärdas innan produktion.

## 1. Autentisering (Authentication)

### Krav
- Alla API-endpoints ska kräva autentiserad användare
- Stöd för SITHS-kort (HSA-ID) för vårdpersonal
- Stöd för BankID för patienter via 1177
- JWT-tokens för sessionshantering

### Implementation
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        return http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt())
            .build();
    }
}
```

### Endpoints utan autentisering
- `GET /actuator/health` - hälsokontroll för Kubernetes
- `GET /api/v1/*/openapi` - OpenAPI-dokumentation (dev-miljö)

## 2. Auktorisation (Authorization)

### Roller
| Roll | Beskrivning |
|------|-------------|
| `ROLE_DOCTOR` | Läkare - kan skapa/signera intyg |
| `ROLE_NURSE` | Sjuksköterska - kan fylla i formulär |
| `ROLE_SECRETARY` | Sekreterare - kan hantera utkast |
| `ROLE_PATIENT` | Patient - kan se sina egna uppgifter |
| `ROLE_ADMIN` | Administratör - kan hantera mallar |

### Behörighetsregler per modul

#### Forms-modulen
| Operation | Roller |
|-----------|--------|
| Skapa formulärmall | ADMIN |
| Fylla i formulär | DOCTOR, NURSE |
| Granska formulär | DOCTOR |
| Se egna formulär | PATIENT |

#### Certificates-modulen
| Operation | Roller |
|-----------|--------|
| Skapa intyg | DOCTOR |
| Signera intyg | DOCTOR (med legitimation) |
| Skicka intyg | DOCTOR, SECRETARY |
| Makulera intyg | DOCTOR |
| Se egna intyg | PATIENT |

#### Consent-modulen
| Operation | Roller |
|-----------|--------|
| Skapa samtycke | DOCTOR, NURSE |
| Aktivera samtycke | PATIENT |
| Återkalla samtycke | PATIENT |
| Skapa spärr | PATIENT |
| Kontrollera åtkomst | DOCTOR, NURSE |

### Implementation
```java
@PreAuthorize("hasRole('DOCTOR')")
public CertificateDto signCertificate(UUID id, SignRequest request) {
    // Kontrollera att läkaren har vårdrelation till patienten
    // ...
}
```

## 3. Vårdrelationskontroll

### Krav enligt PDL (Patientdatalagen)
- Åtkomst till patientdata kräver pågående vårdrelation
- Nödåtkomst ("break the glass") måste loggas särskilt
- Spärrar (Consent-modulens AccessBlock) måste respekteras

### Implementation
```java
@Component
public class CareRelationValidator {

    public boolean hasActiveCareRelation(UUID practitionerId, UUID patientId) {
        // Kontrollera i Encounter-modulen om vårdkontakt finns
        // Kontrollera spärrar via Consent-modulen
    }
}
```

## 4. Audit Logging

### Krav
Alla dataåtkomster ska loggas med:
- **Vem** (HSA-ID/personnummer)
- **Vad** (vilken resurs, operation)
- **När** (tidsstämpel)
- **Varför** (vårdrelationskontext)

### Implementation
```java
@Aspect
@Component
public class AuditAspect {

    @Around("@annotation(Audited)")
    public Object audit(ProceedingJoinPoint joinPoint) {
        AuditEvent event = AuditEvent.builder()
            .userId(SecurityContextHolder.getContext().getUserId())
            .action(extractAction(joinPoint))
            .resourceType(extractResourceType(joinPoint))
            .resourceId(extractResourceId(joinPoint))
            .timestamp(Instant.now())
            .build();

        auditRepository.save(event);
        return joinPoint.proceed();
    }
}
```

### Audit-tabell
```sql
CREATE TABLE audit_log (
    id UUID PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    user_role VARCHAR(30) NOT NULL,
    action VARCHAR(50) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    resource_id UUID,
    patient_id UUID,
    care_unit_id UUID,
    timestamp TIMESTAMPTZ NOT NULL,
    ip_address VARCHAR(45),
    details JSONB
);
```

## 5. Dataskydd

### Kryptering
- **I transit:** TLS 1.3 obligatoriskt
- **I vila:** AES-256 för känsliga fält (diagnoser, etc.)

### Personnummer-hantering
- Personnummer ska hashas för loggar
- Fullständigt personnummer endast vid behov

## 6. Input-validering

### Krav
- Alla API-inputs valideras vid gränsen
- Whitelist-validering där möjligt
- SQL Injection-skydd via JPA/parameterized queries
- XSS-skydd via output encoding

### Befintlig implementation
Modulerna använder redan:
- `@Valid` på request DTOs
- Jakarta Validation annotations
- Spring Data JPA (skyddar mot SQL injection)

## 7. Prioriterad åtgärdsplan

### Fas 1: Grundläggande säkerhet (Innan test)
1. Lägg till Spring Security-dependency
2. Konfigurera JWT-validering mot auth-server
3. Lägg till `@PreAuthorize` på controllers
4. Implementera audit-logging

### Fas 2: Vårdrelationskontroll (Innan pilot)
1. Integrera med Encounter-modulen
2. Implementera CareRelationValidator
3. Integrera spärrfunktionalitet från Consent

### Fas 3: Fullständig compliance (Innan produktion)
1. Penetrationstest
2. GDPR-review
3. PDL-compliance verifiering
4. Säkerhetsgranskning av extern part

## 8. Konfiguration per miljö

### Development
```yaml
security:
  enabled: false  # Endast för lokal utveckling
```

### Test/Staging
```yaml
security:
  enabled: true
  jwt:
    issuer: https://auth-test.curanexus.se
  audit:
    enabled: true
```

### Production
```yaml
security:
  enabled: true
  jwt:
    issuer: https://auth.curanexus.se
  audit:
    enabled: true
    retention-days: 3650  # 10 år enligt PDL
  care-relation:
    strict-mode: true
```

## Referenser

- [Patientdatalagen (PDL)](https://www.riksdagen.se/sv/dokument-lagar/dokument/svensk-forfattningssamling/patientdatalag-2008355_sfs-2008-355)
- [GDPR](https://eur-lex.europa.eu/eli/reg/2016/679/oj)
- [Socialstyrelsens föreskrifter om journalföring](https://www.socialstyrelsen.se/regler-och-riktlinjer/foreskrifter-och-allmanna-rad/konsoliderade-foreskrifter/201628-om-journalforing-och-behandling-av-personuppgifter-i-halso--och-sjukvarden/)
